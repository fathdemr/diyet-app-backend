package com.fatihdemir.diyetappbackend.service;


import com.fatihdemir.diyetappbackend.dto.auth.AuthResponse;
import com.fatihdemir.diyetappbackend.dto.auth.OAuthRequest;
import com.fatihdemir.diyetappbackend.entity.Role;

public interface AuthService {

    AuthResponse oauthLogin(OAuthRequest request, Role role);

    void logout(String token);
}
