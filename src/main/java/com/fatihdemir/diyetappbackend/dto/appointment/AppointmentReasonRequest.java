package com.fatihdemir.diyetappbackend.dto.appointment;

import jakarta.validation.constraints.NotBlank;

public record AppointmentReasonRequest(
        @NotBlank String reason
) {
}