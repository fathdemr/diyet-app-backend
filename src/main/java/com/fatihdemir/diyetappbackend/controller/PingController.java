package com.fatihdemir.diyetappbackend.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/ping")
public class PingController {

    private final StringRedisTemplate redisTemplate;

    public PingController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> ping() {
        String redisStatus;
        try {
            redisTemplate.opsForValue().set("ping", "pong");
            redisStatus = redisTemplate.opsForValue().get("ping");
        } catch (Exception e) {
            redisStatus = "error: " + e.getMessage();
        }

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "redis", redisStatus != null ? redisStatus : "null"
        ));
    }
}