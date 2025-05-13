package ua.oip.jiralite.ui.model;

import java.time.LocalDateTime;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;

/**
 * Модель даних для представлення картки задачі в інтерфейсі користувача.
 * Реалізація патерну DTO (Data Transfer Object) для відокремлення
 * UI від доменної моделі.
 */
public class IssueCardModel {
    private Long id;
    private String key;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private String reporterName;
    private String assigneeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Конструктор за замовчуванням
     */
    public IssueCardModel() {
    }
    
    /**
     * Створює модель на основі доменного об'єкту Issue
     */
    public static IssueCardModel fromIssue(Issue issue) {
        IssueCardModel model = new IssueCardModel();
        
        model.setId(issue.getId());
        model.setKey(issue.getKey());
        model.setTitle(issue.getTitle());
        model.setDescription(issue.getDescription());
        model.setPriority(issue.getPriority());
        model.setStatus(issue.getStatus());
        
        User reporter = issue.getReporter();
        if (reporter != null) {
            model.setReporterName(reporter.getFullName());
        }
        
        User assignee = issue.getAssignee();
        if (assignee != null) {
            model.setAssigneeName(assignee.getFullName());
        }
        
        model.setCreatedAt(issue.getCreatedAt());
        model.setUpdatedAt(issue.getUpdatedAt());
        
        return model;
    }
    
    /**
     * Оновлює доменний об'єкт Issue даними з моделі
     */
    public void updateIssue(Issue issue) {
        issue.setTitle(this.title);
        issue.setDescription(this.description);
        issue.setPriority(this.priority);
        issue.setStatus(this.status);
    }

    // Геттери та сеттери
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 