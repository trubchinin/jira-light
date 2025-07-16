package ua.oip.jiralite.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Role;
import ua.oip.jiralite.service.AuthService.AuthException;

/**
 * Модульні тести для AuthService
 */
public class AuthServiceTest {
    
    private AuthService authService;
    
    @BeforeEach
    public void setUp() {
        // Отримуємо синглтон-екземпляр AuthService
        authService = AuthService.getInstance();
        // Скидаємо поточного користувача перед кожним тестом
        authService.logout();
    }
    
    @Test
    @DisplayName("Автентифікація адміністратора має бути успішною")
    public void testAdminAuthentication() throws AuthException {
        // Коли
        User admin = authService.signIn("admin", "qwerty");
        
        // Тоді
        assertNotNull(admin, "Адміністратор має бути успішно автентифікований");
        assertEquals("admin", admin.getUsername(), "Ім'я користувача має збігатися");
        assertEquals(Role.ADMIN, admin.getRole(), "Роль має бути ADMIN");
        assertEquals(admin, authService.getCurrentUser(), "Поточний користувач має бути адміністратором");
    }
    
    @Test
    @DisplayName("Автентифікація звичайного користувача має бути успішною")
    public void testUserAuthentication() throws AuthException {
        // Коли
        User user = authService.signIn("john", "1234");
        
        // Тоді
        assertNotNull(user, "Користувач має бути успішно автентифікований");
        assertEquals("john", user.getUsername(), "Ім'я користувача має збігатися");
        assertEquals(Role.USER, user.getRole(), "Роль має бути USER");
        assertEquals(user, authService.getCurrentUser(), "Поточний користувач має бути user");
    }
    
    @Test
    @DisplayName("Автентифікація гостя має бути успішною")
    public void testGuestAuthentication() throws AuthException {
        // Коли
        User guest = authService.signIn("guest", "guest");
        
        // Тоді
        assertNotNull(guest, "Гість має бути успішно автентифікований");
        assertEquals("guest", guest.getUsername(), "Ім'я користувача має збігатися");
        assertEquals(Role.GUEST, guest.getRole(), "Роль має бути GUEST");
        assertEquals(guest, authService.getCurrentUser(), "Поточний користувач має бути guest");
    }
    
    @Test
    @DisplayName("Автентифікація з невірними обліковими даними має викликати виняток")
    public void testInvalidAuthentication() {
        // Тоді
        Exception exception = assertThrows(AuthException.class, () -> {
            // Коли
            authService.signIn("wronguser", "wrongpass");
        });
        
        assertTrue(exception.getMessage().contains("Невірний логін або пароль"), 
                "Повідомлення про помилку має містити інформацію про невірний логін або пароль");
        assertNull(authService.getCurrentUser(), "Поточний користувач має залишитися null");
    }
    
    @Test
    @DisplayName("Вихід із системи має скидати поточного користувача")
    public void testLogout() throws AuthException {
        // Дано
        authService.signIn("admin", "qwerty");
        assertNotNull(authService.getCurrentUser(), "Користувач має бути автентифікований");
        
        // Коли
        authService.logout();
        
        // Тоді
        assertNull(authService.getCurrentUser(), "Поточний користувач має бути null після виходу");
    }
    
    @Test
    @DisplayName("Адміністратор має мати всі права")
    public void testAdminPermissions() throws AuthException {
        // Дано
        authService.signIn("admin", "qwerty");
        
        // Тоді
        assertTrue(authService.canCreateIssue(), "Адміністратор має мати право створювати задачі");
        assertTrue(authService.canEditIssue(), "Адміністратор має мати право редагувати задачі");
        assertTrue(authService.canDeleteIssue(), "Адміністратор має мати право видаляти задачі");
    }
    
    @Test
    @DisplayName("Звичайний користувач має мати обмежені права")
    public void testUserPermissions() throws AuthException {
        // Дано
        authService.signIn("john", "1234");
        
        // Тоді
        assertTrue(authService.canCreateIssue(), "Користувач має мати право створювати задачі");
        assertTrue(authService.canEditIssue(), "Користувач має мати право редагувати задачі");
        assertTrue(authService.canDeleteIssue(), "Користувач має мати право видаляти задачі");
    }
    
    @Test
    @DisplayName("Гість не має мати прав на зміну")
    public void testGuestPermissions() throws AuthException {
        // Дано
        authService.signIn("guest", "guest");
        
        // Тоді
        assertFalse(authService.canCreateIssue(), "Гість не має мати право створювати задачі");
        assertFalse(authService.canEditIssue(), "Гість не має мати право редагувати задачі");
        assertFalse(authService.canDeleteIssue(), "Гість не має мати право видаляти задачі");
    }
    
    @Test
    @DisplayName("Автентифікація з порожнім логіном має викликати виняток")
    public void testEmptyUsernameAuthentication() {
        // Тоді
        Exception exception = assertThrows(AuthException.class, () -> {
            // Коли
            authService.signIn("", "password");
        });
        
        assertTrue(exception.getMessage().contains("Невірний логін або пароль"), 
                "Повідомлення про помилку має містити інформацію про невірний логін");
        assertNull(authService.getCurrentUser(), "Поточний користувач має залишитися null");
    }
    
    @Test
    @DisplayName("Автентифікація з null логіном має викликати виняток")
    public void testNullUsernameAuthentication() {
        // Тоді
        Exception exception = assertThrows(AuthException.class, () -> {
            // Коли
            authService.signIn(null, "password");
        });
        
        assertTrue(exception.getMessage().contains("Невірний логін або пароль"), 
                "Повідомлення про помилку має містити інформацію про невірний логін");
        assertNull(authService.getCurrentUser(), "Поточний користувач має залишитися null");
    }
    
    @Test
    @DisplayName("Автентифікація з порожнім паролем має викликати виняток")
    public void testEmptyPasswordAuthentication() {
        // Тоді
        Exception exception = assertThrows(AuthException.class, () -> {
            // Коли
            authService.signIn("admin", "");
        });
        
        assertTrue(exception.getMessage().contains("Невірний логін або пароль"), 
                "Повідомлення про помилку має містити інформацію про невірний пароль");
        assertNull(authService.getCurrentUser(), "Поточний користувач має залишитися null");
    }
    
    @Test
    @DisplayName("Повторний вихід із системи має бути безпечним")
    public void testMultipleLogout() throws AuthException {
        // Дано
        authService.signIn("admin", "qwerty");
        authService.logout();
        
        // Коли - повторний logout
        authService.logout();
        
        // Тоді
        assertNull(authService.getCurrentUser(), "Поточний користувач має залишитися null");
    }
    
    @Test
    @DisplayName("Перевірка прав без автентифікації має повертати false")
    public void testPermissionsWithoutAuthentication() {
        // Тоді
        assertFalse(authService.canCreateIssue(), "Неавтентифікований користувач не має мати права створювати задачі");
        assertFalse(authService.canEditIssue(), "Неавтентифікований користувач не має мати права редагувати задачі");
        assertFalse(authService.canDeleteIssue(), "Неавтентифікований користувач не має мати права видаляти задачі");
    }
} 