package ua.oip.jiralite.repository;

import java.util.List;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Project;

/**
 * Репозиторий для работы с досками
 */
public interface BoardRepository {
    
    /**
     * Сохраняет доску
     */
    Board save(Board board);
    
    /**
     * Находит доску по ID
     */
    Board findById(Long id);
    
    /**
     * Находит все доски проекта
     */
    List<Board> findByProject(Project project);
    
    /**
     * Удаляет доску
     */
    void delete(Board board);
} 