package com.zhiqi.platform.user;

import com.zhiqi.platform.auth.Requests;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    UsersPage list(
            @AuthenticationPrincipal PlatformUser actor,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "") String search) {
        return users.list(actor.id(), page, pageSize, search);
    }

    @GetMapping("/parents")
    java.util.List<ParentOption> parents(
            @AuthenticationPrincipal PlatformUser actor,
            @RequestParam String role,
            @RequestParam(defaultValue = "") String search) {
        return users.parents(actor.id(), role, search);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, UserView> create(
            @AuthenticationPrincipal PlatformUser actor,
            @Valid @RequestBody Requests.CreateUser input) {
        return Map.of("user", users.create(actor.id(), input));
    }

    @PatchMapping("/{id}")
    Map<String, UserView> update(
            @AuthenticationPrincipal PlatformUser actor,
            @PathVariable long id,
            @RequestBody UserUpdate input) {
        return Map.of("user", users.update(actor.id(), id, input));
    }

    @PostMapping("/{id}/purchase")
    Map<String, UserView> purchase(
            @AuthenticationPrincipal PlatformUser actor,
            @PathVariable long id,
            @Valid @RequestBody Requests.Purchase input) {
        return Map.of("user", users.confirmPurchase(actor.id(), id, input.reference()));
    }
}
