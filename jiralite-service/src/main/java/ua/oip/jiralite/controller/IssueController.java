package ua.oip.jiralite.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Контроллер для работы с задачами (Issues)
 */
@RestController
@RequestMapping("/issue")
public class IssueController {
    
    // Простое in-memory хранилище для демонстрации
    private final Map<Long, IssueDto> issueStorage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * Создание новой задачи
     * @param issueRequest данные для создания задачи
     * @return созданная задача с присвоенным ID
     */
    @PostMapping
    public ResponseEntity<IssueDto> createIssue(@RequestBody CreateIssueRequest issueRequest) {
        Long id = idGenerator.getAndIncrement();
        
        IssueDto issue = new IssueDto(
            id,
            issueRequest.getTitle(),
            issueRequest.getDescription(),
            issueRequest.getPriority() != null ? issueRequest.getPriority() : "MEDIUM",
            "OPEN",
            LocalDateTime.now(),
            null
        );
        
        issueStorage.put(id, issue);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }
    
    /**
     * Получение задачи по ID
     * @param id ID задачи
     * @return задача или 404 если не найдена
     */
    @GetMapping("/{id}")
    public ResponseEntity<IssueDto> getIssue(@PathVariable Long id) {
        IssueDto issue = issueStorage.get(id);
        
        if (issue == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(issue);
    }
    
    /**
     * DTO для создания задачи
     */
    public static class CreateIssueRequest {
        private String title;
        private String description;
        private String priority;
        
        // Конструкторы
        public CreateIssueRequest() {}
        
        public CreateIssueRequest(String title, String description, String priority) {
            this.title = title;
            this.description = description;
            this.priority = priority;
        }
        
        // Геттеры и сеттеры
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
    
    /**
     * DTO для ответа с данными задачи
     */
    public static class IssueDto {
        private Long id;
        private String title;
        private String description;
        private String priority;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Конструкторы
        public IssueDto() {}
        
        public IssueDto(Long id, String title, String description, String priority, 
                       String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        // Геттеры и сеттеры
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
} 