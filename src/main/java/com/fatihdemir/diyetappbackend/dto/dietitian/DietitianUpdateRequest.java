package com.fatihdemir.diyetappbackend.dto.dietitian;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DietitianUpdateRequest(
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,
        @Size(max = 500) String bio,
        @Size(max = 100) String specialization,
        @Min(0) @Max(60) Integer experienceYear,
        @Size(max = 100) String city,
        LocalDate birthDay
) {}