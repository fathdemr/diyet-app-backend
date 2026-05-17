package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.DietitianResponse;
import com.fatihdemir.diyetappbackend.dto.DietitianUpdateRequest;
import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.exception.AuthException;
import com.fatihdemir.diyetappbackend.repository.DietitianProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DietitianService {

    private final DietitianProfileRepository dietitianProfileRepository;

    @Transactional(readOnly = true)
    public PageResponse<DietitianResponse> getDietitians(Pageable pageable) {
        return PageResponse.from(
                dietitianProfileRepository.findAll(pageable).map(DietitianResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public DietitianResponse getDietitianById(UUID userId) {
        return dietitianProfileRepository.findByUserId(userId)
                .map(DietitianResponse::from)
                .orElseThrow(() -> new AuthException("Diyetisyen bulunamadı", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public DietitianResponse updateProfile(String principalId, DietitianUpdateRequest request) {
        var profile = dietitianProfileRepository.findByUserId(UUID.fromString(principalId))
                .orElseThrow(() -> new AuthException("Diyetisyen bulunamadı", HttpStatus.NOT_FOUND));

        if (request.firstName() != null)      profile.setFirstName(request.firstName());
        if (request.lastName() != null)       profile.setLastName(request.lastName());
        if (request.bio() != null)            profile.setBio(request.bio());
        if (request.specialization() != null) profile.setSpecialization(request.specialization());
        if (request.experienceYear() != null) profile.setExperienceYear(request.experienceYear());
        if (request.city() != null)           profile.setCity(request.city());
        if (request.birthDay() != null)       profile.setBirthDay(request.birthDay());

        if (request.firstName() != null || request.lastName() != null) {
            String first = profile.getFirstName() != null ? profile.getFirstName() : "";
            String last  = profile.getLastName()  != null ? profile.getLastName()  : "";
            profile.setFullName((first + " " + last).trim());
        }

        return DietitianResponse.from(dietitianProfileRepository.save(profile));
    }
}