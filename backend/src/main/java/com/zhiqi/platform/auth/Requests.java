package com.zhiqi.platform.auth;

import com.zhiqi.platform.common.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class Requests {
    private Requests() {}

    public record Phone(
            @NotBlank(message = "请输入手机号")
                    @Pattern(regexp = "1[3-9]\\d{9}", message = "请输入有效的中国大陆手机号")
                    String phone) {}

    public record Login(
            @NotBlank(message = "请输入手机号")
                    @Pattern(regexp = "1[3-9]\\d{9}", message = "请输入有效的中国大陆手机号")
                    String phone,
            @NotBlank(message = "请输入密码") @ValidPassword String password) {}

    public record Register(
            @NotBlank(message = "请输入手机号")
                    @Pattern(regexp = "1[3-9]\\d{9}", message = "请输入有效的中国大陆手机号")
                    String phone,
            @NotBlank(message = "请输入姓名") @Size(max = 40, message = "姓名不超过 40 个字符") String name,
            @NotBlank(message = "请输入密码") @ValidPassword String password,
            @NotBlank(message = "请输入验证码") @Pattern(regexp = "\\d{6}", message = "请输入六位验证码")
                    String code,
            String role) {}

    public record Password(
            @NotBlank(message = "请输入原密码") @ValidPassword String oldPassword,
            @NotBlank(message = "请输入新密码") @ValidPassword String newPassword) {}

    public record CreateUser(
            @NotBlank(message = "请输入手机号")
                    @Pattern(regexp = "1[3-9]\\d{9}", message = "请输入有效的中国大陆手机号")
                    String phone,
            @NotBlank(message = "请输入姓名") @Size(max = 40, message = "姓名不超过 40 个字符") String name,
            @NotBlank(message = "请输入密码") @ValidPassword String password,
            @NotBlank(message = "请选择角色") String role,
            Long parentId,
            Integer teacherQuota,
            Integer studentQuota) {}

    public record Purchase(
            @NotBlank(message = "请输入实际购买凭证编号")
                    @Size(min = 3, max = 100, message = "购买凭证需为 3 至 100 个字符")
                    String reference) {}
}
