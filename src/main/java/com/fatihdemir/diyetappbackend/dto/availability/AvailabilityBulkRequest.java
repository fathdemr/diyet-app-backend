package com.fatihdemir.diyetappbackend.dto.availability;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AvailabilityBulkRequest(
        @NotEmpty List<@Valid AvailabilitySlotRequest> slots
) {}