package ua.oip.jiralite.domain.user;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Менеджер ролей и прав пользователей.
 * Связывает класс Role с набором разрешений Permission.
 */
public class RoleManager {
    
    private static final Map<String, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();
    
    static {
        // Инициализация прав для роли ADMIN
        Set<Permission> adminPermissions = EnumSet.allOf(Permission.class);
        ROLE_PERMISSIONS.put("ADMIN", adminPermissions);
        
        // Инициализация прав для роли USER
        Set<Permission> userPermissions = EnumSet.of(
            Permission.VIEW_PROJECT,
            Permission.CREATE_ISSUE,
            Permission.EDIT_ISSUE,
            Permission.VIEW_ISSUE,
            Permission.CREATE_COMMENT,
            Permission.EDIT_COMMENT,
            Permission.DELETE_COMMENT
        );
        ROLE_PERMISSIONS.put("USER", userPermissions);
        
        // Инициализация прав для роли GUEST
        Set<Permission> guestPermissions = EnumSet.of(
            Permission.VIEW_PROJECT,
            Permission.VIEW_ISSUE
        );
        ROLE_PERMISSIONS.put("GUEST", guestPermissions);
    }
    
    /**
     * Проверяет, есть ли у роли указанное разрешение
     * 
     * @param roleName название роли пользователя
     * @param permission разрешение для проверки
     * @return true, если роль имеет указанное разрешение
     */
    public static boolean hasPermission(String roleName, Permission permission) {
        if (roleName == null || permission == null) {
            return false;
        }
        
        Set<Permission> permissions = ROLE_PERMISSIONS.get(roleName);
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Получает все разрешения для указанной роли
     * 
     * @param roleName название роли пользователя
     * @return набор разрешений для роли
     */
    public static Set<Permission> getPermissions(String roleName) {
        if (roleName == null) {
            return EnumSet.noneOf(Permission.class);
        }
        
        Set<Permission> permissions = ROLE_PERMISSIONS.get(roleName);
        return permissions != null ? EnumSet.copyOf(permissions) : EnumSet.noneOf(Permission.class);
    }
    
    /**
     * Проверяет, доступно ли указанное действие для роли
     * 
     * @param roleName название роли пользователя
     * @param action действие для проверки
     * @return true, если действие доступно для роли
     */
    public static boolean isActionAllowed(String roleName, String action) {
        if (roleName == null || action == null || action.isEmpty()) {
            return false;
        }
        
        Set<Permission> permissions = ROLE_PERMISSIONS.get(roleName);
        if (permissions == null) {
            return false;
        }
        
        // Проверяем все разрешения роли
        for (Permission permission : permissions) {
            if (permission.allowed(action)) {
                return true;
            }
        }
        
        return false;
    }
} 