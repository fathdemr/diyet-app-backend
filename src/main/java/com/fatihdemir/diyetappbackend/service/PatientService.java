package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.patient.PatientUpdateRequest;
import com.fatihdemir.diyetappbackend.dto.patient.PatientUpdateResponse;

import java.util.UUID;

public interface PatientService {

    PatientUpdateResponse updatePatient(PatientUpdateRequest request, UUID userId);

    void deletePatient(UUID userId);
}
