package com.fatihdemir.diyetappbackend.exception;

public class NotificationChannelException extends AppException {
    public NotificationChannelException(String message) {
        super(ErrorCode.NO_CHANNEL_AVAILABLE, message);
    }
}
