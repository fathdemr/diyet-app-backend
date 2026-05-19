package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.dto.university.UniversityResponse;
import com.fatihdemir.diyetappbackend.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UniversityService {

    private final UniversityRepository universityRepository;

    @Transactional(readOnly = true)
    public PageResponse<UniversityResponse> getUniversities(Pageable pageable) {
        return PageResponse.from(
                universityRepository.findAll(pageable).map(UniversityResponse::from)
        );
    }
}
