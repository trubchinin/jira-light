package ua.oip.jiralite.ui;

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
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.service.AuthService.AuthException;

/**
 * Тест для перевірки логіки автентифікації
 */
public class SimpleLoginTest {
    
    private AuthService authService;
    
    @BeforeEach
    public void setUp() {
        authService = AuthService.getInstance();
        // Скидаємо поточного користувача перед кожним тестом
        authService.logout();
    }
    
    @Test
    @DisplayName("Має успішно автентифікувати адміністратора")
    public void testAdminLogin() throws AuthException {
        // Коли
        User user = authService.signIn("admin", "qwerty");
        
        // Тоді
        assertNotNull(user, "Користувач не має бути null");
        assertEquals("admin", user.getUsername(), "Ім'я користувача має бути 'admin'");
        assertEquals(Role.ADMIN, user.getRole(), "Роль користувача має бути ADMIN");
        assertEquals(user, authService.getCurrentUser(), "CurrentUser має бути рівним автентифікованому користувачу");
    }
    
    @Test
    @DisplayName("Має успішно автентифікувати звичайного користувача")
    public void testUserLogin() throws AuthException {
        // Коли
        User user = authService.signIn("john", "1234");
        
        // Тоді
        assertNotNull(user, "Користувач не має бути null");
        assertEquals("john", user.getUsername(), "Ім'я користувача має бути 'john'");
        assertEquals(Role.USER, user.getRole(), "Роль користувача має бути USER");
        assertEquals(user, authService.getCurrentUser(), "CurrentUser має бути рівним автентифікованому користувачу");
    }
    
    @Test
    @DisplayName("Має успішно автентифікувати гостя")
    public void testGuestLogin() throws AuthException {
        // Коли
        User user = authService.signIn("guest", "guest");
        
        // Тоді
        assertNotNull(user, "Користувач не має бути null");
        assertEquals("guest", user.getUsername(), "Ім'я користувача має бути 'guest'");
        assertEquals(Role.GUEST, user.getRole(), "Роль користувача має бути GUEST");
        assertEquals(user, authService.getCurrentUser(), "CurrentUser має бути рівним автентифікованому користувачу");
    }
    
    @Test
    @DisplayName("Має викидати виняток при невірних облікових даних")
    public void testInvalidLogin() {
        // Тоді
        Exception exception = assertThrows(AuthException.class, () -> {
            // Коли
            authService.signIn("wronguser", "wrongpass");
        });
        
        assertNotNull(exception.getMessage(), "Повідомлення про помилку не має бути null");
        assertTrue(exception.getMessage().contains("Невірний логін або пароль"), 
                "Повідомлення про помилку має містити інформацію про невірний логін або пароль");
    }
    
    @Test
    @DisplayName("Має коректно розлогінювати користувача")
    public void testLogout() throws AuthException {
        // Дано
        authService.signIn("admin", "qwerty");
        assertNotNull(authService.getCurrentUser(), "Користувач має бути автентифікований");
        
        // Коли
        authService.logout();
        
        // Тоді
        assertNull(authService.getCurrentUser(), "Після виходу currentUser має бути null");
    }
    
    @Test
    @DisplayName("Має правильно визначати права користувачів")
    public void testUserPermissions() throws AuthException {
        // Адміністратор
        authService.signIn("admin", "qwerty");
        assertTrue(authService.canCreateIssue(), "Адміністратор має мати право створювати задачі");
        assertTrue(authService.canEditIssue(), "Адміністратор має мати право редагувати задачі");
        assertTrue(authService.canDeleteIssue(), "Адміністратор має мати право видаляти задачі");
        
        // Звичайний користувач
        authService.logout();
        authService.signIn("john", "1234");
        assertTrue(authService.canCreateIssue(), "Користувач має мати право створювати задачі");
        assertTrue(authService.canEditIssue(), "Користувач має мати право редагувати задачі");
        assertTrue(authService.canDeleteIssue(), "Користувач має мати право видаляти задачі");
        
        // Гість
        authService.logout();
        authService.signIn("guest", "guest");
        assertFalse(authService.canCreateIssue(), "Гість не має мати права створювати задачі");
        assertFalse(authService.canEditIssue(), "Гість не має мати права редагувати задачі");
        assertFalse(authService.canDeleteIssue(), "Гість не має мати права видаляти задачі");
    }
} 