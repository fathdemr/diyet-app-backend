package com.fatihdemir.diyetappbackend.controller;

import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseController {

    protected String getToken(HttpServletRequest request) {
        return (String) request.getAttribute("jwt_token");
    }
}