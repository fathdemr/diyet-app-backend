package com.fatihdemir.diyetappbackend.dto;

import com.fatihdemir.diyetappbackend.entity.LoginProvider;
import com.fatihdemir.diyetappbackend.entity.Role;
import com.fatihdemir.diyetappbackend.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        Role role,
        LoginProvider loginProvider,
        LocalDate birthDay,
        boolean enabled,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user, String firstName, String lastName,
                                    String fullName, LocalDate birthDay) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                firstName,
                lastName,
                fullName,
                user.getRole(),
                user.getLoginProvider(),
                birthDay,
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}