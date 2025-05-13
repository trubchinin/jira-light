package ua.oip.jiralite.domain;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.domain.user.Role;

class ProjectTest {
    
    private Project project;
    private User user;
    
    @BeforeEach
    void setUp() {
        // Створюємо тестовий проект
        project = new Project("Test Project", "TEST", "Test project description");
        
        // Створюємо тестового користувача
        Role role = new Role("Developer", "Developer role");
        user = new User("testuser", "password", "test@example.com", "Test User");
        user.setRole(role);
    }
    
    @Test
    @DisplayName("Додавання користувача в проект та перевірка його наявності")
    void addMember_thenContainsUser() {
        // Додаємо користувача в проект
        project.addMember(user);
        
        // Перевіряємо, що користувач є в проекті
        assertThat(project.getMembers()).contains(user);
    }
    
    @Test
    @DisplayName("Перевірка створення проекту з дошкою")
    void projectCreation_hasBoard() {
        // Перевіряємо, що проект має дошку
        assertThat(project.getBoard()).isNotNull();
    }
    
    @Test
    @DisplayName("Видалення користувача з проекту")
    void removeMember_thenDoesNotContainUser() {
        // Додаємо користувача в проект
        project.addMember(user);
        
        // Перевіряємо, що користувач є в проекті
        assertThat(project.getMembers()).contains(user);
        
        // Видаляємо користувача з проекту
        project.removeMember(user);
        
        // Перевіряємо, що користувача немає в проекті
        assertThat(project.getMembers()).doesNotContain(user);
    }
} 