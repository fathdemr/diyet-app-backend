package com.fatihdemir.diyetappbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BruteForceProtectionService {

    private static final String ATTEMPTS_PREFIX = "brute:attempts:";
    private static final String BLOCKED_PREFIX  = "brute:blocked:";

    private final StringRedisTemplate redisTemplate;

    @Value("${security.rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.rate-limit.block-duration-minutes:30}")
    private long blockDurationMinutes;

    @Value("${security.rate-limit.window-minutes:15}")
    private long windowMinutes;

    public boolean isBlocked(String ip) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLOCKED_PREFIX + ip));
    }

    public void recordFailedAttempt(String ip) {
        String attemptsKey = ATTEMPTS_PREFIX + ip;

        Long count = redisTemplate.opsForValue().increment(attemptsKey);
        if (count == null) return;

        if (count == 1) {
            redisTemplate.expire(attemptsKey, Duration.ofMinutes(windowMinutes));
        }

        if (count >= maxAttempts) {
            redisTemplate.opsForValue().set(BLOCKED_PREFIX + ip, "1", Duration.ofMinutes(blockDurationMinutes));
            redisTemplate.delete(attemptsKey);
            log.warn("Brute-force koruması: IP bloklandı [{}]", ip);
        } else {
            log.warn("Başarısız giriş denemesi: IP={} deneme={}/{}", ip, count, maxAttempts);
        }
    }

    public void resetAttempts(String ip) {
        redisTemplate.delete(ATTEMPTS_PREFIX + ip);
    }

    public long getRemainingAttempts(String ip) {
        String value = redisTemplate.opsForValue().get(ATTEMPTS_PREFIX + ip);
        if (value == null) return maxAttempts;
        return Math.max(0, maxAttempts - Long.parseLong(value));
    }

    public long getBlockRemainingSeconds(String ip) {
        Long ttl = redisTemplate.getExpire(BLOCKED_PREFIX + ip, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}