package com.fatihdemir.diyetappbackend.dto.university;

import com.fatihdemir.diyetappbackend.entity.University;

public record UniversityResponse(
        String id,
        String name,
        String yokCode
) {
    public static UniversityResponse from(University u) {
        return new UniversityResponse(
                u.getId(),
                u.getName(),
                u.getYokCode()
        );
    }
}
