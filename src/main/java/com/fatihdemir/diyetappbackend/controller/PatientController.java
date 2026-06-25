package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.patient.PatientUpdateRequest;
import com.fatihdemir.diyetappbackend.dto.patient.PatientUpdateResponse;
import com.fatihdemir.diyetappbackend.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exapi/patient")
public class PatientController extends BaseController {

    private final PatientService patientService;

    @PutMapping
    public PatientUpdateResponse updatePatient(@RequestBody @Valid PatientUpdateRequest patientUpdateRequest) {
        return patientService.updatePatient(patientUpdateRequest, getCurrentUserId());
    }

    @DeleteMapping
    public void deletePatient() {
        patientService.deletePatient(getCurrentUserId());
    }

}
