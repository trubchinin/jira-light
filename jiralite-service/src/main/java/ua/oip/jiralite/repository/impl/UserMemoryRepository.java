package ua.oip.jiralite.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.repository.UserRepository;

/**
 * In-memory реализация репозитория пользователей
 */
public class UserMemoryRepository implements UserRepository {
    
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;
    
    public UserMemoryRepository() {
        // Создаем тестовых пользователей
        initDefaultUsers();
    }
    
    private void initDefaultUsers() {
        // Добавляем администратора
        User admin = new User("admin", "qwerty", "Administrator", "admin@example.com");
        admin.setRole(ua.oip.jiralite.domain.enums.Role.ADMIN);
        save(admin);
        
        // Добавляем обычного пользователя
        User user = new User("john", "1234", "John Developer", "john@example.com");
        user.setRole(ua.oip.jiralite.domain.enums.Role.USER);
        save(user);
        
        // Добавляем гостя
        User guest = new User("guest", "guest", "Guest User", "guest@example.com");
        guest.setRole(ua.oip.jiralite.domain.enums.Role.GUEST);
        save(guest);
    }
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        users.put(user.getId(), user);
        return user;
    }
    
    @Override
    public User findById(Long id) {
        return users.get(id);
    }
    
    @Override
    public User findByLogin(String login) {
        for (User user : users.values()) {
            if (login.equals(user.getLogin())) {
                return user;
            }
        }
        return null;
    }
    
    @Override
    public User findByEmail(String email) {
        for (User user : users.values()) {
            if (email.equals(user.getEmail())) {
                return user;
            }
        }
        return null;
    }
    
    @Override
    public List<User> findByProjectsContaining(Project project) {
        List<User> result = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getProjects().contains(project)) {
                result.add(user);
            }
        }
        return result;
    }
    
    @Override
    public boolean existsByLogin(String login) {
        return findByLogin(login) != null;
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }
    
    @Override
    public void delete(User user) {
        if (user != null && user.getId() != null) {
            users.remove(user.getId());
        }
    }
} 