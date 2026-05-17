package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.UserResponse;
import com.fatihdemir.diyetappbackend.entity.Role;
import com.fatihdemir.diyetappbackend.entity.User;
import com.fatihdemir.diyetappbackend.exception.AuthException;
import com.fatihdemir.diyetappbackend.repository.ClientProfileRepository;
import com.fatihdemir.diyetappbackend.repository.DietitianProfileRepository;
import com.fatihdemir.diyetappbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final DietitianProfileRepository dietitianProfileRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(String principalId) {
        UUID userId = UUID.fromString(principalId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        if (user.getRole() == Role.DIETITIAN) {
            var profile = dietitianProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new AuthException("Profil bulunamadı", HttpStatus.NOT_FOUND));
            return UserResponse.from(user, profile.getFirstName(), profile.getLastName(),
                    profile.getFullName(), profile.getBirthDay());
        }

        var profile = clientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException("Profil bulunamadı", HttpStatus.NOT_FOUND));
        return UserResponse.from(user, profile.getFirstName(), profile.getLastName(),
                profile.getFullName(), profile.getBirthDay());
    }
}