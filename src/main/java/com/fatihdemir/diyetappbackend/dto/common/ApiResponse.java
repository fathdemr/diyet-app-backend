package com.fatihdemir.diyetappbackend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String status,
        String message,
        T data,
        List<FieldError> fieldErrors
) {

    public record FieldError(String field, String message) {}

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", null, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data, null);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("error", message, null, null);
    }

    public static ApiResponse<Void> error(String message, List<FieldError> fieldErrors) {
        return new ApiResponse<>("error", message, null, fieldErrors);
    }
}