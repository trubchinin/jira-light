package ua.oip.jiralite.service;

import java.time.LocalDateTime;

import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Role;
import ua.oip.jiralite.repository.UserRepository;

/**
 * Сервіс автентифікації користувачів
 */
public class AuthService {
    
    private static AuthService instance;
    private final UserRepository userRepository;
    private User currentUser;
    
    /**
     * Повертає Singleton екземпляр сервісу
     */
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Конструктор для впровадження залежностей
     */
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Конструктор для Singleton
     */
    private AuthService() {
        this.userRepository = null; // У реальному застосунку тут була б ініціалізація репозиторію
    }
    
    /**
     * Автентифікація користувача
     * 
     * @param login логін
     * @param password пароль
     * @return автентифікований користувач
     * @throws AuthException якщо автентифікація не вдалася
     */
    public User signIn(String login, String password) throws AuthException {
        if (login == null || login.trim().isEmpty()) {
            throw new AuthException("Логін не може бути порожнім");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new AuthException("Пароль не може бути порожнім");
        }
        
        // Для тестового режиму без репозиторію
        if (userRepository == null) {
            // Адміністратор
            if ("admin".equals(login) && "qwerty".equals(password)) {
                User admin = new User(login, password, "Administrator", "admin@example.com");
                admin.setId(1L);
                admin.setRole(Role.ADMIN);
                currentUser = admin;
                return admin;
            }
            
            // Зареєстрований користувач
            if ("john".equals(login) && "1234".equals(password)) {
                User user = new User(login, password, "John Developer", "john@example.com");
                user.setId(2L);
                user.setRole(Role.USER);
                currentUser = user;
                return user;
            }
            
            // Гість з обмеженим доступом
            if ("guest".equals(login) && "guest".equals(password)) {
                User guest = new User(login, password, "Guest User", "guest@example.com");
                guest.setId(3L);
                guest.setRole(Role.GUEST);
                currentUser = guest;
                return guest;
            }
            
            // Помилка автентифікації
            throw new AuthException("Невірний логін або пароль");
        }
        
        // Для реального репозиторію
        if (userRepository != null) {
            User user = userRepository.findByLogin(login);
            
            if (user != null && password.equals(user.getPassword())) {
                // Оновлюємо час останнього входу
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                
                // Встановлюємо поточного користувача
                currentUser = user;
                return user;
            }
        }
        
        throw new AuthException("Невірний логін або пароль");
    }
    
    /**
     * Метод authenticate для сумісності з LoginAction
     * 
     * @param username ім'я користувача
     * @param password пароль
     * @return користувач або null, якщо автентифікація не вдалася
     */
    public User authenticate(String username, String password) {
        try {
            return signIn(username, password);
        } catch (AuthException e) {
            return null;
        }
    }
    
    /**
     * Повертає поточного користувача
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Визначає роль поточного користувача
     */
    public Role getUserRole() {
        if (currentUser == null) {
            return null;
        }
        
        return currentUser.getRole();
    }
    
    /**
     * Вихід користувача з системи
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Перевіряє, чи має поточний користувач права на зміну задачі
     */
    public boolean canEditIssue() {
        if (currentUser == null) {
            return false;
        }
        Role role = currentUser.getRole();
        return role == Role.ADMIN || role == Role.USER;
    }
    
    /**
     * Перевіряє, чи має поточний користувач права на додавання нових задач
     */
    public boolean canCreateIssue() {
        if (currentUser == null) {
            return false;
        }
        Role role = currentUser.getRole();
        return role == Role.ADMIN || role == Role.USER;
    }
    
    /**
     * Перевіряє, чи має поточний користувач права на видалення задач
     */
    public boolean canDeleteIssue() {
        if (currentUser == null) {
            return false;
        }
        Role role = currentUser.getRole();
        return role == Role.ADMIN || role == Role.USER;
    }
    
    /**
     * Виняток при помилці автентифікації
     */
    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}