package com.fatihdemir.diyetappbackend.service.Impl;

import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianProfileUpdateRequest;
import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianProfileUpdateResponse;
import com.fatihdemir.diyetappbackend.exception.ResourceNotFoundException;
import com.fatihdemir.diyetappbackend.repository.DietitianRepository;
import com.fatihdemir.diyetappbackend.service.DietitianService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DietitianServiceImpl implements DietitianService {

    private final DietitianRepository dietitianRepository;

    @Override
    @Transactional
    public DietitianProfileUpdateResponse updateProfile(UUID userId, DietitianProfileUpdateRequest request) {
        var dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Diyetisyen"));

        var user = dietitian.getUser();

        dietitian.setBio(request.bio());
        dietitian.setCity(request.city());
        dietitian.setBirthDay(request.birthDay());
        dietitian.setExperienceYear(request.experienceYear());

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setFullName(request.firstName() + " " + request.lastName());
        user.setUserName(request.userName());

        dietitianRepository.save(dietitian);

        return DietitianProfileUpdateResponse.from(dietitian);
    }

    @Transactional
    public void deleteProfile(UUID id) {
        var dietitian = dietitianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diyetisyen"));

        dietitianRepository.delete(dietitian);
    }

}