package ua.oip.jiralite.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Role;

/**
 * Тесты системы ролей и разрешений
 */
public class RolePermissionTest {

    @Test
    public void testRoleManager() {
        // Проверка прав для администратора
        assertTrue(RoleManager.hasPermission("ADMIN", Permission.MANAGE_USERS));
        assertTrue(RoleManager.hasPermission("ADMIN", Permission.CREATE_PROJECT));
        assertTrue(RoleManager.hasPermission("ADMIN", Permission.DELETE_ISSUE));
        
        // Проверка прав для пользователя
        assertTrue(RoleManager.hasPermission("USER", Permission.CREATE_ISSUE));
        assertTrue(RoleManager.hasPermission("USER", Permission.VIEW_PROJECT));
        assertFalse(RoleManager.hasPermission("USER", Permission.MANAGE_USERS));
        
        // Проверка прав для гостя
        assertTrue(RoleManager.hasPermission("GUEST", Permission.VIEW_PROJECT));
        assertTrue(RoleManager.hasPermission("GUEST", Permission.VIEW_ISSUE));
        assertFalse(RoleManager.hasPermission("GUEST", Permission.CREATE_ISSUE));
        assertFalse(RoleManager.hasPermission("GUEST", Permission.EDIT_ISSUE));
    }
    
    @Test
    public void testUserPermissions() {
        // Создаем пользователей с разными ролями
        User admin = new User("admin", "admin@test.com", Role.ADMIN);
        User regularUser = new User("user", "user@test.com", Role.USER);
        User guest = new User("guest", "guest@test.com", Role.GUEST);
        
        // Проверяем права администратора
        assertTrue(admin.hasPermission(Permission.MANAGE_USERS));
        assertTrue(admin.hasPermission(Permission.CREATE_PROJECT));
        assertTrue(admin.hasPermission(Permission.DELETE_ISSUE));
        
        // Проверяем права обычного пользователя
        assertTrue(regularUser.hasPermission(Permission.CREATE_ISSUE));
        assertTrue(regularUser.hasPermission(Permission.VIEW_PROJECT));
        assertFalse(regularUser.hasPermission(Permission.MANAGE_USERS));
        
        // Проверяем права гостя
        assertTrue(guest.hasPermission(Permission.VIEW_PROJECT));
        assertTrue(guest.hasPermission(Permission.VIEW_ISSUE));
        assertFalse(guest.hasPermission(Permission.CREATE_ISSUE));
        assertFalse(guest.hasPermission(Permission.EDIT_ISSUE));
    }
    
    @Test
    public void testActionAllowed() {
        // Проверяем доступные действия для ролей
        assertTrue(RoleManager.isActionAllowed("ADMIN", "user_create"));
        assertTrue(RoleManager.isActionAllowed("ADMIN", "project_delete"));
        assertTrue(RoleManager.isActionAllowed("ADMIN", "issue_create"));
        
        assertTrue(RoleManager.isActionAllowed("USER", "issue_create"));
        assertFalse(RoleManager.isActionAllowed("USER", "user_create"));
        
        assertTrue(RoleManager.isActionAllowed("GUEST", "project_view"));
        assertFalse(RoleManager.isActionAllowed("GUEST", "issue_create"));
    }
    
    @Test
    public void testUserActionAllowed() {
        // Создаем пользователей с разными ролями
        User admin = new User("admin", "admin@test.com", Role.ADMIN);
        User regularUser = new User("user", "user@test.com", Role.USER);
        User guest = new User("guest", "guest@test.com", Role.GUEST);
        
        // Проверяем действия администратора
        assertTrue(admin.isActionAllowed("user_create"));
        assertTrue(admin.isActionAllowed("project_delete"));
        assertTrue(admin.isActionAllowed("issue_create"));
        
        // Проверяем действия обычного пользователя
        assertTrue(regularUser.isActionAllowed("issue_create"));
        assertFalse(regularUser.isActionAllowed("user_create"));
        
        // Проверяем действия гостя
        assertTrue(guest.isActionAllowed("project_view"));
        assertFalse(guest.isActionAllowed("issue_create"));
    }
} 