package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.auth.AuthResponse;
import com.fatihdemir.diyetappbackend.dto.auth.OAuthRequest;
import com.fatihdemir.diyetappbackend.entity.Role;
import com.fatihdemir.diyetappbackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    // ── Public API (/api/**) ─────────────────────────────────────────────────

    @PostMapping("/api/auth/login/dietitian")
    public AuthResponse dietitianLogin(@Valid @RequestBody OAuthRequest request) {
        return authService.oauthLogin(request, Role.DIETITIAN);
    }

    @PostMapping("/api/auth/login/patient")
    public AuthResponse patientLogin(@Valid @RequestBody OAuthRequest request) {
        return authService.oauthLogin(request, Role.PATIENT);
    }

    // ── Private API (/exapi/**) ──────────────────────────────────────────────

    @PostMapping("/exapi/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(getToken(request));
        return ResponseEntity.noContent().build();
    }

}