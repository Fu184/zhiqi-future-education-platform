package com.zhiqi.platform.common;

import static com.zhiqi.platform.common.ApiException.check;

public final class Checks {
    private Checks() {}

    public static String phone(Object value) {
        check(
                value instanceof String && ((String) value).matches("1[3-9]\\d{9}"),
                400,
                "请输入有效的中国大陆手机号");
        return (String) value;
    }

    public static String name(Object value) {
        check(
                value instanceof String
                        && !((String) value).trim().isEmpty()
                        && ((String) value).trim().length() <= 40,
                400,
                "姓名需为 1 至 40 个字符");
        return ((String) value).trim();
    }

    public static String password(Object value) {
        check(
                value instanceof String && validPassword((String) value),
                400,
                "密码至少 8 个字符，UTF-8 编码不超过 72 字节");
        return (String) value;
    }

    public static boolean validPassword(String value) {
        return value != null
                && value.length() >= 8
                && value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72;
    }

    public static int quota(Object value) {
        check(
                value instanceof Integer && (int) value >= 0 && (int) value <= 100000,
                400,
                "名额需为 0 至 100000 的整数");
        return (int) value;
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
