package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.DietitianResponse;
import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.service.DietitianService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dietitians")
@RequiredArgsConstructor
public class DietitianController {

    private final DietitianService dietitianService;

    @GetMapping
    public ResponseEntity<PageResponse<DietitianResponse>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(dietitianService.getDietitians(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DietitianResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(dietitianService.getDietitianById(id));
    }
}