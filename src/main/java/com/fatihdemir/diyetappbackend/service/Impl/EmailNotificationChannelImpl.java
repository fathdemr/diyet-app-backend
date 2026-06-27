package com.fatihdemir.diyetappbackend.service.Impl;

import com.fatihdemir.diyetappbackend.entity.Notification;
import com.fatihdemir.diyetappbackend.entity.NotificationType;
import com.fatihdemir.diyetappbackend.service.NotificationChannel;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationChannelImpl implements NotificationChannel {

    private static final int RESEND_BATCH_LIMIT = 100;

    private final Resend resendClient;
    private final Queue<Notification> emailQueue = new ConcurrentLinkedQueue<>();

    // Quota aşıldığında bu dolup scheduler o zamana kadar gönderimi atlar
    private volatile LocalDateTime pausedUntil = null;

    @Override
    public void send(Notification notification) {
        emailQueue.add(notification);
        log.debug("[EMAIL-QUEUE] Kuyruğa eklendi: to={}, subject={}", notification.getEmail(), notification.getTitle());
    }

    @Scheduled(fixedDelayString = "${notification.email.flush-interval-ms:60000}")
    public void flushQueue() {
        if (emailQueue.isEmpty()) return;

        // Quota dolu ve süre dolmadıysa atla
        if (pausedUntil != null && LocalDateTime.now().isBefore(pausedUntil)) {
            log.debug("[EMAIL-BATCH] Gönderim duraklatıldı. Devam zamanı: {}", pausedUntil);
            return;
        }
        pausedUntil = null;

        List<Notification> drained = new ArrayList<>();
        Notification n;
        while ((n = emailQueue.poll()) != null) {
            drained.add(n);
        }

        List<CreateEmailOptions> allOptions = drained.stream()
                .map(notification -> CreateEmailOptions.builder()
                        .from("info@blockcertify.uk")
                        .to(notification.getEmail())
                        .subject(notification.getTitle())
                        .html(notification.getMessage())
                        .build())
                .toList();

        log.info("[EMAIL-BATCH] {} mail gönderiliyor", allOptions.size());

        for (int i = 0; i < allOptions.size(); i += RESEND_BATCH_LIMIT) {
            List<CreateEmailOptions> chunk = allOptions.subList(i, Math.min(i + RESEND_BATCH_LIMIT, allOptions.size()));
            List<Notification> chunkDrained = drained.subList(i, Math.min(i + RESEND_BATCH_LIMIT, drained.size()));
            try {
                resendClient.batch().send(chunk);
                log.debug("[EMAIL-BATCH] Chunk gönderildi: {}/{}", Math.min(i + RESEND_BATCH_LIMIT, allOptions.size()), allOptions.size());
            } catch (ResendException e) {
                if (isQuotaExceeded(e)) {
                    // Günlük kota doldu — bu ve kalan tüm chunk'ları kuyruğa geri al, yarına kadar bekle
                    List<Notification> remaining = drained.subList(i, drained.size());
                    emailQueue.addAll(remaining);
                    pausedUntil = LocalDate.now().plusDays(1).atStartOfDay();
                    log.warn("[EMAIL-BATCH] Günlük kota aşıldı. {} mail kuyruğa alındı, gönderim {} tarihinde devam edecek.",
                            remaining.size(), pausedUntil);
                    break;
                }
                // Geçici hata (network vb.) — sadece bu chunk'ı geri koy
                log.error("[EMAIL-BATCH] Chunk gönderilemedi, {} mail yeniden kuyruğa alındı: {}", chunkDrained.size(), e.getMessage());
                emailQueue.addAll(chunkDrained);
            }
        }
    }

    private boolean isQuotaExceeded(ResendException e) {
        return e.getMessage() != null && e.getMessage().toLowerCase().contains("quota");
    }

    @Override
    public boolean supports(NotificationType type) {
        return type == NotificationType.EMAIL;
    }
}