package ua.oip.jiralite.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import ua.oip.jiralite.domain.user.Permission;
import ua.oip.jiralite.domain.user.Role;

/**
 * Модель користувача системи.
 */
public class User extends BaseEntity {

    private String username;
    private String email;
    private String fullName;
    private Role   role;
    private String passwordHash;

    /**
     * Повний конструктор для створення користувача
     */
    public User(String username, String email, Role role) {
        super();
        this.username = username;
        this.email    = email;
        this.role     = role;
    }
    
    /**
     * Розширений конструктор для користувача
     */
    public User(String username, String password, String email, String fullName) {
        super();
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        if (password != null) {
            setPassword(password);
        }
    }
    
    /**
     * Спрощений конструктор для тестів
     */
    public User(String username) {
        super();
        this.username = username;
    }

    // ── Гетери / сетери ──────────────────────────────────────────────────────
    public String getUsername()            { return username; }
    public void   setUsername(String n)    { this.username = n; touch(); }

    public String getEmail()               { return email; }
    public void   setEmail(String e)       { this.email = e; touch(); }
    
    public String getFullName()            { return fullName; }
    public void   setFullName(String name) { this.fullName = name; touch(); }

    public Role   getRole()                { return role; }
    public void   setRole(Role r)          { this.role  = r; touch(); }

    /** Встановити пароль користувача (зберігається хеш) */
    public void setPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            this.passwordHash = Base64.getEncoder().encodeToString(hash);
            touch();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /** Перевірити пароль */
    public boolean checkPassword(char[] password) {
        if (passwordHash == null) {
            return false;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(new String(password).getBytes());
            String inputHash = Base64.getEncoder().encodeToString(hash);
            return passwordHash.equals(inputHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        } finally {
            // Очищаємо масив з паролем для безпеки
            Arrays.fill(password, '\0');
        }
    }

    /** Перевірити, чи має користувач певний дозвіл. */
    public boolean can(Permission p)       { 
        return role != null && role.has(p); 
    }
} 