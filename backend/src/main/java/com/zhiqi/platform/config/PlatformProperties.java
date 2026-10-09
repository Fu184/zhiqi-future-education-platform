package com.zhiqi.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record PlatformProperties(
        boolean production, String publicOrigin, Bootstrap bootstrap, Sms sms) {
    public record Bootstrap(boolean enabled, String phone, String password, String name) {}

    public record Sms(String mode, String secret, String webhookUrl, String webhookToken) {}
}
