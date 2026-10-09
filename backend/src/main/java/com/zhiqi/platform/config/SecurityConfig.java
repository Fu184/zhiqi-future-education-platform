package com.zhiqi.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiqi.platform.auth.AuthService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    SecurityFilterChain apiSecurity(
            HttpSecurity http, AuthService auth, PlatformProperties properties, ObjectMapper mapper)
            throws Exception {
        // Cookie 身份配合 SameSite、JSON 请求和 Origin 校验；不创建 Servlet HttpSession。
        http.csrf(csrf -> csrf.disable())
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .requestCache(cache -> cache.disable())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        routes ->
                                routes.requestMatchers(HttpMethod.GET, "/api/health")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/auth/sms",
                                                "/api/auth/register",
                                                "/api/auth/login",
                                                "/api/auth/logout")
                                        .permitAll()
                                        .requestMatchers("/api/users", "/api/users/**")
                                        .hasAnyRole("ADMIN", "PRINCIPAL", "TEACHER")
                                        .requestMatchers("/api/government/**")
                                        .hasAnyRole("ADMIN", "GOVERNMENT")
                                        .requestMatchers("/api/learning/**")
                                        .hasAnyRole("ADMIN", "PRINCIPAL", "TEACHER", "LEARNER")
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        errors ->
                                errors.authenticationEntryPoint(
                                                (request, response, error) -> {
                                                    response.setStatus(401);
                                                    response.setContentType(
                                                            "application/json;charset=UTF-8");
                                                    mapper.writeValue(
                                                            response.getWriter(),
                                                            Map.of("message", "请先登录"));
                                                })
                                        .accessDeniedHandler(
                                                (request, response, error) -> {
                                                    response.setStatus(403);
                                                    response.setContentType(
                                                            "application/json;charset=UTF-8");
                                                    mapper.writeValue(
                                                            response.getWriter(),
                                                            Map.of("message", "无权访问该接口"));
                                                }))
                .addFilterBefore(
                        new ApiSecurityFilter(auth, properties, mapper),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
