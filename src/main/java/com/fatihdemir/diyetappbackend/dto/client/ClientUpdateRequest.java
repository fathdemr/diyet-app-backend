package com.fatihdemir.diyetappbackend.dto.client;

import jakarta.validation.constraints.Size;

public record ClientUpdateRequest(
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,
        Double height,
        Double weight,
        String goal
) {
}
