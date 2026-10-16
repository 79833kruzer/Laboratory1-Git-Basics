package com.nazarkoldun.motionstudio.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@Order(2)
public class SessionFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "MS_SID";
    public static final String REQUEST_ATTRIBUTE = "studioSession";

    private final SessionManager sessionManager;

    public SessionFilter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String incomingId = readCookie(request);
        StudioSession session = sessionManager.resolve(incomingId);

        if (!session.getId().equals(incomingId)) {
            Cookie cookie = new Cookie(COOKIE_NAME, session.getId());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);
        }

        request.setAttribute(REQUEST_ATTRIBUTE, session);
        chain.doFilter(request, response);
    }

    private static String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
