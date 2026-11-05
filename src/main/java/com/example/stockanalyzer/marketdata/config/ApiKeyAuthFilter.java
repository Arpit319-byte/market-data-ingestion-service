package com.example.stockanalyzer.marketdata.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    static final String HEADER = "X-API-Key";

    private final String apiKey;

    public ApiKeyAuthFilter(@Value("${app.security.api-key:}") String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!isPublic(request) && matches(presentedKey(request))) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "api-client",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_API")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String presentedKey(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && !header.isBlank()) {
            return header;
        }
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7).trim();
        }
        if (request.getServletPath().startsWith("/ws/")) {
            return request.getParameter("api_key");
        }
        return null;
    }

    private boolean matches(String presented) {
        if (apiKey.isBlank() || presented == null) {
            return false;
        }
        return MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8),
                presented.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isPublic(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path.equals("/error") || path.equals("/actuator/info") || path.startsWith("/actuator/health")) {
            return true;
        }
        return "GET".equalsIgnoreCase(request.getMethod()) && path.matches("/api/v1/auth/\\d+/callback");
    }
}
