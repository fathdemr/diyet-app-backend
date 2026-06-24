package com.fatihdemir.diyetappbackend.exception;

public class RoleConflictException extends AppException {

    public RoleConflictException(String message) {
        super(ErrorCode.ROLE_CONFLICT, message);
    }
}