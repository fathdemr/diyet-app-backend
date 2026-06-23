package com.fatihdemir.diyetappbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public abstract class BaseController {

    protected String getToken(HttpServletRequest request) {
        return (String) request.getAttribute("jwt_token");
    }

    protected UUID getCurrentUserId() {
        return UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
        );
    }
}