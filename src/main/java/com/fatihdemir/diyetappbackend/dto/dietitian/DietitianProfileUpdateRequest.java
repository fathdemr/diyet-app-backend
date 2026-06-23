package com.fatihdemir.diyetappbackend.dto.dietitian;

import java.time.LocalDate;

public record DietitianProfileUpdateRequest(
        LocalDate birthDay,
        String bio,
        Integer experienceYear,
        String city,
        String userName,
        String firstName,
        String lastName

) {
}
