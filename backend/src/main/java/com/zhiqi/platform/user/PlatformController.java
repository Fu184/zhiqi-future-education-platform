package com.zhiqi.platform.user;

import static com.zhiqi.platform.common.ApiException.check;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PlatformController {
    private final UserService users;

    public PlatformController(UserService users) {
        this.users = users;
    }

    @GetMapping("/api/health")
    Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/api/government/stats")
    Map<String, Object> statistics(@AuthenticationPrincipal PlatformUser actor) {
        return users.statistics(actor.id());
    }

    @GetMapping("/api/learning/access")
    Map<String, Boolean> learning(@AuthenticationPrincipal PlatformUser actor) {
        check(users.current(actor.id()).role().canLearn(), 403, "请先购买学习资料或联系老师开通学习账号");
        return Map.of("allowed", true);
    }
}
