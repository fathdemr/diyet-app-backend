package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exapi/auth")
@RequiredArgsConstructor
public class SecureAuthController {

    private final AuthService authService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(authorization);
        return ResponseEntity.noContent().build();
    }
}