package com.fatihdemir.diyetappbackend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Auth ─────────────────────────────────────────────────────────────────
    FIREBASE_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Geçersiz veya süresi dolmuş Firebase token"),
    ROLE_CONFLICT(HttpStatus.CONFLICT, "Bu hesap zaten farklı bir rolle kayıtlı"),

    // ── Resource ──────────────────────────────────────────────────────────────
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Kaynak bulunamadı"),

    // ── Authorization ─────────────────────────────────────────────────────────
    ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "Bu işlem için yetkiniz yok"),

    // ── Slot ──────────────────────────────────────────────────────────────────
    SLOT_ALREADY_BOOKED(HttpStatus.CONFLICT, "Bu slot zaten rezerve edilmiş"),
    SLOT_CONFLICT(HttpStatus.CONFLICT, "Slot mevcut bir saatle çakışıyor"),
    INVALID_SLOT_TIME(HttpStatus.BAD_REQUEST, "Başlangıç saati bitiş saatinden önce olmalıdır"),
    BATCH_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "Toplu işlem limiti aşıldı"),

    // ── Appointment ───────────────────────────────────────────────────────────
    INVALID_APPOINTMENT_STATE(HttpStatus.CONFLICT, "Randevu mevcut durumda bu işlemi gerçekleştiremez"),

    // ── Validation ────────────────────────────────────────────────────────────
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Giriş doğrulama hatası"),

    // ── Notification ───────────────────────────────────────────────────────────────

    NOTIFICATION_ERROR(HttpStatus.BAD_REQUEST, "Mail gönderimi hatası"),

    NO_CHANNEL_AVAILABLE(HttpStatus.BAD_REQUEST, "No channel supports notification type:"),

    // ── Generic ───────────────────────────────────────────────────────────────
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmedik bir hata oluştu");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}