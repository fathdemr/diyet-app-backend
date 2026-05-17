package com.fatihdemir.diyetappbackend.dto.dietitian;

import com.fatihdemir.diyetappbackend.entity.DietitianProfile;

import java.time.LocalDateTime;
import java.util.UUID;

public record DietitianResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        String bio,
        String specialization,
        Integer experienceYear,
        String city,
        LocalDateTime createdAt
) {
    public static DietitianResponse from(DietitianProfile d) {
        return new DietitianResponse(
                d.getUserId(),
                d.getFirstName(),
                d.getLastName(),
                d.getFullName(),
                d.getBio(),
                d.getSpecialization(),
                d.getExperienceYear(),
                d.getCity(),
                d.getCreatedAt()
        );
    }
}