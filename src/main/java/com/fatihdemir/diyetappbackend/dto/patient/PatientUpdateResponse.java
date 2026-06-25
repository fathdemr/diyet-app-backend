package com.fatihdemir.diyetappbackend.dto.patient;

import com.fatihdemir.diyetappbackend.entity.Patient;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PatientUpdateResponse(
        LocalDate birthDay,
        BigDecimal height,
        BigDecimal weight,
        String goal,
        String gender,
        String firstName,
        String lastName,
        String userName
) {


    // Bu bir Factory Method pattern örneği. Amacımız nesne oluşturmadan from metodunu çağırabilmek.
    // Bu şekilde dto dönüşüm mantığını dto içinde tutuyoruz.
    public static PatientUpdateResponse from(Patient patient) {
        return new PatientUpdateResponse(
                patient.getBirthDay(),
                patient.getHeight(),
                patient.getWeight(),
                patient.getGoal(),
                patient.getGender(),
                patient.getUser().getFirstName(),
                patient.getUser().getLastName(),
                patient.getUser().getUserName()
        );
    }
}
