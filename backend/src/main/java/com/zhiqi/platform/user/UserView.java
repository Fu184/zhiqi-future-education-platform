package com.zhiqi.platform.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

// 对外 DTO 永远不包含 passwordHash；字段名保持现有前端协议。
public record UserView(
        long id,
        String phone,
        String name,
        String role,
        @JsonProperty("parent_id") Long parentId,
        @JsonProperty("teacher_quota") int teacherQuota,
        @JsonProperty("student_quota") int studentQuota,
        boolean active,
        @JsonProperty("created_at") LocalDateTime createdAt,
        String roleLabel,
        boolean canLearn,
        int teacherUsed,
        int studentUsed,
        String parentName) {
    public static UserView of(PlatformUser user, int teachers, int students, String parentName) {
        return new UserView(
                user.id(),
                user.phone(),
                user.name(),
                user.role().code(),
                user.parentId(),
                user.teacherQuota(),
                user.studentQuota(),
                user.active(),
                user.createdAt(),
                user.role().label(),
                user.role().canLearn(),
                teachers,
                students,
                parentName);
    }
}
