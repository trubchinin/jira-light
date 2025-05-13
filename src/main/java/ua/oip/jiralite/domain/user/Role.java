package ua.oip.jiralite.domain.user;

import java.util.EnumSet;
import java.util.Set;

/**
 * Роль користувача об'єднує набір конкретних дозволів.
 */
public class Role {

    private final String name;
    private String description;
    private final Set<Permission> permissions = EnumSet.noneOf(Permission.class);

    public Role(String name, Permission... perms) {
        this.name = name;
        permissions.addAll(EnumSet.of(perms[0], perms));
    }
    
    /**
     * Конструктор для створення ролі тільки з ім'ям і описом (для тестів)
     */
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ── API ───────────────────────────────────────────────────────────────────
    public boolean has(Permission p) { return permissions.contains(p); }
    public String  getName()         { return name; }
    public String  getDescription()  { return description; }
    public Set<Permission> getPermissions() { return EnumSet.copyOf(permissions); }
} 