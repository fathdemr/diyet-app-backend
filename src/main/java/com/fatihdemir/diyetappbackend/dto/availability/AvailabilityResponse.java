package com.fatihdemir.diyetappbackend.dto.availability;

import com.fatihdemir.diyetappbackend.entity.DietitianAvailability;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvailabilityResponse(
        UUID id,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        boolean isBooked
) {
    public static AvailabilityResponse from(DietitianAvailability a, boolean isBooked) {
        return new AvailabilityResponse(
                a.getId(),
                a.getStartDateTime(),
                a.getEndDateTime(),
                isBooked
        );
    }
}