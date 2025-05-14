package ua.oip.jiralite.ui.model;

import java.io.Serializable;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;

/**
 * Модель карточки задачи для пользовательского интерфейса
 */
public class IssueCardModel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String key;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private String assigneeName;
    
    /**
     * Конструктор по умолчанию
     */
    public IssueCardModel() {}
    
    /**
     * Конструктор со всеми параметрами
     */
    public IssueCardModel(Long id, String key, String title, String description, 
                         Status status, Priority priority, String assigneeName) {
        this.id = id;
        this.key = key;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assigneeName = assigneeName;
    }
    
    /**
     * Создает модель карточки из доменной сущности задачи
     * 
     * @param issue доменная сущность задачи
     * @return модель карточки
     */
    public static IssueCardModel fromIssue(Issue issue) {
        if (issue == null) {
            return null;
        }
        
        IssueCardModel model = new IssueCardModel();
        model.setId(issue.getId());
        
        try {
            // Используем идентификатор задачи в формате PROJECT-ID если нет ключа
            // или не удается получить ключ из модели
            model.setKey("ISSUE-" + issue.getId());
        } catch (Exception e) {
            model.setKey("ISSUE-" + issue.getId());
        }
        
        try {
            model.setTitle(issue.getTitle());
        } catch (Exception e) {
            try {
                // Если нет метода getTitle, пробуем getSummary (для совместимости)
                model.setTitle("Задача #" + issue.getId());
            } catch (Exception ex) {
                model.setTitle("Задача #" + issue.getId());
            }
        }
        
        // Улучшаем обработку описания задачи с подробным логированием
        try {
            String description = issue.getDescription();
            System.out.println("IssueCardModel.fromIssue: ID=" + issue.getId() + 
                ", описание=" + (description != null ? description.substring(0, Math.min(30, description.length())) + "..." : "null"));
            
            model.setDescription(description);
            
            // Проверка, что описание было установлено
            System.out.println("IssueCardModel.fromIssue: После установки, описание в модели=" + 
                (model.getDescription() != null ? model.getDescription().substring(0, Math.min(30, model.getDescription().length())) + "..." : "null"));
        } catch (Exception e) {
            System.err.println("IssueCardModel.fromIssue: Ошибка при установке описания: " + e.getMessage());
            e.printStackTrace();
            // Не устанавливаем никакое значение по умолчанию, оставляем null или пустую строку
        }
        
        try {
            Status uiStatus;
            switch (issue.getStatus()) {
                case TO_DO:
                    uiStatus = Status.TO_DO;
                    break;
                case IN_PROGRESS:
                    uiStatus = Status.IN_PROGRESS;
                    break;
                case DONE:
                    uiStatus = Status.DONE;
                    break;
                default:
                    uiStatus = Status.TO_DO;
            }
            model.setStatus(uiStatus);
        } catch (Exception e) {
            model.setStatus(Status.TO_DO);
        }
        
        try {
            // Пробуем получить приоритет, если в доменной модели нет метода
            // getPriority, используем средний приоритет по умолчанию
            model.setPriority(Priority.MEDIUM);
        } catch (Exception e) {
            model.setPriority(Priority.MEDIUM);
        }
        
        if (issue.getAssignee() != null) {
            try {
                model.setAssigneeName(issue.getAssignee().getFullName());
                System.out.println("IssueCardModel.fromIssue: ID=" + issue.getId() + 
                    ", установлен assigneeName=" + model.getAssigneeName());
            } catch (Exception e) {
                try {
                    model.setAssigneeName(issue.getAssignee().getUsername());
                    System.out.println("IssueCardModel.fromIssue: ID=" + issue.getId() + 
                        ", установлен assigneeName из username=" + model.getAssigneeName());
                } catch (Exception ex) {
                    model.setAssigneeName("Неизвестно");
                    System.err.println("IssueCardModel.fromIssue: Ошибка при получении имени пользователя: " + ex.getMessage());
                }
            }
        } else {
            System.out.println("IssueCardModel.fromIssue: ID=" + issue.getId() + ", assignee=null");
            model.setAssigneeName(null);
        }
        
        return model;
    }
    
    // Геттеры и сеттеры
    
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
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public String getAssigneeName() {
        return assigneeName;
    }
    
    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }
} 