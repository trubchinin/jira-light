package ua.oip.jiralite.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import ua.oip.jiralite.domain.user.User;

@Entity
@Table(name = "projects")
public class Project extends BaseEntity {
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "key", nullable = false, unique = true)
    private String key;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Issue> issues = new ArrayList<>();
    
    public Project() {
        // Створюємо дошку за замовчуванням при створенні проекту
        Board defaultBoard = new Board("Default Board", "Default project board");
        addBoard(defaultBoard);
    }
    
    public Project(String name, String key, String description) {
        this();
        this.name = name;
        this.key = key;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Set<User> getMembers() {
        return members;
    }
    
    public void setMembers(Set<User> members) {
        this.members = members;
    }
    
    public void addMember(User user) {
        members.add(user);
        user.getProjects().add(this);
    }
    
    public void removeMember(User user) {
        members.remove(user);
        user.getProjects().remove(this);
    }
    
    public List<Board> getBoards() {
        return boards;
    }
    
    // Метод для отримання дошки (дошка за замовчуванням знаходиться першою в списку)
    public Board getBoard() {
        if (boards.isEmpty()) {
            return null;
        }
        return boards.get(0);
    }
    
    public void setBoards(List<Board> boards) {
        this.boards = boards;
    }
    
    public void addBoard(Board board) {
        boards.add(board);
        board.setProject(this);
    }
    
    public void removeBoard(Board board) {
        boards.remove(board);
        board.setProject(null);
    }
    
    public List<Issue> getIssues() {
        return issues;
    }
    
    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }
    
    public void addIssue(Issue issue) {
        issues.add(issue);
        issue.setProject(this);
    }
    
    public void removeIssue(Issue issue) {
        issues.remove(issue);
        issue.setProject(null);
    }
} 