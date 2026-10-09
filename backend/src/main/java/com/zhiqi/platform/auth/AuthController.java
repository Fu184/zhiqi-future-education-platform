package com.zhiqi.platform.auth;

import com.zhiqi.platform.sms.SmsService;
import com.zhiqi.platform.user.PlatformUser;
import com.zhiqi.platform.user.UserRepository;
import com.zhiqi.platform.user.UserView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final SmsService sms;
    private final UserRepository users;
    private final RateLimiter limits;

    public AuthController(
            AuthService auth, SmsService sms, UserRepository users, RateLimiter limits) {
        this.auth = auth;
        this.sms = sms;
        this.users = users;
        this.limits = limits;
    }

    @PostMapping("/sms")
    Map<String, Object> sms(@Valid @RequestBody Requests.Phone input, HttpServletRequest request) {
        return sms.send(input.phone(), request.getRemoteAddr());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UserView> register(
            @Valid @RequestBody Requests.Register input, HttpServletResponse response) {
        var user = auth.register(input);
        auth.issueSession(user, response);
        return Map.of("user", users.publicUser(user));
    }

    @PostMapping("/login")
    Map<String, UserView> login(
            @Valid @RequestBody Requests.Login input,
            HttpServletRequest request,
            HttpServletResponse response) {
        var user = auth.login(input, request.getRemoteAddr());
        auth.issueSession(user, response);
        return Map.of("user", users.publicUser(user));
    }

    @PostMapping("/logout")
    Map<String, String> logout(HttpServletRequest request, HttpServletResponse response) {
        auth.logout(request, response);
        return Map.of("message", "已退出登录");
    }

    @GetMapping("/me")
    Map<String, UserView> me(@AuthenticationPrincipal PlatformUser user) {
        return Map.of("user", users.publicUser(user));
    }

    @PostMapping("/password")
    Map<String, String> password(
            @AuthenticationPrincipal PlatformUser actor,
            @Valid @RequestBody Requests.Password input,
            HttpServletResponse response) {
        limits.consume("password:" + actor.id(), 10, 900000);
        var user = auth.changePassword(actor.id(), input);
        auth.issueSession(user, response);
        return Map.of("message", "密码已修改，其他会话已失效");
    }
}
