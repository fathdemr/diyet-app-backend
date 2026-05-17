package com.fatihdemir.diyetappbackend.dto.client;

import com.fatihdemir.diyetappbackend.entity.ClientProfile;

import java.time.LocalDate;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        LocalDate birthDay,
        Double height,
        Double weight,
        String goal
) {
    public static ClientResponse from(ClientProfile p) {
        return new ClientResponse(
                p.getUserId(),
                p.getFirstName(),
                p.getLastName(),
                p.getFullName(),
                p.getBirthDay(),
                p.getHeight(),
                p.getWeight(),
                p.getGoal()
        );
    }
}
