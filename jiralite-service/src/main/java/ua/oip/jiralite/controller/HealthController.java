package ua.oip.jiralite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Контроллер для проверки состояния сервиса
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    
    /**
     * Проверка состояния сервиса
     * @return статус сервиса UP
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> healthStatus = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "service", "JiraLite Service API",
            "version", "1.0.0-SNAPSHOT"
        );
        
        return ResponseEntity.ok(healthStatus);
    }
} 