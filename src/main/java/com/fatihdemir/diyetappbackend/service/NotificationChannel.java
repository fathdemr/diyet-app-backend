package com.fatihdemir.diyetappbackend.service;


import com.fatihdemir.diyetappbackend.entity.Notification;
import com.fatihdemir.diyetappbackend.entity.NotificationType;

public interface NotificationChannel {

    void send(Notification notification);

    boolean supports(NotificationType type);
}
