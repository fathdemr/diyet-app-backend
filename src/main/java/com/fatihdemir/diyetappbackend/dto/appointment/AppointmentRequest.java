package com.fatihdemir.diyetappbackend.dto.appointment;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AppointmentRequest(
        @NotNull UUID slotId
) {
}
