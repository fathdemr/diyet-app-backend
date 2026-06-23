package com.fatihdemir.diyetappbackend.dto.dietitian;

import com.fatihdemir.diyetappbackend.entity.Dietitian;

import java.time.LocalDate;
import java.util.UUID;

public record DietitianProfileUpdateResponse(
        UUID id,
        String firstName,
        String lastName,
        String userName,
        String bio,
        String city,
        LocalDate birthDay,
        Integer experienceYear
) {
    public static DietitianProfileUpdateResponse from(Dietitian dietitian) {
        return new DietitianProfileUpdateResponse(
                dietitian.getId(),
                dietitian.getUser().getFirstName(),
                dietitian.getUser().getLastName(),
                dietitian.getUser().getUserName(),
                dietitian.getBio(),
                dietitian.getCity(),
                dietitian.getBirthDay(),
                dietitian.getExperienceYear()
        );
    }
}