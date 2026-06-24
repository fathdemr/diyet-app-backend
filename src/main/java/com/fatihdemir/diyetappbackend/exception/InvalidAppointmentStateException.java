package com.fatihdemir.diyetappbackend.exception;

import com.fatihdemir.diyetappbackend.entity.AppointmentStatus;

public class InvalidAppointmentStateException extends AppException {

    public InvalidAppointmentStateException(String action, AppointmentStatus currentStatus) {
        super(ErrorCode.INVALID_APPOINTMENT_STATE,
                "'" + action + "' işlemi '" + currentStatus + "' durumundaki randevu için yapılamaz");
    }
}