package com.fatihdemir.diyetappbackend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthRequest(
        @NotBlank String firebaseToken
) {}