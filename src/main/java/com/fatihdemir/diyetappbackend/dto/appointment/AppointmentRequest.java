package com.fatihdemir.diyetappbackend.dto.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentRequest(
        @NotNull UUID dietitianId,
        @NotNull @Future LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime,
        String notes
) {}