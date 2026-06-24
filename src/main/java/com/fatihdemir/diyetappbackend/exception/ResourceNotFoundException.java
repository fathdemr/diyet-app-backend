package com.fatihdemir.diyetappbackend.exception;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resourceName + " bulunamadı: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}