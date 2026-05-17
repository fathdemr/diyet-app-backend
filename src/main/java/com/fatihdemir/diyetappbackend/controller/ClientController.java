package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.client.ClientResponse;
import com.fatihdemir.diyetappbackend.dto.client.ClientUpdateRequest;
import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/exapi/clients")
    public ResponseEntity<PageResponse<ClientResponse>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable){
        return ResponseEntity.ok(clientService.getClients(pageable));
    }

    @GetMapping("/exapi/clients/{id}")
    public ResponseEntity<ClientResponse> getById(@PathVariable UUID id){
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PreAuthorize("hasRole('CLIENTS')")
    @PatchMapping("/exapi/clients/me")
    public ResponseEntity<ClientResponse> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ClientUpdateRequest request) {
        return ResponseEntity.ok(clientService.updateProfile(userId, request));
    }
}
