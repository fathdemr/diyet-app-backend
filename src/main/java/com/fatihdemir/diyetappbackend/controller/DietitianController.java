package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianProfileUpdateRequest;
import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianProfileUpdateResponse;
import com.fatihdemir.diyetappbackend.service.DietitianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exapi/dietitian")
public class DietitianController extends BaseController {

    private final DietitianService dietitianService;

    @PutMapping("/update")
    public ResponseEntity<DietitianProfileUpdateResponse> updateProfile(
            @RequestBody DietitianProfileUpdateRequest request) {
        return ResponseEntity.ok(dietitianService.updateProfile(getCurrentUserId(), request));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteProfile() {
        dietitianService.deleteProfile(getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}