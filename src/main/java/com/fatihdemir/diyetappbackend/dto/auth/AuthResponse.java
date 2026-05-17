package com.fatihdemir.diyetappbackend.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresInMs) {
        this(accessToken, refreshToken, "Bearer", expiresInMs / 1000);
    }
}