package ua.oip.jiralite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения для JiraLite Service API
 */
@SpringBootApplication
public class JiraLiteServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(JiraLiteServiceApplication.class, args);
        System.out.println("JiraLite Service API запущен на порту 8080");
        System.out.println("Доступные endpoints:");
        System.out.println("  GET  /api/health - проверка состояния сервиса");
        System.out.println("  POST /issue - создание новой задачи");
        System.out.println("  GET  /issue/{id} - получение задачи по ID");
    }
} 