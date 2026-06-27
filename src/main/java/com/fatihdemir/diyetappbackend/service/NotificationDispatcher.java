package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.entity.Notification;
import com.fatihdemir.diyetappbackend.exception.NotificationChannelException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final List<NotificationChannel> channels;

    public void dispatch(Notification notification) {
        List<NotificationChannel> eligible = channels.stream()
                .filter(channel -> channel.supports(notification.getType()))
                .toList();

        if (eligible.isEmpty()) {
            throw new NotificationChannelException("Desteklenen kanal bulunamadı: " + notification.getType());
        }

        // Her kanal bağımsız çalışır — biri başarısız olsa diğerleri etkilenmez
        for (NotificationChannel channel : eligible) {
            try {
                channel.send(notification);
            } catch (Exception e) {
                log.error("[DISPATCHER] Kanal başarısız: channel={}, to={}, hata={}",
                        channel.getClass().getSimpleName(), notification.getEmail(), e.getMessage());
            }
        }
    }
}