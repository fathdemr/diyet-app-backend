package com.fatihdemir.diyetappbackend.dto.dietitian;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DietitianProfileUpdateRequest(
        @NotNull LocalDate birthDay,
        @NotBlank String bio,
        @NotNull Integer experienceYear,
        @NotBlank String city,
        @NotBlank String userName,
        @NotBlank String firstName,
        @NotBlank String lastName

) {
}
