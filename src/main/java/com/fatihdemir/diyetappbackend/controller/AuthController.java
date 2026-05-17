package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.AuthResponse;
import com.fatihdemir.diyetappbackend.dto.ForgotPasswordRequest;
import com.fatihdemir.diyetappbackend.dto.OAuthRequest;
import com.fatihdemir.diyetappbackend.dto.RefreshRequest;
import com.fatihdemir.diyetappbackend.entity.Role;
import com.fatihdemir.diyetappbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/client/login")
    public ResponseEntity<AuthResponse> clientLogin(@Valid @RequestBody OAuthRequest request) {
        return ResponseEntity.ok(authService.oauthLogin(request, Role.CLIENT));
    }

    @PostMapping("/dietitian/login")
    public ResponseEntity<AuthResponse> dietitianLogin(@Valid @RequestBody OAuthRequest request) {
        return ResponseEntity.ok(authService.oauthLogin(request, Role.DIETITIAN));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody OAuthRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok().build();
    }
}