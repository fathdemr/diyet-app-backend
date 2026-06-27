package com.fatihdemir.diyetappbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupHealthChecker {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final KafkaAdmin kafkaAdmin;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("================================================");
        log.info("  {} starting up — service health check", "diyet-app-backend");
        log.info("================================================");
        checkDatabase();
        checkRedis();
        checkKafka();
        log.info("================================================");
        log.info("  Uygulama hazir. Port: 5075");
        log.info("================================================");
    }

    private void checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                log.info("  [OK] PostgreSQL  — baglanti saglam");
            } else {
                log.warn("  [WARN] PostgreSQL — baglanti gecersiz");
            }
        } catch (Exception e) {
            log.error("  [FAIL] PostgreSQL — {}", e.getMessage());
        }
    }

    private void checkRedis() {
        try (var conn = redisConnectionFactory.getConnection()) {
            String pong = conn.ping();
            if ("PONG".equalsIgnoreCase(pong)) {
                log.info("  [OK] Redis        — baglanti saglam");
            } else {
                log.warn("  [WARN] Redis       — beklenmeyen yanit: {}", pong);
            }
        } catch (Exception e) {
            log.error("  [FAIL] Redis       — {}", e.getMessage());
        }
    }

    private void checkKafka() {
        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            client.listTopics().names().get(3, TimeUnit.SECONDS);
            log.info("  [OK] Kafka        — broker erisimi saglam");
        } catch (Exception e) {
            log.error("  [FAIL] Kafka       — {}", e.getMessage());
        }
    }
}