package ua.oip.jiralite.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Дошка (Kanban) із колонками.
 */
public class Board extends BaseEntity {

    private String name;
    private String description;
    private final List<BoardColumn> columns = new ArrayList<>();
    private Project project;
    
    /**
     * Створює нову дошку зі стандартними колонками
     */
    public Board() {
        super();
    }
    
    /**
     * Створює нову дошку з назвою та описом
     */
    public Board(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }
    
    public Board(Project project) {
        super();
        this.project = project;
    }

    // ── Гетери і сетери ───────────────────────────────────────────────────────
    public List<BoardColumn> getColumns() { 
        return Collections.unmodifiableList(columns); 
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
        touch();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        touch();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        touch();
    }
    
    /**
     * Шукає колонку за назвою
     * @param name назва колонки
     * @return знайдена колонка або null, якщо не знайдено
     */
    public BoardColumn findColumn(String name) {
        return columns.stream()
                .filter(col -> col.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Додати нову колонку до дошки
     * @param column колонка для додавання
     */
    public void addColumn(BoardColumn column) {
        columns.add(column);
        column.setBoard(this);
        touch();
    }
    
    /**
     * Видалити колонку з дошки
     * @param column колонка для видалення
     */
    public void removeColumn(BoardColumn column) {
        columns.remove(column);
        column.setBoard(null);
        touch();
    }
    
    // ── Додавання / переміщення задач ────────────────────────────────────────
    public void addIssue(Issue issue, String columnName) {
        BoardColumn column = findColumn(columnName);
        if (column != null) {
            column.addIssue(issue);
            touch();
        }
    }
    
    public void moveIssue(Issue issue, String fromColumnName, String toColumnName) {
        BoardColumn fromColumn = findColumn(fromColumnName);
        BoardColumn toColumn = findColumn(toColumnName);
        
        if (fromColumn != null && toColumn != null) {
            fromColumn.removeIssue(issue);
            toColumn.addIssue(issue);
            touch();
        }
    }
} 