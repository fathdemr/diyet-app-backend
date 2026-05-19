package com.fatihdemir.diyetappbackend.dto.dietitian;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record DietitianUpdateRequest(
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,
        @Size(max = 500) String bio,
        @Min(0) @Max(60) Integer experienceYear,
        @Size(max = 100) String city,
        LocalDate birthDay,
        String university,
        @Size(max = 5) List<Long> tagIds
) {
}