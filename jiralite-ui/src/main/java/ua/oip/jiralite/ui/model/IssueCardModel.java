package ua.oip.jiralite.ui.model;

import java.io.Serializable;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;

/**
 * Модель карточки задачі для користувальницького інтерфейсу
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
     * Конструктор за замовчуванням
     */
    public IssueCardModel() {}
    
    /**
     * Конструктор з усіма параметрами
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
     * Створює модель карточки з доменної сутності задачі
     * 
     * @param issue доменна сутність задачі
     * @return модель карточки
     */
    public static IssueCardModel fromIssue(Issue issue) {
        if (issue == null) {
            return null;
        }
        
        IssueCardModel model = new IssueCardModel();
        model.setId(issue.getId());
        
        // Отримуємо ресурси локалізації
        java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("i18n.labels", new java.util.Locale("uk", "UA"));
        
        try {
            // Використовуємо ідентифікатор задачі в форматі PROJECT-ID якщо немає ключа
            // або не вдається отримати ключ з моделі
            model.setKey("ISSUE-" + issue.getId());
        } catch (Exception e) {
            model.setKey("ISSUE-" + issue.getId());
        }
        
        try {
            model.setTitle(issue.getTitle());
        } catch (Exception e) {
            try {
                // Якщо немає методу getTitle, спробуємо getSummary (для сумісності)
                model.setTitle(messages.getString("issue.default_title") + issue.getId());
            } catch (Exception ex) {
                model.setTitle(messages.getString("issue.default_title") + issue.getId());
            }
        }
        
        // Улучшаємо обробку опису задачі з детальним логуванням
        try {
            String description = issue.getDescription();
            System.out.println("IssueCardModel.fromIssue: ID=" + issue.getId() + 
                ", опис=" + (description != null ? description.substring(0, Math.min(30, description.length())) + "..." : "null"));
            
            model.setDescription(description);
            
            // Перевірка, що опис був встановлений
            System.out.println("IssueCardModel.fromIssue: Після встановлення, опис в моделі=" + 
                (model.getDescription() != null ? model.getDescription().substring(0, Math.min(30, model.getDescription().length())) + "..." : "null"));
        } catch (Exception e) {
            System.err.println("IssueCardModel.fromIssue: Помилка при встановленні опису: " + e.getMessage());
            e.printStackTrace();
            // Не встановлюємо жодне значення за замовчуванням, залишаємо null або пусту строку
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
            // Спробуємо отримати приоритет, якщо в доменній моделі немає методу
            // getPriority, використовуємо середній приоритет за замовчуванням
            model.setPriority(Priority.MEDIUM);
        } catch (Exception e) {
            model.setPriority(Priority.MEDIUM);
        }
        
        if (issue.getAssignee() != null) {
            try {
                model.setAssigneeName(issue.getAssignee().getFullName());
                System.out.println("IssueCardModel.fromIssue: ID=" + issue.getId() + 
                    ", встановлено assigneeName=" + model.getAssigneeName());
            } catch (Exception e) {
                try {
                    model.setAssigneeName(issue.getAssignee().getUsername());
                    System.out.println("IssueCardModel.fromIssue: ID=" + issue.getId() + 
                        ", встановлено assigneeName з username=" + model.getAssigneeName());
                } catch (Exception ex) {
                    model.setAssigneeName(messages.getString("user.unknown"));
                    System.err.println("IssueCardModel.fromIssue: Помилка при отриманні імені користувача: " + ex.getMessage());
                }
            }
        } else {
            System.out.println("IssueCardModel.fromIssue: ID=" + issue.getId() + ", assignee=null");
            // Замість встановлення null, встановлюємо текст "не призначено" з ресурсів
            model.setAssigneeName(messages.getString("issue.not_assigned"));
            System.out.println("IssueCardModel.fromIssue: встановлено значення 'не призначено' замість null");
        }
        
        return model;
    }
    
    // Геттери і сеттери
    
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