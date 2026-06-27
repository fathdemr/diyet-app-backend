package com.fatihdemir.diyetappbackend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Data
public class Notification {

    private NotificationType type;

    private String message;

    private String title;

    private String email;

    public Map<String, Object> data;


}
