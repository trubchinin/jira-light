package ua.oip.jiralite.repository;

import java.util.List;

import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;

/**
 * Репозиторий для работы с пользователями
 */
public interface UserRepository {
    
    /**
     * Сохраняет пользователя
     */
    User save(User user);
    
    /**
     * Находит пользователя по ID
     */
    User findById(Long id);
    
    /**
     * Находит пользователя по логину
     */
    User findByLogin(String login);
    
    /**
     * Находит пользователя по email
     */
    User findByEmail(String email);
    
    /**
     * Находит всех пользователей проекта
     */
    List<User> findByProjectsContaining(Project project);
    
    /**
     * Проверяет существование пользователя по логину
     */
    boolean existsByLogin(String login);
    
    /**
     * Проверяет существование пользователя по email
     */
    boolean existsByEmail(String email);
    
    /**
     * Удаляет пользователя
     */
    void delete(User user);
} 