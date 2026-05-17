package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.DietitianResponse;
import com.fatihdemir.diyetappbackend.dto.DietitianUpdateRequest;
import com.fatihdemir.diyetappbackend.service.DietitianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exapi/dietitians")
@RequiredArgsConstructor
public class SecureDietitianController {

    private final DietitianService dietitianService;

    @PreAuthorize("hasRole('DIETITIAN')")
    @PatchMapping("/me")
    public ResponseEntity<DietitianResponse> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody DietitianUpdateRequest request) {
        return ResponseEntity.ok(dietitianService.updateProfile(userId, request));
    }
}