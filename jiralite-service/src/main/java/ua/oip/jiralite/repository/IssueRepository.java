package ua.oip.jiralite.repository;

import java.util.List;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;

/**
 * Репозиторий для работы с задачами
 */
public interface IssueRepository {
    
    /**
     * Сохраняет задачу
     */
    Issue save(Issue issue);
    
    /**
     * Находит задачу по ID
     */
    Issue findById(Long id);
    
    /**
     * Находит все задачи доски
     */
    List<Issue> findByBoard(Board board);
    
    /**
     * Находит все задачи проекта
     */
    List<Issue> findByProject(Project project);
    
    /**
     * Находит все задачи, назначенные пользователю
     */
    List<Issue> findByAssignee(User assignee);
    
    /**
     * Удаляет задачу
     */
    void delete(Issue issue);
} 