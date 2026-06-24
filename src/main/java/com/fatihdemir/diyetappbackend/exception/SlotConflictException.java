package com.fatihdemir.diyetappbackend.exception;

import java.time.LocalTime;

public class SlotConflictException extends AppException {

    public SlotConflictException(LocalTime start, LocalTime end) {
        super(ErrorCode.SLOT_CONFLICT, "Slot çakışması: " + start + " - " + end);
    }
}