package com.example.stockanalyzer.marketdata.logging;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private static final Pattern SECRET_QUERY = Pattern.compile("(?i)(api_key|access_token|request_token|token)=[^&]*");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = requestId(request.getHeader(REQUEST_ID_HEADER));
        long started = System.nanoTime();
        MDC.put(IncidentContext.REQUEST_ID, requestId);
        MDC.put(IncidentContext.OPERATION, "http");
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            if (shouldLog(request.getServletPath(), status)) {
                long millis = (System.nanoTime() - started) / 1_000_000L;
                log.info("{} {}{} -> {} in {} ms",
                        request.getMethod(),
                        request.getServletPath(),
                        safeQuery(request.getQueryString()),
                        status,
                        millis);
            }
            MDC.remove(IncidentContext.REQUEST_ID);
            MDC.remove(IncidentContext.OPERATION);
        }
    }

    private static boolean shouldLog(String path, int status) {
        boolean probe = path.startsWith("/actuator/health") || "/actuator/info".equals(path);
        return !probe || status >= 400;
    }

    private static String requestId(String header) {
        if (header != null && SAFE_ID.matcher(header).matches()) {
            return header;
        }
        return UUID.randomUUID().toString();
    }

    private static String safeQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        return "?" + SECRET_QUERY.matcher(query).replaceAll("$1=***");
    }
}
