package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private static final String RT_PREFIX       = "rt:";
    private static final String RT_USER_PREFIX  = "rt:user:";
    private static final String BL_PREFIX       = "bl:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    // ── Refresh Token ─────────────────────────────────────────────────────────

    public void saveRefreshToken(String token, UUID userId) {
        long ttlMs = jwtProperties.getRefreshTokenExpiration();

        // token → userId
        redisTemplate.opsForValue().set(
                RT_PREFIX + token,
                userId.toString(),
                ttlMs,
                TimeUnit.MILLISECONDS
        );

        // userId → Set<token>  (tüm cihazları revoke edebilmek için)
        String userKey = RT_USER_PREFIX + userId;
        redisTemplate.opsForSet().add(userKey, token);
        redisTemplate.expire(userKey, Duration.ofMillis(ttlMs));
    }

    public Optional<UUID> getUserIdByRefreshToken(String token) {
        String value = redisTemplate.opsForValue().get(RT_PREFIX + token);
        return Optional.ofNullable(value).map(UUID::fromString);
    }

    public void deleteRefreshToken(String token) {
        getUserIdByRefreshToken(token).ifPresent(userId ->
                redisTemplate.opsForSet().remove(RT_USER_PREFIX + userId, token)
        );
        redisTemplate.delete(RT_PREFIX + token);
    }

    /** Kullanıcının tüm refresh token'larını iptal eder (logout all devices). */
    public void revokeAllRefreshTokens(UUID userId) {
        String userKey = RT_USER_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userKey);

        if (tokens != null) {
            tokens.forEach(token -> redisTemplate.delete(RT_PREFIX + token));
        }
        redisTemplate.delete(userKey);
    }

    // ── Access Token Blacklist ────────────────────────────────────────────────

    public void blacklistJti(String jti, long remainingTtlMs) {
        if (remainingTtlMs > 0) {
            redisTemplate.opsForValue().set(
                    BL_PREFIX + jti,
                    "1",
                    remainingTtlMs,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public boolean isJtiBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BL_PREFIX + jti));
    }
}