package com.zhiqi.platform.user;

import com.zhiqi.platform.common.ApiException;

import java.util.Arrays;

public enum Role {
    ORDINARY("ordinary", "普通用户"),
    LEARNER("learner", "可学习用户"),
    PRINCIPAL("principal", "校长"),
    TEACHER("teacher", "老师"),
    ADMIN("admin", "最大管理员"),
    GOVERNMENT("government", "政府账号");

    private final String code;
    private final String label;

    Role(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public boolean canLearn() {
        return this != ORDINARY && this != GOVERNMENT;
    }

    public boolean canManage() {
        return this == ADMIN || this == PRINCIPAL || this == TEACHER;
    }

    public boolean isStudent() {
        return this == ORDINARY || this == LEARNER;
    }

    public static Role parse(String code) {
        return Arrays.stream(values())
                .filter(role -> role.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new ApiException(400, "无效的角色"));
    }
}
