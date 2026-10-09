package com.zhiqi.platform;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiqi.platform.auth.AuthService;
import com.zhiqi.platform.auth.RateLimiter;
import com.zhiqi.platform.auth.Requests;
import com.zhiqi.platform.auth.TokenCodec;
import com.zhiqi.platform.common.ApiException;
import com.zhiqi.platform.config.PlatformProperties;
import com.zhiqi.platform.sms.SmsGateway;
import com.zhiqi.platform.sms.SmsService;
import com.zhiqi.platform.user.Role;
import com.zhiqi.platform.user.UserRepository;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(
        properties = {
            "spring.config.import=",
            "spring.datasource.url=${test.database.url:jdbc:h2:mem:platform-tests;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000}",
            "spring.datasource.username=${test.database.username:sa}",
            "spring.datasource.password=${test.database.password:}",
            "spring.sql.init.schema-locations=${test.database.schema:classpath:schema.sql}",
            "app.bootstrap.enabled=false",
            "app.sms.mode=console",
            "app.sms.secret=test-secret-key",
            "app.public-origin="
        })
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class PlatformApiTest {
    private static final String PASSWORD = "Test-password-123";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired com.zhiqi.platform.common.DatabaseDialect dialect;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired RateLimiter limits;
    @Autowired AuthService auth;
    @Autowired TransactionTemplate transactions;
    private Cookie admin;
    private long adminId;
    private final AtomicInteger numbers = new AtomicInteger();

    @BeforeEach
    void reset() throws Exception {
        for (String table :
                List.of("audit_logs", "purchases", "sessions", "sms_codes", "rate_limits"))
            jdbc.update("DELETE FROM " + table);
        // MySQL 对自引用外键逐行检查，先删学生、老师，再删根账号。
        jdbc.update(
                "DELETE FROM users WHERE role IN ('ordinary','learner') AND parent_id IS NOT NULL");
        jdbc.update("DELETE FROM users WHERE role='teacher'");
        jdbc.update("DELETE FROM users");
        adminId =
                users.insert(
                        "13800000000", "管理员", passwords.encode(PASSWORD), Role.ADMIN, null, 0, 0);
        admin = cookie(login("13800000000"));
        numbers.set(0);
    }

    private MvcResult request(String method, String path, Cookie cookie, Object body)
            throws Exception {
        var builder =
                MockMvcRequestBuilders.request(HttpMethod.valueOf(method), "/api" + path)
                        .contentType(MediaType.APPLICATION_JSON);
        if (cookie != null) builder.cookie(cookie);
        if (body != null) builder.content(mapper.writeValueAsBytes(body));
        return mvc.perform(builder).andReturn();
    }

    private int status(MvcResult result) {
        return result.getResponse().getStatus();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private Cookie cookie(MvcResult result) {
        String header = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(header);
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.contains("SameSite=Lax"));
        String[] pair = header.split(";", 2)[0].split("=", 2);
        return new Cookie(pair[0], pair[1]);
    }

    private MvcResult login(String phone) throws Exception {
        return request("POST", "/auth/login", null, Map.of("phone", phone, "password", PASSWORD));
    }

    private MvcResult create(Cookie actor, String role, Map<String, Object> extra)
            throws Exception {
        var input = new java.util.HashMap<String, Object>(extra);
        input.put("phone", "139100" + String.format("%05d", numbers.incrementAndGet()));
        input.put("name", role);
        input.put("password", PASSWORD);
        input.put("role", role);
        return request("POST", "/users", actor, input);
    }

    private JsonNode created(Cookie actor, String role, Map<String, Object> extra)
            throws Exception {
        var result = create(actor, role, extra);
        assertEquals(201, status(result), result.getResponse().getContentAsString());
        return json(result).get("user");
    }

    @Test
    void smsRegistrationRejectsPrivilegeEscalationAndReplay() throws Exception {
        String phone = "13900000001";
        var base =
                new java.util.HashMap<String, Object>(
                        Map.of(
                                "phone",
                                phone,
                                "name",
                                "小明",
                                "password",
                                PASSWORD,
                                "code",
                                "123456"));
        assertEquals(400, status(request("POST", "/auth/register", null, base)));
        var sent = request("POST", "/auth/sms", null, Map.of("phone", phone));
        assertEquals(200, status(sent));
        String code = json(sent).get("devCode").asText();
        assertTrue(code.matches("\\d{6}"));
        assertNotEquals(
                code,
                jdbc.queryForObject(
                        "SELECT code_hash FROM sms_codes WHERE phone=?", String.class, phone));
        assertEquals(429, status(request("POST", "/auth/sms", null, Map.of("phone", phone))));
        base.put("code", code);
        base.put("role", "admin");
        assertEquals(403, status(request("POST", "/auth/register", null, base)));
        base.remove("role");
        base.put("code", "000000");
        assertEquals(400, status(request("POST", "/auth/register", null, base)));
        base.put("code", code);
        var registration = request("POST", "/auth/register", null, base);
        assertEquals(201, status(registration));
        var user = json(registration).get("user");
        assertEquals("ordinary", user.get("role").asText());
        assertFalse(user.has("password_hash"));
        assertFalse(user.has("passwordHash"));
        assertTrue(users.find(user.get("id").asLong()).passwordHash().startsWith("$2a$"));
        var session = cookie(registration);
        assertEquals(200, status(request("GET", "/auth/me", session, null)));
        assertEquals(403, status(request("GET", "/learning/access", session, null)));
        assertEquals(403, status(request("GET", "/users", session, null)));
        assertEquals(400, status(request("POST", "/auth/register", null, base)));
        assertEquals(200, status(request("POST", "/auth/logout", session, Map.of())));
        assertEquals(401, status(request("GET", "/auth/me", session, null)));
    }

    @Test
    void expiredCodesAndFiveWrongAttemptsRemainBlocked() throws Exception {
        String phone = "13900000002";
        String code =
                json(request("POST", "/auth/sms", null, Map.of("phone", phone)))
                        .get("devCode")
                        .asText();
        var input =
                new java.util.HashMap<String, Object>(
                        Map.of(
                                "phone",
                                phone,
                                "name",
                                "验证码用户",
                                "password",
                                PASSWORD,
                                "code",
                                "000000"));
        for (int i = 0; i < 5; i++)
            assertEquals(400, status(request("POST", "/auth/register", null, input)));
        assertEquals(
                5,
                jdbc.queryForObject(
                        "SELECT attempts FROM sms_codes WHERE phone=?", Integer.class, phone));
        input.put("code", code);
        assertEquals(400, status(request("POST", "/auth/register", null, input)));
        jdbc.update("UPDATE sms_codes SET attempts=0,expires_at=0 WHERE phone=?", phone);
        assertEquals(400, status(request("POST", "/auth/register", null, input)));
        jdbc.update("UPDATE sms_codes SET sent_at=0 WHERE phone=?", phone);
        assertEquals(200, status(request("POST", "/auth/sms", null, Map.of("phone", phone))));
    }

    @Test
    void hierarchyAndConcurrentQuotasAreEnforced() throws Exception {
        var principal = created(admin, "principal", Map.of("teacherQuota", 2, "studentQuota", 3));
        var other = created(admin, "principal", Map.of("teacherQuota", 1, "studentQuota", 1));
        Cookie school = cookie(login(principal.get("phone").asText()));
        Cookie otherSchool = cookie(login(other.get("phone").asText()));
        assertEquals(403, status(create(school, "admin", Map.of())));
        assertEquals(
                403,
                status(create(school, "teacher", Map.of("parentId", other.get("id").asLong()))));
        var teacher = created(school, "teacher", Map.of("studentQuota", 2));
        assertEquals(409, status(create(school, "teacher", Map.of("studentQuota", 2))));
        var teacher2 = created(school, "teacher", Map.of("studentQuota", 1));
        assertEquals(409, status(create(school, "teacher", Map.of())));
        var outsider = created(otherSchool, "teacher", Map.of("studentQuota", 1));
        Cookie teacherCookie = cookie(login(teacher.get("phone").asText()));
        Cookie teacher2Cookie = cookie(login(teacher2.get("phone").asText()));
        assertEquals(403, status(create(teacherCookie, "teacher", Map.of())));
        var executor = Executors.newFixedThreadPool(3);
        List<MvcResult> results;
        try {
            var futures =
                    java.util.stream.IntStream.range(0, 3)
                            .mapToObj(
                                    index ->
                                            CompletableFuture.supplyAsync(
                                                    () -> {
                                                        try {
                                                            return create(
                                                                    teacherCookie,
                                                                    "learner",
                                                                    Map.of());
                                                        } catch (Exception error) {
                                                            throw new java.util.concurrent
                                                                    .CompletionException(error);
                                                        }
                                                    },
                                                    executor))
                            .toList();
            results = futures.stream().map(CompletableFuture::join).toList();
        } finally {
            executor.shutdownNow();
        }
        assertEquals(List.of(201, 201, 409), results.stream().map(this::status).sorted().toList());
        var student =
                json(results.stream()
                                .filter(result -> status(result) == 201)
                                .findFirst()
                                .orElseThrow())
                        .get("user");
        Cookie studentCookie = cookie(login(student.get("phone").asText()));
        assertEquals(200, status(request("GET", "/learning/access", studentCookie, null)));
        assertEquals(2, json(request("GET", "/users", teacherCookie, null)).get("users").size());
        assertEquals(0, json(request("GET", "/users", teacher2Cookie, null)).get("users").size());
        var schoolUsers = json(request("GET", "/users", school, null)).get("users");
        assertEquals(4, schoolUsers.size());
        for (JsonNode user : schoolUsers)
            assertNotEquals(outsider.get("id").asLong(), user.get("id").asLong());
        assertEquals(
                403,
                status(
                        request(
                                "PATCH",
                                "/users/" + student.get("id"),
                                teacher2Cookie,
                                Map.of("name", "越权"))));
        assertEquals(
                403,
                status(
                        request(
                                "PATCH",
                                "/users/" + outsider.get("id"),
                                school,
                                Map.of("name", "越权"))));
        assertEquals(
                400,
                status(
                        request(
                                "PATCH",
                                "/users/" + student.get("id"),
                                teacherCookie,
                                Map.of("role", "admin"))));
        assertEquals(
                409,
                status(
                        request(
                                "PATCH",
                                "/users/" + teacher.get("id"),
                                school,
                                Map.of("studentQuota", 1))));
        assertEquals(
                409,
                status(
                        request(
                                "PATCH",
                                "/users/" + principal.get("id"),
                                admin,
                                Map.of("teacherQuota", 1))));
        assertEquals(
                409,
                status(
                        request(
                                "PATCH",
                                "/users/" + principal.get("id"),
                                admin,
                                Map.of("studentQuota", 2))));
        assertEquals(
                400,
                status(
                        request(
                                "PATCH",
                                "/users/" + teacher.get("id"),
                                school,
                                Map.of("studentQuota", 1.5))));
        assertEquals(
                200,
                status(
                        request(
                                "PATCH",
                                "/users/" + teacher.get("id"),
                                school,
                                Map.of("active", false))));
        assertEquals(401, status(request("GET", "/auth/me", studentCookie, null)));
        assertEquals(403, status(login(student.get("phone").asText())));
    }

    @Test
    void governmentOnlySeesAggregatesAndCannotManageUsers() throws Exception {
        var government = created(admin, "government", Map.of());
        Cookie cookie = cookie(login(government.get("phone").asText()));
        var stats = request("GET", "/government/stats", cookie, null);
        assertEquals(200, status(stats));
        assertEquals(6, json(stats).get("roles").size());
        assertFalse(json(stats).has("users"));
        assertFalse(json(stats).has("phone"));
        assertEquals(403, status(request("GET", "/users", cookie, null)));
        assertEquals(403, status(create(cookie, "ordinary", Map.of())));
        assertEquals(403, status(request("GET", "/learning/access", cookie, null)));
        var principal = created(admin, "principal", Map.of());
        assertEquals(
                403,
                status(
                        request(
                                "GET",
                                "/government/stats",
                                cookie(login(principal.get("phone").asText())),
                                null)));
    }

    @Test
    void confirmedPurchaseEnablesLearningAndReferencesAreUnique() throws Exception {
        var ordinary = created(admin, "ordinary", Map.of());
        Cookie session = cookie(login(ordinary.get("phone").asText()));
        assertEquals(403, status(request("GET", "/learning/access", session, null)));
        String path = "/users/" + ordinary.get("id") + "/purchase";
        assertEquals(403, status(request("POST", path, session, Map.of("reference", "ORDER-1"))));
        assertEquals(200, status(request("POST", path, admin, Map.of("reference", "ORDER-1"))));
        assertEquals(200, status(request("GET", "/learning/access", session, null)));
        assertEquals(409, status(request("POST", path, admin, Map.of("reference", "ORDER-1"))));
        assertEquals(
                1,
                json(request("GET", "/government/stats", admin, null))
                        .get("confirmedPurchases")
                        .asInt());
    }

    @Test
    void passwordChangesRevokeSessionsAndCrossSiteWritesAreBlocked() throws Exception {
        Cookie second = cookie(login("13800000000"));
        var changed =
                request(
                        "POST",
                        "/auth/password",
                        admin,
                        Map.of("oldPassword", PASSWORD, "newPassword", "A-new-password-123"));
        assertEquals(200, status(changed));
        assertEquals(401, status(request("GET", "/auth/me", second, null)));
        Cookie updated = cookie(changed);
        assertEquals(200, status(request("GET", "/auth/me", updated, null)));
        assertEquals(401, status(login("13800000000")));
        var evil =
                mvc.perform(
                                MockMvcRequestBuilders.post("/api/auth/logout")
                                        .cookie(updated)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{}")
                                        .header("Origin", "https://evil.test"))
                        .andReturn();
        assertEquals(403, status(evil));
        assertEquals(
                401,
                status(
                        request(
                                "GET",
                                "/users",
                                new Cookie("zhiqi_session", "a".repeat(64)),
                                null)));
        assertEquals(
                400,
                status(request("PATCH", "/users/" + adminId, updated, Map.of("active", false))));
        for (int i = 0; i < 10; i++) assertEquals(401, status(login("13999999999")));
        assertEquals(429, status(login("13999999999")));
    }

    @Test
    void failedSmsDeliveryReleasesReservationAndWebhookDoesNotExposeCodes() {
        var config =
                new PlatformProperties(
                        false,
                        "",
                        null,
                        new PlatformProperties.Sms(
                                "webhook",
                                "gateway-test-secret",
                                "https://sms.example.test/send",
                                "token"));
        var lastCode = new java.util.concurrent.atomic.AtomicReference<String>();
        var success = new java.util.concurrent.atomic.AtomicBoolean();
        var gateway =
                new SmsGateway(config, mapper) {
                    @Override
                    public void send(String phone, String code) {
                        lastCode.set(code);
                        if (!success.get()) throw new ApiException(502, "失败");
                    }
                };
        var service =
                new SmsService(
                        new com.zhiqi.platform.sms.SmsRepository(jdbc, dialect),
                        users,
                        limits,
                        config,
                        gateway,
                        transactions);
        service.validateConfiguration();
        String phone = "13900000400";
        assertEquals(
                502,
                assertThrows(ApiException.class, () -> service.send(phone, "test-ip")).getStatus());
        assertEquals(0, users.count("SELECT COUNT(*) FROM sms_codes WHERE phone=?", phone));
        success.set(true);
        var result = service.send(phone, "test-ip");
        assertFalse(result.containsKey("devCode"));
        assertEquals(
                TokenCodec.smsHash("gateway-test-secret", phone, lastCode.get()),
                jdbc.queryForObject(
                        "SELECT code_hash FROM sms_codes WHERE phone=?", String.class, phone));
    }

    @Test
    void productionConfigurationRejectsLocalSmsAndWeakSecrets() {
        var console =
                new PlatformProperties(
                        true,
                        "https://education.example",
                        null,
                        new PlatformProperties.Sms("console", "a".repeat(32), "", ""));
        assertThrows(
                ApiException.class,
                () ->
                        new SmsService(null, null, null, console, null, null)
                                .validateConfiguration());
        var weak =
                new PlatformProperties(
                        true,
                        "http://education.example",
                        null,
                        new PlatformProperties.Sms(
                                "webhook", "short", "https://sms.example", "token"));
        assertThrows(
                ApiException.class,
                () -> new SmsService(null, null, null, weak, null, null).validateConfiguration());
    }

    @Test
    void passwordResetBetweenLoginAndSessionCreationCannotIssueOldCredentials() {
        var candidate = auth.login(new Requests.Login("13800000000", PASSWORD), "test-ip");
        auth.changePassword(adminId, new Requests.Password(PASSWORD, "Changed-password-123"));
        var response = new MockHttpServletResponse();
        var error = assertThrows(ApiException.class, () -> auth.issueSession(candidate, response));
        assertEquals(401, error.getStatus());
        assertNull(response.getHeader("Set-Cookie"));
    }

    @Test
    void passwordsRespectUtf8ByteLimitAcrossAllWriteEndpoints() throws Exception {
        var input =
                new java.util.HashMap<String, Object>(
                        Map.of(
                                "phone",
                                "13900000800",
                                "name",
                                "长密码",
                                "role",
                                "ordinary",
                                "password",
                                "a".repeat(73)));
        assertEquals(400, status(request("POST", "/users", admin, input)));
        input.put("password", "中".repeat(25));
        assertEquals(400, status(request("POST", "/users", admin, input)));
        input.put("password", "中".repeat(24));
        var created = request("POST", "/users", admin, input);
        assertEquals(201, status(created));
        assertEquals(
                200,
                status(
                        request(
                                "POST",
                                "/auth/login",
                                null,
                                Map.of(
                                        "phone",
                                        input.get("phone"),
                                        "password",
                                        input.get("password")))));
        assertEquals(
                400,
                status(
                        request(
                                "PATCH",
                                "/users/" + json(created).get("user").get("id"),
                                admin,
                                Map.of("password", "a".repeat(73)))));
        assertEquals(
                400,
                status(
                        request(
                                "POST",
                                "/auth/password",
                                admin,
                                Map.of("oldPassword", PASSWORD, "newPassword", "中".repeat(25)))));
        String phone = "13900000801";
        String code =
                json(request("POST", "/auth/sms", null, Map.of("phone", phone)))
                        .get("devCode")
                        .asText();
        assertEquals(
                400,
                status(
                        request(
                                "POST",
                                "/auth/register",
                                null,
                                Map.of(
                                        "phone",
                                        phone,
                                        "name",
                                        "注册",
                                        "code",
                                        code,
                                        "password",
                                        "a".repeat(73)))));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT attempts FROM sms_codes WHERE phone=?", Integer.class, phone));
        assertEquals(
                201,
                status(
                        request(
                                "POST",
                                "/auth/register",
                                null,
                                Map.of(
                                        "phone",
                                        phone,
                                        "name",
                                        "注册",
                                        "code",
                                        code,
                                        "password",
                                        "a".repeat(72)))));
    }

    @Test
    void paginationSearchAndParentLookupRespectManagementScopes() throws Exception {
        var school = created(admin, "principal", Map.of("teacherQuota", 1, "studentQuota", 30));
        var teacher =
                created(
                        admin,
                        "teacher",
                        Map.of("parentId", school.get("id").asLong(), "studentQuota", 30));
        String hash = passwords.encode(PASSWORD);
        for (int i = 0; i < 25; i++)
            users.insert(
                    "139200" + String.format("%05d", i),
                    "分页学生" + i,
                    hash,
                    Role.LEARNER,
                    teacher.get("id").asLong(),
                    0,
                    0);
        users.insert("13930000000", "其他机构学生", hash, Role.LEARNER, null, 0, 0);
        Cookie teacherSession = cookie(login(teacher.get("phone").asText()));
        var first = json(request("GET", "/users?page=1&pageSize=10", teacherSession, null));
        var second = json(request("GET", "/users?page=2&pageSize=10", teacherSession, null));
        assertEquals(25, first.get("total").asInt());
        assertEquals(10, first.get("users").size());
        var ids = new java.util.HashSet<Long>();
        for (var user : first.get("users")) {
            ids.add(user.get("id").asLong());
            assertEquals(teacher.get("name").asText(), user.get("parentName").asText());
            assertFalse(user.has("passwordHash"));
        }
        for (var user : second.get("users")) assertFalse(ids.contains(user.get("id").asLong()));
        assertEquals(
                0,
                json(request("GET", "/users?search=其他机构", teacherSession, null))
                        .get("total")
                        .asInt());
        assertEquals(
                25,
                json(request("GET", "/users?search=可学习", teacherSession, null))
                        .get("total")
                        .asInt());
        assertEquals(400, status(request("GET", "/users?pageSize=101", admin, null)));
        assertEquals(400, status(request("GET", "/users?page=wrong", admin, null)));
        assertEquals(
                403, status(request("GET", "/users/parents?role=teacher", teacherSession, null)));
        assertEquals(1, json(request("GET", "/users/parents?role=teacher", admin, null)).size());
        assertEquals(
                200,
                status(
                        request(
                                "PATCH",
                                "/users/" + school.get("id"),
                                admin,
                                Map.of("active", false))));
        assertEquals(0, json(request("GET", "/users/parents?role=teacher", admin, null)).size());
    }

    @Test
    void typedPatchRejectsNullUnknownAndCoercedValuesWithoutChangingAccount() throws Exception {
        var school = created(admin, "principal", Map.of("teacherQuota", 2));
        String path = "/users/" + school.get("id");
        assertEquals(400, status(request("PATCH", path, admin, Map.of("unexpected", true))));
        assertEquals(400, status(request("PATCH", path, admin, Map.of("teacherQuota", "3"))));
        assertEquals(400, status(request("PATCH", path, admin, Map.of("active", "false"))));
        var nullInput = new java.util.HashMap<String, Object>();
        nullInput.put("active", null);
        assertEquals(400, status(request("PATCH", path, admin, nullInput)));
        assertEquals(2, users.find(school.get("id").asLong()).teacherQuota());
        assertTrue(users.find(school.get("id").asLong()).active());
    }

    @Test
    void differentInstitutionsAndRateKeysDoNotWaitForAnotherInstitutionLock() throws Exception {
        var first = created(admin, "principal", Map.of("teacherQuota", 1));
        var second = created(admin, "principal", Map.of("teacherQuota", 1));
        Cookie school = cookie(login(second.get("phone").asText()));
        var held = new java.util.concurrent.CountDownLatch(1);
        var release = new java.util.concurrent.CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(3);
        var holding =
                CompletableFuture.runAsync(
                        () ->
                                transactions.executeWithoutResult(
                                        tx -> {
                                            users.lockHierarchies(first.get("id").asLong());
                                            held.countDown();
                                            try {
                                                assertTrue(
                                                        release.await(
                                                                10,
                                                                java.util.concurrent.TimeUnit
                                                                        .SECONDS));
                                            } catch (InterruptedException error) {
                                                throw new RuntimeException(error);
                                            }
                                        }),
                        executor);
        try {
            assertTrue(held.await(3, java.util.concurrent.TimeUnit.SECONDS));
            var creation =
                    CompletableFuture.supplyAsync(
                            () -> {
                                try {
                                    return create(school, "teacher", Map.of());
                                } catch (Exception error) {
                                    throw new RuntimeException(error);
                                }
                            },
                            executor);
            assertEquals(201, status(creation.get(3, java.util.concurrent.TimeUnit.SECONDS)));
            CompletableFuture.runAsync(() -> limits.consume("independent-key", 3, 60000), executor)
                    .get(3, java.util.concurrent.TimeUnit.SECONDS);
        } finally {
            release.countDown();
            holding.get(3, java.util.concurrent.TimeUnit.SECONDS);
            executor.shutdownNow();
        }
        var concurrent = Executors.newFixedThreadPool(4);
        try {
            var results =
                    java.util.stream.IntStream.range(0, 4)
                            .mapToObj(
                                    i ->
                                            CompletableFuture.supplyAsync(
                                                    () -> {
                                                        try {
                                                            limits.consume(
                                                                    "parallel-key", 2, 60000);
                                                            return 200;
                                                        } catch (ApiException error) {
                                                            return error.getStatus();
                                                        }
                                                    },
                                                    concurrent))
                            .toList()
                            .stream()
                            .map(CompletableFuture::join)
                            .sorted()
                            .toList();
            assertEquals(List.of(200, 200, 429, 429), results);
        } finally {
            concurrent.shutdownNow();
        }
    }
}
