package com.fatihdemir.diyetappbackend.service;

import java.util.Optional;
import java.util.UUID;

public interface RedisTokenService {

    void saveRefreshToken(String token, UUID userId);

    Optional<UUID> getUserIdByRefreshToken(String token);

    void deleteRefreshToken(String token);

    void revokeAllRefreshTokens(UUID userId);

    void blacklistJti(String jti, long remainingTtlMs);

    boolean isJtiBlacklisted(String jti);

}
