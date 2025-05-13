package ua.oip.jiralite.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "boards")
public class Board extends BaseEntity {
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardColumn> columns = new ArrayList<>();
    
    public Board() {
        // Ініціалізуємо стандартні колонки
        addColumn(new BoardColumn("To Do", "Tasks that need to be completed"));
        addColumn(new BoardColumn("In Progress", "Tasks that are currently being worked on"));
        addColumn(new BoardColumn("Done", "Completed tasks"));
    }
    
    public Board(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public List<BoardColumn> getColumns() {
        return columns;
    }
    
    public void setColumns(List<BoardColumn> columns) {
        this.columns = columns;
    }
    
    public void addColumn(BoardColumn column) {
        columns.add(column);
        column.setBoard(this);
    }
    
    public void removeColumn(BoardColumn column) {
        columns.remove(column);
        column.setBoard(null);
    }
    
    public BoardColumn findColumn(String name) {
        return columns.stream()
                .filter(column -> column.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
} 