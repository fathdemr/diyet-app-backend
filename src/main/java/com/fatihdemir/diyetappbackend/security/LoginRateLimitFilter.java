package com.fatihdemir.diyetappbackend.security;

import com.fatihdemir.diyetappbackend.service.BruteForceProtectionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PROTECTED_PATHS = List.of(
            "/api/auth/client/login",
            "/api/auth/dietitian/login"
    );

    private final BruteForceProtectionService bruteForceService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        if (!isProtectedPath(request)) {
            chain.doFilter(request, response);
            return;
        }

        String ip = extractClientIp(request);

        if (bruteForceService.isBlocked(ip)) {
            long remainingSeconds = bruteForceService.getBlockRemainingSeconds(ip);
            long remainingMinutes = (remainingSeconds / 60) + 1;
            writeError(response, HttpStatus.TOO_MANY_REQUESTS,
                    "Çok fazla başarısız giriş denemesi. " + remainingMinutes + " dakika sonra tekrar deneyin.");
            return;
        }

        chain.doFilter(request, response);

        // 200 → başarılı giriş, sayacı sıfırla
        // 400 → geçersiz Firebase token → potansiyel brute-force
        // 403 → email doğrulanmamış / yanlış rol → geçerli token, saymıyoruz
        int status = response.getStatus();
        if (status == HttpServletResponse.SC_OK) {
            bruteForceService.resetAttempts(ip);
        } else if (status == HttpServletResponse.SC_BAD_REQUEST) {
            bruteForceService.recordFailedAttempt(ip);

            if (!bruteForceService.isBlocked(ip)) {
                long remaining = bruteForceService.getRemainingAttempts(ip);
                log.info("Kalan giriş hakkı: IP={} remaining={}", ip, remaining);
            }
        }
    }

    private boolean isProtectedPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PROTECTED_PATHS.stream().anyMatch(p -> PATH_MATCHER.match(p, uri));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\":\"" + message + "\",\"timestamp\":\"" + Instant.now() + "\"}"
        );
    }
}