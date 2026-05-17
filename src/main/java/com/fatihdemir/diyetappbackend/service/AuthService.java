package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.AuthResponse;
import com.fatihdemir.diyetappbackend.dto.ForgotPasswordRequest;
import com.fatihdemir.diyetappbackend.dto.OAuthRequest;
import com.fatihdemir.diyetappbackend.dto.RefreshRequest;
import com.fatihdemir.diyetappbackend.entity.*;
import com.fatihdemir.diyetappbackend.exception.AuthException;
import com.fatihdemir.diyetappbackend.repository.ClientProfileRepository;
import com.fatihdemir.diyetappbackend.repository.DietitianProfileRepository;
import com.fatihdemir.diyetappbackend.repository.UserRepository;
import com.fatihdemir.diyetappbackend.security.JwtProperties;
import com.fatihdemir.diyetappbackend.security.JwtService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String IDENTITY_TOOLKIT_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode";

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final DietitianProfileRepository dietitianProfileRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RedisTokenService redisTokenService;

    @Value("${firebase.web-api-key}")
    private String firebaseWebApiKey;

    // ── OAuth Login ───────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse oauthLogin(OAuthRequest request, Role role) {
        FirebaseToken firebaseToken = verifyFirebaseToken(request.firebaseToken());

        String uid             = firebaseToken.getUid();
        String email           = firebaseToken.getEmail();
        String name            = firebaseToken.getName();
        LoginProvider provider = extractProvider(firebaseToken);

        /*
        if (provider == LoginProvider.EMAIL && !firebaseToken.isEmailVerified()) {
            throw new AuthException(
                    "E-posta adresiniz doğrulanmamış. Lütfen gelen kutunuzu kontrol edin.",
                    HttpStatus.FORBIDDEN
            );
        }

         */

        User user = findOrCreateUser(uid, email, name, provider, role);
        String fullName = getProfileFullName(user);

        String accessToken  = jwtService.generateAccessToken(user, fullName);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
    }

    // ── Token Refresh (with rotation) ─────────────────────────────────────────

    public AuthResponse refresh(RefreshRequest request) {
        UUID userId = redisTokenService.getUserIdByRefreshToken(request.refreshToken())
                .orElseThrow(() -> new AuthException("Geçersiz veya süresi dolmuş refresh token", HttpStatus.UNAUTHORIZED));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Kullanıcı bulunamadı", HttpStatus.NOT_FOUND));

        String fullName = getProfileFullName(user);
        String newAccessToken  = jwtService.generateAccessToken(user, fullName);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken, jwtProperties.getAccessTokenExpiration());
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    public void logout(String authHeader) {
        String token = authHeader.substring(7);
        UUID userId  = UUID.fromString(jwtService.extractSubject(token));
        jwtService.revokeAllUserTokens(userId, token);
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    public void forgotPassword(ForgotPasswordRequest request) {
        callIdentityToolkit(Map.of(
                "requestType", "PASSWORD_RESET",
                "email", request.email()
        ));
    }

    // ── Resend Verification Email ─────────────────────────────────────────────

    public void resendVerification(OAuthRequest request) {
        callIdentityToolkit(Map.of(
                "requestType", "VERIFY_EMAIL",
                "idToken", request.firebaseToken()
        ));
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private FirebaseToken verifyFirebaseToken(String idToken) {
        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new IllegalArgumentException("Geçersiz Firebase token: " + e.getMessage(), e);
        }
    }

    private LoginProvider extractProvider(FirebaseToken token) {
        @SuppressWarnings("unchecked")
        Map<String, Object> firebaseClaims = (Map<String, Object>) token.getClaims().get("firebase");
        if (firebaseClaims == null) return LoginProvider.EMAIL;

        String signInProvider = (String) firebaseClaims.get("sign_in_provider");
        return switch (signInProvider != null ? signInProvider : "") {
            case "google.com" -> LoginProvider.GOOGLE;
            case "apple.com"  -> LoginProvider.APPLE;
            default           -> LoginProvider.EMAIL;
        };
    }

    private User findOrCreateUser(String uid, String email, String name, LoginProvider provider, Role role) {
        Optional<User> byUid = userRepository.findByFireBaseUid(uid);
        if (byUid.isPresent()) {
            validateRole(byUid.get(), role);
            return byUid.get();
        }

        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            validateRole(user, role);
            user.setFireBaseUid(uid);
            user.setLoginProvider(provider);
            return userRepository.save(user);
        }

        return buildNewUser(uid, email, name, provider, role);
    }

    private void validateRole(User user, Role requestedRole) {
        if (user.getRole() != requestedRole) {
            String correctEndpoint = user.getRole() == Role.CLIENT
                    ? "/api/auth/client/login"
                    : "/api/auth/dietitian/login";
            throw new AuthException(
                    "Bu hesap " + user.getRole().name() + " rolüne sahiptir. Lütfen " + correctEndpoint + " kullanın.",
                    HttpStatus.FORBIDDEN
            );
        }
    }

    private User buildNewUser(String uid, String email, String name, LoginProvider provider, Role role) {
        String fullName = (name != null && !name.isBlank()) ? name : email.split("@")[0];
        String[] parts  = fullName.split(" ", 2);

        User user = new User();
        user.setFireBaseUid(uid);
        user.setEmail(email);
        user.setRole(role);
        user.setLoginProvider(provider);
        user.setEnabled(true);
        user = userRepository.save(user);

        if (role == Role.DIETITIAN) {
            DietitianProfile profile = new DietitianProfile();
            profile.setUser(user);
            profile.setFullName(fullName);
            profile.setFirstName(parts[0]);
            profile.setLastName(parts.length > 1 ? parts[1] : null);
            dietitianProfileRepository.save(profile);
        } else {
            ClientProfile profile = new ClientProfile();
            profile.setUser(user);
            profile.setFullName(fullName);
            profile.setFirstName(parts[0]);
            profile.setLastName(parts.length > 1 ? parts[1] : null);
            clientProfileRepository.save(profile);
        }

        return user;
    }

    private String getProfileFullName(User user) {
        if (user.getRole() == Role.DIETITIAN) {
            return dietitianProfileRepository.findByUserId(user.getId())
                    .map(DietitianProfile::getFullName)
                    .orElse(user.getEmail());
        }
        return clientProfileRepository.findByUserId(user.getId())
                .map(ClientProfile::getFullName)
                .orElse(user.getEmail());
    }

    private void callIdentityToolkit(Map<String, String> body) {
        try {
            RestClient.create()
                    .post()
                    .uri(IDENTITY_TOOLKIT_URL + "?key=" + firebaseWebApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Firebase Identity Toolkit hatası: {}", e.getMessage());
            throw new AuthException("İşlem gerçekleştirilemedi. Lütfen tekrar deneyin.", HttpStatus.BAD_GATEWAY);
        }
    }
}