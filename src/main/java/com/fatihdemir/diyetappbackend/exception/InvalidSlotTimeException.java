package com.fatihdemir.diyetappbackend.exception;

import java.time.LocalTime;

public class InvalidSlotTimeException extends AppException {

    public InvalidSlotTimeException(LocalTime start, LocalTime end) {
        super(ErrorCode.INVALID_SLOT_TIME,
                "Başlangıç saati bitiş saatinden önce olmalıdır: " + start + " >= " + end);
    }
}