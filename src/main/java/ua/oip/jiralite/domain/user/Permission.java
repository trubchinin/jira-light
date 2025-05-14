package ua.oip.jiralite.domain.user;

/**
 * Перелік дозволів (прав) у системі.
 */
public enum Permission {
    // Проектні права
    CREATE_PROJECT("create_project", "Створення проектів"),
    EDIT_PROJECT("edit_project", "Редагування проектів"),
    DELETE_PROJECT("delete_project", "Видалення проектів"),
    VIEW_PROJECT("view_project", "Перегляд проектів"),
    
    // Права для задач
    CREATE_ISSUE("create_issue", "Створення задач"),
    EDIT_ISSUE("edit_issue", "Редагування задач"),
    DELETE_ISSUE("delete_issue", "Видалення задач"),
    VIEW_ISSUE("view_issue", "Перегляд задач"),
    
    // Права для коментарів
    CREATE_COMMENT("create_comment", "Створення коментарів"),
    EDIT_COMMENT("edit_comment", "Редагування коментарів"),
    DELETE_COMMENT("delete_comment", "Видалення коментарів"),
    
    // Адміністративні права
    MANAGE_USERS("manage_users", "Керування користувачами");
    
    private final String code;
    private final String description;
    
    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Перевіряє, чи дозволена певна дія.
     *
     * @param action дія, яку потрібно перевірити
     * @return true, якщо дія дозволена
     */
    public boolean allowed(String action) {
        if (action == null) {
            return false;
        }
        
        // Перевірка базується на відповідності коду дозволу та дії
        switch (this) {
            case CREATE_PROJECT:
            case EDIT_PROJECT:
            case DELETE_PROJECT:
            case VIEW_PROJECT:
                return action.startsWith("project_") && code.contains(action.substring(8));
                
            case CREATE_ISSUE:
            case EDIT_ISSUE:
            case DELETE_ISSUE:
            case VIEW_ISSUE:
                return action.startsWith("issue_") && code.contains(action.substring(6));
                
            case CREATE_COMMENT:
            case EDIT_COMMENT:
            case DELETE_COMMENT:
                return action.startsWith("comment_") && code.contains(action.substring(8));
                
            case MANAGE_USERS:
                return action.startsWith("user_");
                
            default:
                return false;
        }
    }
} 