package com.fatihdemir.diyetappbackend.exception;

public class BatchSizeExceededException extends AppException {

    public BatchSizeExceededException(int max) {
        super(ErrorCode.BATCH_SIZE_EXCEEDED, "Toplu işlem limiti aşıldı. Maksimum: " + max);
    }
}