package ua.oip.jiralite.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "board_columns")
public class BoardColumn extends BaseEntity {
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "position")
    private Integer position;
    
    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;
    
    @OneToMany(mappedBy = "boardColumn")
    private List<Issue> issues = new ArrayList<>();
    
    public BoardColumn() {
    }
    
    public BoardColumn(String name, Integer position) {
        this.name = name;
        this.position = position;
    }
    
    public BoardColumn(String name, String description) {
        this.name = name;
        // Номер позиції за замовчуванням
        this.position = 0;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getPosition() {
        return position;
    }
    
    public void setPosition(Integer position) {
        this.position = position;
    }
    
    public Board getBoard() {
        return board;
    }
    
    public void setBoard(Board board) {
        this.board = board;
    }
    
    public List<Issue> getIssues() {
        return issues;
    }
    
    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }
    
    public void addIssue(Issue issue) {
        issues.add(issue);
        issue.setBoardColumn(this);
    }
    
    public void removeIssue(Issue issue) {
        issues.remove(issue);
        issue.setBoardColumn(null);
    }
} 