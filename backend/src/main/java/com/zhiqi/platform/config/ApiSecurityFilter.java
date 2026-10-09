package com.zhiqi.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiqi.platform.auth.AuthService;
import com.zhiqi.platform.common.Checks;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

final class ApiSecurityFilter extends OncePerRequestFilter {
    private final AuthService auth;
    private final PlatformProperties properties;
    private final ObjectMapper mapper;

    ApiSecurityFilter(AuthService auth, PlatformProperties properties, ObjectMapper mapper) {
        this.auth = auth;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (request.getRequestURI().startsWith("/api/")
                && !List.of("GET", "HEAD").contains(request.getMethod())) {
            String contentType = request.getContentType();
            if (contentType == null
                    || !contentType
                            .toLowerCase(java.util.Locale.ROOT)
                            .startsWith("application/json")) {
                reject(response, 415, "请使用 application/json");
                return;
            }
            if (request.getContentLengthLong() > 16384) {
                reject(response, 413, "请求内容过大");
                return;
            }
            String origin = request.getHeader("Origin");
            if (origin != null) {
                String sameHost = request.getScheme() + "://" + request.getHeader("Host");
                boolean allowed =
                        Checks.hasText(properties.publicOrigin())
                                ? origin.equals(properties.publicOrigin())
                                : origin.equals(sameHost)
                                        || origin.equals("http://localhost:5173")
                                        || origin.equals("http://127.0.0.1:5173");
                if (!allowed) {
                    reject(response, 403, "请求来源不受信任");
                    return;
                }
            }
            if ("cross-site".equals(request.getHeader("Sec-Fetch-Site"))) {
                reject(response, 403, "不允许跨站写入");
                return;
            }
        }
        var user = auth.authenticate(request);
        if (user != null) {
            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}
