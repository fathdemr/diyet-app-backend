package com.fatihdemir.diyetappbackend.dto.appointment;

import jakarta.validation.constraints.NotBlank;

public record CancelAppointmentRequest(
        @NotBlank String reason
) {}