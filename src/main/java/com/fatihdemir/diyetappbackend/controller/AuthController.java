package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.auth.AuthResponse;
import com.fatihdemir.diyetappbackend.dto.auth.OAuthRequest;
import com.fatihdemir.diyetappbackend.entity.Role;
import com.fatihdemir.diyetappbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/login/dietetian")
    public ResponseEntity<AuthResponse> OAuthLogin(@Valid @RequestBody OAuthRequest request) {
        return ResponseEntity.ok(authService.oauthLogin(request, Role.DIETITIAN));
    }

    @PostMapping("/api/auth/login/client")
    public ResponseEntity<AuthResponse> clientOAuthLogin(@Valid @RequestBody OAuthRequest request) {
        return ResponseEntity.ok(authService.oauthLogin(request, Role.CLIENT));
    }

}
