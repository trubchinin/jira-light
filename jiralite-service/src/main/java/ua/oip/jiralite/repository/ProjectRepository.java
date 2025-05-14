package ua.oip.jiralite.repository;

import java.util.List;

import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;

/**
 * Репозиторий для работы с проектами
 */
public interface ProjectRepository {
    
    /**
     * Сохраняет проект
     */
    Project save(Project project);
    
    /**
     * Находит проект по ID
     */
    Project findById(Long id);
    
    /**
     * Находит проект по ключу
     */
    Project findByKey(String key);
    
    /**
     * Находит все проекты пользователя
     */
    List<Project> findByMembersContaining(User user);
    
    /**
     * Удаляет проект
     */
    void delete(Project project);
} 