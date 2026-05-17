package com.fatihdemir.diyetappbackend.dto.availability;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AvailabilitySlotRequest(
        @NotNull @Future LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime
) {}