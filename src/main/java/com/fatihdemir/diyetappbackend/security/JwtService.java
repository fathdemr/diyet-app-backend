package com.fatihdemir.diyetappbackend.security;

import com.fatihdemir.diyetappbackend.entity.User;
import com.fatihdemir.diyetappbackend.service.RedisTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final KeyPair rsaKeyPair;
    private final JwtProperties jwtProperties;
    private final RedisTokenService redisTokenService;

    // ── Access Token ──────────────────────────────────────────────────────────

    public String generateAccessToken(User user, String fullName) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())           // jti
                .subject(user.getId().toString())           // sub
                .claims(buildClaims(user, fullName))
                .issuedAt(now)                              // iat
                .notBefore(now)                             // nbf
                .expiration(expiry)                         // exp
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    public String generateRefreshToken(User user) {
        redisTokenService.revokeAllRefreshTokens(user.getId());

        String token = UUID.randomUUID().toString();
        redisTokenService.saveRefreshToken(token, user.getId());
        return token;
    }


    // ── Validation ────────────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return !redisTokenService.isJtiBlacklisted(claims.getId());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Geçersiz token: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractClaims(String token) {
        return parseClaims(token);
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    // ── Revocation ────────────────────────────────────────────────────────────

    public void revokeAccessToken(String token) {
        Claims claims  = parseClaims(token);
        long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
        redisTokenService.blacklistJti(claims.getId(), remaining);
    }

    public void revokeAllUserTokens(User user, String currentAccessToken) {
        revokeAccessToken(currentAccessToken);
        redisTokenService.revokeAllRefreshTokens(user.getId());
    }

    public void revokeAllUserTokens(UUID userId, String currentAccessToken) {
        revokeAccessToken(currentAccessToken);
        redisTokenService.revokeAllRefreshTokens(userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(rsaKeyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Map<String, Object> buildClaims(User user, String fullName) {
        return Map.of(
                "email",    user.getEmail(),
                "fullName", fullName,
                "role",     user.getRole().name()
        );
    }
}