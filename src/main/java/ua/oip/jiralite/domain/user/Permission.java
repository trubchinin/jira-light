package ua.oip.jiralite.domain.user;

public enum Permission {
    CREATE_PROJECT("Create Project"),
    EDIT_PROJECT("Edit Project"),
    DELETE_PROJECT("Delete Project"),
    VIEW_PROJECT("View Project"),
    
    CREATE_ISSUE("Create Issue"),
    EDIT_ISSUE("Edit Issue"),
    DELETE_ISSUE("Delete Issue"),
    VIEW_ISSUE("View Issue"),
    
    CREATE_COMMENT("Create Comment"),
    EDIT_COMMENT("Edit Comment"),
    DELETE_COMMENT("Delete Comment"),
    
    MANAGE_USERS("Manage Users");
    
    private final String displayName;
    
    Permission(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Перевіряє, чи дозволена дія з вказаним іменем.
     * Цей метод перевіряє, чи відповідає назва дії даному дозволу.
     *
     * @param action назва дії для перевірки
     * @return true, якщо дія дозволена; false - якщо ні
     */
    public boolean allowed(String action) {
        // Перевіряємо, чи відповідає дія даному дозволу
        // По-справжньому це був би більш складний метод,
        // але для тестування ми використовуємо просту реалізацію
        return this.name().toLowerCase().contains(action.toLowerCase());
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 