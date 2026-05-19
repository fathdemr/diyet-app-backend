package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.dto.client.ClientResponse;
import com.fatihdemir.diyetappbackend.dto.client.ClientUpdateRequest;
import com.fatihdemir.diyetappbackend.exception.AuthException;
import com.fatihdemir.diyetappbackend.repository.ClientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientProfileRepository clientProfileRepository;

    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> getClients(Pageable pageable) {
        return PageResponse.from(
                clientProfileRepository.findAll(pageable).map(ClientResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(UUID userId) {
        return clientProfileRepository.findByUserId(userId)
                .map(ClientResponse::from)
                .orElseThrow(() -> new AuthException("Danışan bulunumadı", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ClientResponse updateProfile(String principalId, ClientUpdateRequest request) {
        var profile = clientProfileRepository.findByUserId(UUID.fromString(principalId))
                .orElseThrow(() -> new AuthException("Danışan bulunamadı", HttpStatus.NOT_FOUND));

        if (request.firstName() != null) profile.setFirstName(request.firstName());
        if (request.lastName() != null) profile.setLastName(request.lastName());
        if (request.height() != null) profile.setHeight(request.height());
        if (request.weight() != null) profile.setWeight(request.weight());
        if (request.goal() != null) profile.setGoal(request.goal());
        if (request.gender() != null) profile.setGender(request.gender());

        if (request.firstName() != null || request.lastName() != null) {
            String first = profile.getFirstName() != null ? profile.getFirstName() : "";
            String last = profile.getLastName() != null ? profile.getLastName() : "";
            profile.setFullName((first + " " + last).trim());
        }

        return ClientResponse.from(clientProfileRepository.save(profile));
    }
}
