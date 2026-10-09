package com.zhiqi.platform.auth;

import static com.zhiqi.platform.common.ApiException.check;

import com.zhiqi.platform.common.ApiException;
import com.zhiqi.platform.common.Checks;
import com.zhiqi.platform.config.PlatformProperties;
import com.zhiqi.platform.sms.SmsService;
import com.zhiqi.platform.user.PlatformUser;
import com.zhiqi.platform.user.Role;
import com.zhiqi.platform.user.UserRepository;
import com.zhiqi.platform.user.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class AuthService {
    public static final String COOKIE_NAME = "zhiqi_session";
    private final UserRepository users;
    private final UserService accounts;
    private final SessionRepository sessions;
    private final PasswordEncoder passwords;
    private final SmsService sms;
    private final RateLimiter limits;
    private final PlatformProperties properties;
    private final String dummyHash;

    public AuthService(
            UserRepository users,
            UserService accounts,
            SessionRepository sessions,
            PasswordEncoder passwords,
            SmsService sms,
            RateLimiter limits,
            PlatformProperties properties) {
        this.users = users;
        this.accounts = accounts;
        this.sessions = sessions;
        this.passwords = passwords;
        this.sms = sms;
        this.limits = limits;
        this.properties = properties;
        this.dummyHash = passwords.encode(TokenCodec.token());
    }

    public PlatformUser authenticate(HttpServletRequest request) {
        String token = tokenFrom(request);
        if (token == null || !token.matches("[a-f0-9]{64}")) return null;
        Long id = sessions.userId(TokenCodec.hash(token), System.currentTimeMillis());
        if (id == null) return null;
        var user = users.find(id);
        return accounts.enabled(user) ? user : null;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, noRollbackFor = ApiException.class)
    public PlatformUser register(Requests.Register input) {
        check(input.role() == null || "ordinary".equals(input.role()), 403, "公开注册只能创建普通用户");
        String name = Checks.name(input.name());
        String hash = passwords.encode(Checks.password(input.password()));
        users.lockPhone(input.phone());
        sms.verify(input.phone(), input.code());
        check(users.byPhone(input.phone()) == null, 409, "手机号已注册");
        long id = users.insert(input.phone(), name, hash, Role.ORDINARY, null, 0, 0);
        sms.consume(input.phone());
        users.audit(id, "register", id);
        return users.find(id);
    }

    public PlatformUser login(Requests.Login input, String ip) {
        limits.consume("login-ip:" + ip, 100, 900000);
        limits.consume("login-phone:" + input.phone(), 10, 900000);
        var user = users.byPhone(input.phone());
        boolean valid =
                passwords.matches(input.password(), user == null ? dummyHash : user.passwordHash());
        check(valid && user != null && user.active(), 401, "手机号、密码错误或账号已停用");
        check(accounts.enabled(user), 403, "所属机构或老师账号已停用");
        limits.clear("login-phone:" + input.phone());
        return user;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void issueSession(PlatformUser user, HttpServletResponse response) {
        users.lockHierarchies(user.id());
        var current = accounts.current(user.id());
        check(current.passwordHash().equals(user.passwordHash()), 401, "登录凭据已变更，请重新登录");
        String token = TokenCodec.token();
        long now = System.currentTimeMillis();
        sessions.removeExpired(now);
        sessions.insert(TokenCodec.hash(token), user.id(), now + Duration.ofDays(7).toMillis());
        setCookie(response, token, Duration.ofDays(7));
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = tokenFrom(request);
        if (token != null) sessions.delete(TokenCodec.hash(token));
        setCookie(response, "", Duration.ZERO);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlatformUser changePassword(long id, Requests.Password input) {
        var candidate = accounts.current(id);
        check(
                passwords.matches(Checks.password(input.oldPassword()), candidate.passwordHash()),
                400,
                "原密码错误");
        String hash = passwords.encode(Checks.password(input.newPassword()));
        users.lockHierarchies(id);
        var user = accounts.current(id);
        check(user.passwordHash().equals(candidate.passwordHash()), 409, "密码已变更，请重试");
        users.changePassword(id, hash);
        users.revokeSessions(id);
        users.audit(id, "change-password", id);
        return users.find(id);
    }

    private void setCookie(HttpServletResponse response, String value, Duration maxAge) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                ResponseCookie.from(COOKIE_NAME, value)
                        .httpOnly(true)
                        .sameSite("Lax")
                        .path("/")
                        .secure(properties.production())
                        .maxAge(maxAge)
                        .build()
                        .toString());
    }

    private String tokenFrom(HttpServletRequest request) {
        if (request.getCookies() != null)
            for (Cookie cookie : request.getCookies())
                if (COOKIE_NAME.equals(cookie.getName())) return cookie.getValue();
        return null;
    }
}
