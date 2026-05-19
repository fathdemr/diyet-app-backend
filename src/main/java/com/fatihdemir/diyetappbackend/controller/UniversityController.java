package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.dto.university.UniversityResponse;
import com.fatihdemir.diyetappbackend.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    @GetMapping("/api/universities")
    public ResponseEntity<PageResponse<UniversityResponse>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(universityService.getUniversities(pageable));
    }
}
