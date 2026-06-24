package com.fatihdemir.diyetappbackend.exception;

public class FirebaseAuthException extends AppException {

    public FirebaseAuthException(String message) {
        super(ErrorCode.FIREBASE_TOKEN_INVALID, message);
    }
}