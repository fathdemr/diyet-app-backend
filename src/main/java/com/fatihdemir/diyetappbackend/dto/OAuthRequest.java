package com.fatihdemir.diyetappbackend.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthRequest(
        @NotBlank String firebaseToken
) {}