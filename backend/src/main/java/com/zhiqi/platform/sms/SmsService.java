package com.zhiqi.platform.sms;

import static com.zhiqi.platform.common.ApiException.check;

import com.zhiqi.platform.auth.RateLimiter;
import com.zhiqi.platform.auth.TokenCodec;
import com.zhiqi.platform.common.ApiException;
import com.zhiqi.platform.common.Checks;
import com.zhiqi.platform.config.PlatformProperties;
import com.zhiqi.platform.user.UserRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SmsService {
    private final SmsRepository repository;
    private final UserRepository users;
    private final RateLimiter limits;
    private final PlatformProperties properties;
    private final SmsGateway gateway;
    private final TransactionTemplate transactions;
    private final String secret;

    public SmsService(
            SmsRepository repository,
            UserRepository users,
            RateLimiter limits,
            PlatformProperties properties,
            SmsGateway gateway,
            TransactionTemplate transactions) {
        this.repository = repository;
        this.users = users;
        this.limits = limits;
        this.properties = properties;
        this.gateway = gateway;
        this.transactions = transactions;
        this.secret =
                Checks.hasText(properties.sms().secret())
                        ? properties.sms().secret()
                        : TokenCodec.token();
    }

    @PostConstruct
    public void validateConfiguration() {
        var config = properties.sms();
        if (properties.production()) {
            check("webhook".equals(config.mode()), 500, "生产环境必须配置真实短信 webhook");
            check(
                    Checks.hasText(config.secret())
                            && config.secret().length() >= 32
                            && validHttps(properties.publicOrigin()),
                    500,
                    "生产环境需设置至少 32 字符的 SMS_SECRET 和 HTTPS PUBLIC_ORIGIN");
        }
        check(
                "console".equals(config.mode()) || "webhook".equals(config.mode()),
                500,
                "SMS_MODE 必须为 console 或 webhook");
        if ("webhook".equals(config.mode())) {
            check(
                    validHttps(config.webhookUrl()) && Checks.hasText(config.webhookToken()),
                    500,
                    "短信 webhook 需配置 HTTPS 地址与授权令牌");
        }
    }

    private boolean validHttps(String value) {
        try {
            var uri = java.net.URI.create(value);
            return "https".equals(uri.getScheme()) && uri.getHost() != null;
        } catch (Exception error) {
            return false;
        }
    }

    public Map<String, Object> send(String phone, String ip) {
        limits.consume("sms-ip:" + ip, 20, 3600000);
        limits.consume("sms-phone:" + phone, 10, 86400000);
        String code = TokenCodec.code();
        long now = System.currentTimeMillis();
        transactions.executeWithoutResult(
                status -> {
                    users.lockPhone(phone);
                    var previous = repository.find(phone);
                    check(
                            previous == null || now - previous.sentAt() >= 60000,
                            429,
                            "请在 60 秒后重新获取验证码");
                    check(users.byPhone(phone) == null, 409, "手机号已注册");
                    repository.reserve(
                            phone, TokenCodec.smsHash(secret, phone, code), now + 300000, now);
                });
        try {
            gateway.send(phone, code);
        } catch (Exception error) {
            repository.release(phone, now);
            throw new ApiException(502, "短信发送失败，请稍后重试");
        }
        var result = new LinkedHashMap<String, Object>();
        result.put("message", "验证码已发送");
        result.put("retryAfter", 60);
        if (!properties.production() && "console".equals(properties.sms().mode())) {
            result.put("devCode", code);
            result.put("message", "本地测试验证码，未发送真实短信");
        }
        return result;
    }

    // 调用方注册事务持有手机号锁；失败次数提交，成功创建与销毁验证码原子提交。
    public void verify(String phone, String code) {
        var codeState = repository.find(phone);
        check(codeState != null, 400, "验证码过期或尝试次数已用完");
        check(
                codeState.expiresAt() > System.currentTimeMillis() && codeState.attempts() < 5,
                400,
                "验证码过期或尝试次数已用完");
        repository.attempt(phone);
        check(
                TokenCodec.equal(codeState.hash(), TokenCodec.smsHash(secret, phone, code)),
                400,
                "验证码错误");
    }

    public void consume(String phone) {
        repository.consume(phone);
    }
}
