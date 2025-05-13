package ua.oip.jiralite.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Проєкт містить дошки, задачі та учасників.
 */
public class Project extends BaseEntity {

    private String name;
    private String key;
    private String description;
    private User owner;
    private final List<Board> boards = new ArrayList<>();
    private final List<User> members = new ArrayList<>();
    private final List<Issue> issues = new ArrayList<>();

    /**
     * Конструктор для створення нового проекту
     */
    public Project(String name) {
        super();
        this.name = name;
    }

    /**
     * Розширений конструктор для створення нового проекту
     */
    public Project(String name, String key, String description) {
        super();
        this.name = name;
        this.key = key;
        this.description = description;
    }

    /**
     * Конструктор для проекту з власником
     */
    public Project(String name, User owner) {
        this(name);
        this.owner = owner;
        this.members.add(owner);
    }

    // ── Гетери/сетери ────────────────────────────────────────────
    public String getName() { return name; }
    public void setName(String name) { this.name = name; touch(); }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; touch(); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; touch(); }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; touch(); }

    // ── API для дошок ────────────────────────────────────────────────────────
    public void addBoard(Board board) {
        boards.add(board);
        board.setProject(this);
        touch();
    }

    public void removeBoard(Board board) {
        boards.remove(board);
        board.setProject(null);
        touch();
    }

    public List<Board> getBoards() { return Collections.unmodifiableList(boards); }

    // ── API для задач ────────────────────────────────────────────────────────
    public void addIssue(Issue issue) {
        issues.add(issue);
        issue.setProject(this);
        touch();
    }

    public void removeIssue(Issue issue) {
        issues.remove(issue);
        issue.setProject(null);
        touch();
    }

    public List<Issue> getIssues() { return Collections.unmodifiableList(issues); }

    // ── API для учасників ────────────────────────────────────────────────────
    public void addMember(User user) {
        if (!members.contains(user)) {
            members.add(user);
            touch();
        }
    }

    public void removeMember(User user) {
        if (user != owner) {  // Не можна видалити власника проекту
            members.remove(user);
            touch();
        }
    }

    public List<User> getMembers() { return Collections.unmodifiableList(members); }
} 