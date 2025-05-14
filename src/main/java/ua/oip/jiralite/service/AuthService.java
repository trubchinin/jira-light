package ua.oip.jiralite.service;

import java.util.Optional;
import java.time.LocalDateTime;

import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Role;
import ua.oip.jiralite.domain.user.Permission;
import ua.oip.jiralite.domain.user.RoleManager;
import ua.oip.jiralite.repository.UserRepository;

/**
 * Сервис аутентификации пользователей
 */
public class AuthService {
    
    private static AuthService instance;
    private final UserRepository userRepository;
    private User currentUser;
    
    /**
     * Возвращает Singleton экземпляр сервиса
     */
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Конструктор для внедрения зависимостей
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Конструктор для Singleton
     */
    private AuthService() {
        this.userRepository = null; // В реальном приложении здесь была бы инициализация репозитория
    }
    
    /**
     * Аутентификация пользователя
     * 
     * @param login логин
     * @param password пароль
     * @return аутентифицированный пользователь
     * @throws AuthException если аутентификация не удалась
     */
    public User signIn(String login, String password) throws AuthException {
        if (login == null || login.trim().isEmpty()) {
            throw new AuthException("Логін не може бути порожнім");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new AuthException("Пароль не може бути порожнім");
        }
        
        // Для тестового режима без репозитория
        if (userRepository == null) {
            // Администратор
            if ("admin".equals(login) && "qwerty".equals(password)) {
                User admin = new User(login, password, "Administrator", "admin@example.com");
                admin.setId(1L);
                admin.setRole(Role.ADMIN);
                currentUser = admin;
                return admin;
            }
            
            // Зарегистрированный пользователь
            if ("john".equals(login) && "1234".equals(password)) {
                User user = new User(login, password, "John Developer", "john@example.com");
                user.setId(2L);
                user.setRole(Role.USER);
                currentUser = user;
                return user;
            }
            
            // Гость с ограниченным доступом
            if ("guest".equals(login) && "guest".equals(password)) {
                User guest = new User(login, password, "Guest User", "guest@example.com");
                guest.setId(3L);
                guest.setRole(Role.GUEST);
                currentUser = guest;
                return guest;
            }
            
            // Ошибка аутентификации
            throw new AuthException("Невірний логін або пароль");
        }
        
        // Для реального репозитория
        if (userRepository != null) {
            User user = userRepository.findByLogin(login);
            
            if (user != null && password.equals(user.getPassword())) {
                // Обновляем время последнего входа
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                
                // Устанавливаем текущего пользователя
                currentUser = user;
                return user;
            }
        }
        
        throw new AuthException("Невірний логін або пароль");
    }
    
    /**
     * Метод authenticate для совместимости с LoginAction
     * 
     * @param username имя пользователя
     * @param password пароль
     * @return пользователь или null, если аутентификация не удалась
     */
    public User authenticate(String username, String password) {
        try {
            return signIn(username, password);
        } catch (AuthException e) {
            return null;
        }
    }
    
    /**
     * Возвращает текущего пользователя
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Определяет роль текущего пользователя
     */
    public Role getUserRole() {
        if (currentUser == null) {
            return null;
        }
        
        return currentUser.getRole();
    }
    
    /**
     * Выход пользователя из системы
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Проверяет, имеет ли текущий пользователь права на изменение задачи
     */
    public boolean canEditIssue() {
        if (currentUser == null) {
            return false;
        }
        return currentUser.hasPermission(Permission.EDIT_ISSUE);
    }
    
    /**
     * Проверяет, имеет ли текущий пользователь права на добавление новых задач
     */
    public boolean canCreateIssue() {
        if (currentUser == null) {
            return false;
        }
        return currentUser.hasPermission(Permission.CREATE_ISSUE);
    }
    
    /**
     * Проверяет, имеет ли текущий пользователь права на просмотр задач
     */
    public boolean canViewIssue() {
        if (currentUser == null) {
            return false;
        }
        return currentUser.hasPermission(Permission.VIEW_ISSUE);
    }
    
    /**
     * Проверяет, имеет ли текущий пользователь права на удаление задач
     */
    public boolean canDeleteIssue() {
        if (currentUser == null) {
            return false;
        }
        return currentUser.hasPermission(Permission.DELETE_ISSUE);
    }
    
    /**
     * Исключение при ошибке аутентификации
     */
    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
} 