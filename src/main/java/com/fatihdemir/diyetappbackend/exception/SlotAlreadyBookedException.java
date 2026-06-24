package com.fatihdemir.diyetappbackend.exception;

public class SlotAlreadyBookedException extends AppException {

    public SlotAlreadyBookedException() {
        super(ErrorCode.SLOT_ALREADY_BOOKED);
    }

    public SlotAlreadyBookedException(Object slotId) {
        super(ErrorCode.SLOT_ALREADY_BOOKED, "Slot zaten rezerve edilmiş: " + slotId);
    }
}