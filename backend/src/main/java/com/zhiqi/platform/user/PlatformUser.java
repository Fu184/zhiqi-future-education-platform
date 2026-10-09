package com.zhiqi.platform.user;

import java.time.LocalDateTime;

public record PlatformUser(
        long id,
        String phone,
        String name,
        String passwordHash,
        Role role,
        Long parentId,
        int teacherQuota,
        int studentQuota,
        boolean active,
        LocalDateTime createdAt) {}
