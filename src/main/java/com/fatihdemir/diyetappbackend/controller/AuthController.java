package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.RefreshRequest;
import com.fatihdemir.diyetappbackend.dto.UserResponse;
import com.fatihdemir.diyetappbackend.dto.auth.AuthResponse;
import com.fatihdemir.diyetappbackend.dto.auth.ForgotPasswordRequest;
import com.fatihdemir.diyetappbackend.dto.auth.OAuthRequest;
import com.fatihdemir.diyetappbackend.entity.Role;
import com.fatihdemir.diyetappbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/client/login")
    public ResponseEntity<AuthResponse> clientLogin(@Valid @RequestBody OAuthRequest request) {
        return ResponseEntity.ok(authService.oauthLogin(request, Role.CLIENT));
    }

    @PostMapping("/api/auth/dietitian/login")
    public ResponseEntity<AuthResponse> dietitianLogin(@Valid @RequestBody OAuthRequest request) {
        return ResponseEntity.ok(authService.oauthLogin(request, Role.DIETITIAN));
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/api/auth/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/auth/resend-verification")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody OAuthRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/exapi/auth/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(authorization);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exapi/auth/me")
    public ResponseEntity<UserResponse> getMe(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(authService.getMe(authorization));
    }
}