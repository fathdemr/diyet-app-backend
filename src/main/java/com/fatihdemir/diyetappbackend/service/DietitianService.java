package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianProfileUpdateRequest;
import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianProfileUpdateResponse;

import java.util.UUID;

public interface DietitianService {

    DietitianProfileUpdateResponse updateProfile(UUID userId, DietitianProfileUpdateRequest request);

    void deleteProfile(UUID id);
}
