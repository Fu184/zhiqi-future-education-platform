package com.zhiqi.platform;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

class PlatformStartupTest {
    @TempDir Path directory;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private ServletWebServerApplicationContext start() {
        String path = directory.resolve("platform").toString().replace('\\', '/');
        return (ServletWebServerApplicationContext)
                new SpringApplicationBuilder(PlatformApplication.class)
                        .logStartupInfo(false)
                        .run(
                                "--server.port=0",
                                "--server.address=127.0.0.1",
                                "--spring.profiles.active=dev",
                                "--spring.config.import=",
                                "--spring.datasource.url=jdbc:h2:file:"
                                        + path
                                        + ";DB_CLOSE_ON_EXIT=FALSE",
                                "--app.bootstrap.enabled=true",
                                "--app.sms.mode=console",
                                "--app.sms.secret=startup-test-secret");
    }

    private String origin(ServletWebServerApplicationContext app) {
        return "http://127.0.0.1:" + app.getWebServer().getPort();
    }

    private HttpResponse<String> post(String origin, String path, String cookie, String body)
            throws Exception {
        var request =
                HttpRequest.newBuilder(URI.create(origin + "/api" + path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body));
        if (cookie != null) request.header("Cookie", cookie);
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void directDevelopmentStartupAndFileDatabaseSurviveRestart() throws Exception {
        long userId;
        try (var app = start()) {
            String origin = origin(app);
            var health =
                    http.send(
                            HttpRequest.newBuilder(URI.create(origin + "/api/health"))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString());
            assertEquals(200, health.statusCode());
            var login =
                    post(
                            origin,
                            "/auth/login",
                            null,
                            "{\"phone\":\"13800000000\",\"password\":\"DevAdmin123!\"}");
            assertEquals(200, login.statusCode(), login.body());
            String cookie = login.headers().firstValue("set-cookie").orElseThrow().split(";", 2)[0];
            var created =
                    post(
                            origin,
                            "/users",
                            cookie,
                            "{\"phone\":\"13900000999\",\"name\":\"持久化用户\",\"password\":\"Persist-password-123\",\"role\":\"ordinary\"}");
            assertEquals(201, created.statusCode(), created.body());
            userId = mapper.readTree(created.body()).get("user").get("id").asLong();
        }
        try (var app = start()) {
            var login =
                    post(
                            origin(app),
                            "/auth/login",
                            null,
                            "{\"phone\":\"13900000999\",\"password\":\"Persist-password-123\"}");
            assertEquals(200, login.statusCode(), login.body());
            assertEquals(userId, mapper.readTree(login.body()).get("user").get("id").asLong());
            assertEquals(
                    200,
                    post(
                                    origin(app),
                                    "/auth/login",
                                    null,
                                    "{\"phone\":\"13800000000\",\"password\":\"DevAdmin123!\"}")
                            .statusCode());
        }
    }
}
