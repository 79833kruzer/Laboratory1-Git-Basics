package com.nazarkoldun.motionstudio.middleware;

import com.nazarkoldun.motionstudio.session.SessionFilter;
import com.nazarkoldun.motionstudio.session.SessionManager;
import com.nazarkoldun.motionstudio.session.StudioSession;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long start = System.nanoTime();
        MDC.put("rid", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            long millis = (System.nanoTime() - start) / 1_000_000;
            String path = request.getRequestURI()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            String sid = request.getAttribute(SessionFilter.REQUEST_ATTRIBUTE) instanceof StudioSession s
                    ? SessionManager.shortId(s.getId()) : "-";

            if (request.getRequestURI().startsWith("/api/")) {
                log.info("{} {} -> {} ({} ms) sid={}", request.getMethod(), path, response.getStatus(), millis, sid);
            } else {
                log.debug("{} {} -> {} ({} ms)", request.getMethod(), path, response.getStatus(), millis);
            }
            MDC.remove("rid");
        }
    }
}
