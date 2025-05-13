package ua.oip.jiralite.domain.user;

/**
 * Перелік дозволів (прав) у системі.
 */
public enum Permission {
    // Проектні права
    CREATE_PROJECT,
    EDIT_PROJECT,
    DELETE_PROJECT,
    VIEW_PROJECT,
    
    // Права для задач
    CREATE_ISSUE,
    EDIT_ISSUE,
    DELETE_ISSUE,
    VIEW_ISSUE,
    
    // Права для коментарів
    CREATE_COMMENT,
    EDIT_COMMENT,
    DELETE_COMMENT,
    
    // Адміністративні права
    MANAGE_USERS;
    
    /**
     * Перевіряє, чи дозволена певна дія.
     * Використовується для тестування.
     *
     * @param action дія, яку потрібно перевірити
     * @return true, якщо дія дозволена
     */
    public boolean allowed(String action) {
        // У реальному застосунку тут була б логіка перевірки прав
        // Для тестів цей метод буде перевизначатись у mock об'єктах
        return false;
    }
} 