package com.fatihdemir.diyetappbackend.service.Impl;

import com.fatihdemir.diyetappbackend.dto.auth.AuthResponse;
import com.fatihdemir.diyetappbackend.dto.auth.OAuthRequest;
import com.fatihdemir.diyetappbackend.entity.*;
import com.fatihdemir.diyetappbackend.repository.ClientRepository;
import com.fatihdemir.diyetappbackend.repository.DietitianRepository;
import com.fatihdemir.diyetappbackend.repository.UserRepository;
import com.fatihdemir.diyetappbackend.security.JwtProperties;
import com.fatihdemir.diyetappbackend.security.JwtService;
import com.fatihdemir.diyetappbackend.service.AuthService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final DietitianRepository dietitianRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final FirebaseAuth firebaseAuth;

    // ── OAuth Login ───────────────────────────────────────────────────────────
    public AuthResponse oauthLogin(OAuthRequest request, Role role) {
        FirebaseToken firebaseToken = verifyFirebaseToken(request.firebaseToken());

        String uid = firebaseToken.getUid();
        String email = firebaseToken.getEmail();
        String name = firebaseToken.getName();
        LoginProvider provider = extractProvider(firebaseToken);

        User user = findOrCreateUser(uid, email, name, provider, role);
        String fullName = user.getFullName();

        String accessToken = jwtService.generateAccessToken(user, fullName);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());

    }


    // ── Private Helpers ───────────────────────────────────────────────────────
    private FirebaseToken verifyFirebaseToken(String idToken) {
        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new IllegalArgumentException("Geçersiz Firebase token: " + e.getMessage(), e);
        }
    }

    private LoginProvider extractProvider(FirebaseToken firebaseToken) {
        @SuppressWarnings("unchecked")
        Map<String, Object> firebaseClaims = (Map<String, Object>) firebaseToken.getClaims().get("firebase");
        if (firebaseClaims == null) {
            return LoginProvider.EMAIL;
        }

        String signInProvider = (String) firebaseClaims.get("sign_in_provider");
        return switch (signInProvider != null ? signInProvider : "") {
            case "google.com" -> LoginProvider.GOOGLE;
            case "apple.com" -> LoginProvider.APPLE;
            default -> LoginProvider.EMAIL;
        };
    }

    private User findOrCreateUser(String uid, String email, String name, LoginProvider provider, Role role) {
        Optional<User> byUid = userRepository.findByFireBaseUid(uid);
        if (byUid.isPresent()) {
            return byUid.get();
        }

        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            user.setFireBaseUid(uid);
            user.setLoginProvider(provider);
            return userRepository.save(user);
        }

        return buildNewUser(uid, email, name, provider, role);
    }

    private User buildNewUser(String uid, String email, String name, LoginProvider provider, Role role) {
        String fullName = (name != null && !name.isBlank()) ? name : email.split("@")[0];
        String[] parts = fullName.split(" ", 2);

        User user = new User();
        user.setFireBaseUid(uid);
        user.setEmail(email);
        user.setRole(role);
        user.setLoginProvider(provider);
        user.setFullName(fullName);
        user.setFirstName(parts[0]);
        user.setLastName(parts.length > 1 ? parts[1] : null);
        user.setUserName(email.split("@")[0]);
        user = userRepository.save(user);

        if (role == Role.DIETITIAN) {
            Dietitian dietitian = new Dietitian();
            dietitian.setUser(user);
            dietitianRepository.save(dietitian);
        } else if (role == Role.CLIENT) {
            Client client = new Client();
            client.setUser(user);
            clientRepository.save(client);
        }

        return user;
    }
}
