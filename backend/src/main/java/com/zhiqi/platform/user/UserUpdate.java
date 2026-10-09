package com.zhiqi.platform.user;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.zhiqi.platform.common.ApiException;

import java.util.HashSet;
import java.util.Set;

public class UserUpdate {
    private final Set<String> fields = new HashSet<>();

    public Set<String> fields() {
        return Set.copyOf(fields);
    }

    public boolean has(String field) {
        return fields.contains(field);
    }

    @JsonAnySetter
    public void unknown(String name, Object value) {
        throw new ApiException(400, "包含不允许修改的字段: " + name);
    }

    private String name;

    public String name() {
        return name;
    }

    @JsonSetter("name")
    public void setName(String value) {
        fields.add("name");
        name = value;
    }

    private String phone;

    public String phone() {
        return phone;
    }

    @JsonSetter("phone")
    public void setPhone(String value) {
        fields.add("phone");
        phone = value;
    }

    private String password;

    public String password() {
        return password;
    }

    @JsonSetter("password")
    public void setPassword(String value) {
        fields.add("password");
        password = value;
    }

    private String role;

    public String role() {
        return role;
    }

    @JsonSetter("role")
    public void setRole(String value) {
        fields.add("role");
        role = value;
    }

    private Boolean active;

    public Boolean active() {
        return active;
    }

    @JsonSetter("active")
    public void setActive(Boolean value) {
        fields.add("active");
        active = value;
    }

    private Integer teacherQuota;

    public Integer teacherQuota() {
        return teacherQuota;
    }

    @JsonSetter("teacherQuota")
    public void setTeacherQuota(Integer value) {
        fields.add("teacherQuota");
        teacherQuota = value;
    }

    private Integer studentQuota;

    public Integer studentQuota() {
        return studentQuota;
    }

    @JsonSetter("studentQuota")
    public void setStudentQuota(Integer value) {
        fields.add("studentQuota");
        studentQuota = value;
    }
}
