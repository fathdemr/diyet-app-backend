package com.fatihdemir.diyetappbackend.service.Impl;

import com.fatihdemir.diyetappbackend.dto.patient.PatientUpdateRequest;
import com.fatihdemir.diyetappbackend.dto.patient.PatientUpdateResponse;
import com.fatihdemir.diyetappbackend.exception.ResourceNotFoundException;
import com.fatihdemir.diyetappbackend.repository.PatientRepository;
import com.fatihdemir.diyetappbackend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public PatientUpdateResponse updatePatient(PatientUpdateRequest request, UUID userId) {
        var patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta"));

        var user = patient.getUser();

        patient.setBirthDay(request.birthDay());
        patient.setGoal(request.goal());
        patient.setGender(request.gender());
        patient.setWeight(request.weight());
        patient.setHeight(request.height());

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setFullName(request.firstName() + " " + request.lastName());
        user.setUserName(request.userName());

        patientRepository.save(patient);

        return PatientUpdateResponse.from(patient);
    }

    @Override
    @Transactional
    public void deletePatient(UUID userId) {
        var patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta"));

        patientRepository.delete(patient);
    }


}
