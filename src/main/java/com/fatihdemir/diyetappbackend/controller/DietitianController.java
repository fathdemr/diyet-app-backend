package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianResponse;
import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianUpdateRequest;
import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.service.DietitianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DietitianController {

    private final DietitianService dietitianService;

    @GetMapping("/exapi/dietitians")
    public ResponseEntity<PageResponse<DietitianResponse>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(dietitianService.getDietitians(pageable));
    }

    @GetMapping("/exapi/dietitians/{id}")
    public ResponseEntity<DietitianResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(dietitianService.getDietitianById(id));
    }

    @PreAuthorize("hasRole('DIETITIAN')")
    @PatchMapping("/exapi/dietitians/me")
    public ResponseEntity<DietitianResponse> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody DietitianUpdateRequest request) {
        return ResponseEntity.ok(dietitianService.updateProfile(userId, request));
    }
}
