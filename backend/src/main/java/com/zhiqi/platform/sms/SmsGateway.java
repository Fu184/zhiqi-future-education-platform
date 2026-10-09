package com.zhiqi.platform.sms;

import static com.zhiqi.platform.common.ApiException.check;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiqi.platform.common.ApiException;
import com.zhiqi.platform.config.PlatformProperties;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
public class SmsGateway {
    private final PlatformProperties properties;
    private final ObjectMapper mapper;
    private final HttpClient client =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public SmsGateway(PlatformProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
    }

    public void send(String phone, String code) {
        if ("console".equals(properties.sms().mode())) return;
        try {
            var request =
                    HttpRequest.newBuilder(URI.create(properties.sms().webhookUrl()))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + properties.sms().webhookToken())
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            mapper.writeValueAsString(
                                                    Map.of(
                                                            "phone",
                                                            phone,
                                                            "code",
                                                            code,
                                                            "expiresIn",
                                                            300,
                                                            "purpose",
                                                            "register"))))
                            .build();
            int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            check(status >= 200 && status < 300, 502, "短信发送失败，请稍后重试");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new ApiException(502, "短信发送失败，请稍后重试");
        } catch (Exception error) {
            throw new ApiException(502, "短信发送失败，请稍后重试");
        }
    }
}
