package ua.oip.jiralite.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;

/**
 * Задача (Issue) на дошці.
 */
public class Issue extends BaseEntity {

    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private User reporter;
    private User assignee;
    private Project project;
    private BoardColumn boardColumn;
    private final List<Comment> comments = new ArrayList<>();

    /**
     * Конструктор для створення нової задачі
     */
    public Issue(String title, String description, User reporter, Project project, Status status, Priority priority) {
        super();
        this.title = title;
        this.description = description;
        this.reporter = reporter;
        this.project = project;
        this.status = status;
        this.priority = priority;
    }
    
    /**
     * Конструктор для тестів
     */
    public Issue(String title, String description, Status status, Priority priority, Project project, User reporter) {
        super();
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.project = project;
        this.reporter = reporter;
    }

    // ── Гетери / сетери ──────────────────────────────────────────────────────
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; touch(); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; touch(); }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; touch(); }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; touch(); }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; touch(); }

    public User getAssignee() { return assignee; }
    public void setAssignee(User assignee) { this.assignee = assignee; touch(); }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; touch(); }

    public BoardColumn getBoardColumn() { return boardColumn; }
    public void setBoardColumn(BoardColumn boardColumn) { this.boardColumn = boardColumn; touch(); }

    public List<Comment> getComments() { return Collections.unmodifiableList(comments); }

    /**
     * Додати коментар до задачі
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setIssue(this);
        touch();
    }

    /**
     * Видалити коментар з задачі
     */
    public boolean removeComment(Comment comment) {
        boolean removed = comments.remove(comment);
        if (removed) {
            comment.setIssue(null);
            touch();
        }
        return removed;
    }

    /**
     * Зміна статусу задачі з перевіркою допустимості переходу
     */
    public boolean changeStatus(Status newStatus) {
        // Перевірка допустимих переходів
        if (status == Status.TO_DO) {
            if (newStatus == Status.IN_PROGRESS) {
                status = newStatus;
                touch();
                return true;
            }
        } else if (status == Status.IN_PROGRESS) {
            if (newStatus == Status.DONE || newStatus == Status.TO_DO || newStatus == Status.IN_REVIEW) {
                status = newStatus;
                touch();
                return true;
            }
        } else if (status == Status.IN_REVIEW) {
            if (newStatus == Status.IN_PROGRESS || newStatus == Status.DONE) {
                status = newStatus;
                touch();
                return true;
            }
        } else if (status == Status.DONE) {
            if (newStatus == Status.IN_PROGRESS) {
                status = newStatus;
                touch();
                return true;
            }
        }
        
        // Якщо код досяг цього місця, перехід не допустимий
        return false;
    }
} 