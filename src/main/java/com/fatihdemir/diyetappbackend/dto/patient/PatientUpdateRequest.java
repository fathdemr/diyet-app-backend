package com.fatihdemir.diyetappbackend.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PatientUpdateRequest(
        @NotNull LocalDate birthDay,
        @NotNull BigDecimal height,
        @NotNull BigDecimal weight,
        @NotBlank String goal,
        @NotBlank String gender,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String userName

) {
}
