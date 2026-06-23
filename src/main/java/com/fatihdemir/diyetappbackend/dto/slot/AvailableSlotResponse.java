package com.fatihdemir.diyetappbackend.dto.slot;

import com.fatihdemir.diyetappbackend.entity.AvailableSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AvailableSlotResponse(
        UUID id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        boolean booked
) {
    public static AvailableSlotResponse from(AvailableSlot availableSlot) {
        return new AvailableSlotResponse(
                availableSlot.getId(),
                availableSlot.getDate(),
                availableSlot.getStartTime(),
                availableSlot.getEndTime(),
                availableSlot.isBooked()
        );
    }
}