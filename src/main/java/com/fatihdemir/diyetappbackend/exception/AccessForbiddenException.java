package com.fatihdemir.diyetappbackend.exception;

public class AccessForbiddenException extends AppException {

    public AccessForbiddenException() {
        super(ErrorCode.ACCESS_FORBIDDEN);
    }

    public AccessForbiddenException(String message) {
        super(ErrorCode.ACCESS_FORBIDDEN, message);
    }
}