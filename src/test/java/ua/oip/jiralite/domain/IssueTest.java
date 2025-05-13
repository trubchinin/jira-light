package ua.oip.jiralite.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.domain.user.Role;
import ua.oip.jiralite.domain.user.User;

class IssueTest {
    
    private Project project;
    private User reporter;
    private User assignee;
    private Issue issue;
    
    @BeforeEach
    void setUp() {
        // Створюємо тестових користувачів
        Role role = new Role("Developer", "Developer role");
        reporter = new User("reporter", "password", "reporter@example.com", "Reporter User");
        reporter.setRole(role);
        
        assignee = new User("assignee", "password", "assignee@example.com", "Assignee User");
        assignee.setRole(role);
        
        // Створюємо тестовий проект
        project = new Project("Test Project", "TEST", "Test project description");
        project.addMember(reporter);
        project.addMember(assignee);
        
        // Створюємо тестове завдання
        issue = new Issue("Test Issue", "Test issue description", Status.TO_DO, Priority.MEDIUM, project, reporter);
        issue.setAssignee(assignee);
    }
    
    @Test
    void testIssueCreation() {
        assertEquals("Test Issue", issue.getTitle());
        assertEquals("Test issue description", issue.getDescription());
        assertEquals(Status.TO_DO, issue.getStatus());
        assertEquals(Priority.MEDIUM, issue.getPriority());
        assertEquals(project, issue.getProject());
        assertEquals(reporter, issue.getReporter());
        assertEquals(assignee, issue.getAssignee());
    }
    
    @Test
    @DisplayName("Перевірка оновлення статусу завдання")
    void setStatus_updatesField() {
        // Змінюємо статус завдання
        issue.setStatus(Status.IN_PROGRESS);
        
        // Перевіряємо з використанням AssertJ
        assertThat(issue.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }
    
    @Test
    @DisplayName("Перевірка додавання коментаря до завдання")
    void addComment_increasesSize() {
        // Отримуємо поточну кількість коментарів
        int before = issue.getComments().size();
        
        // Додаємо новий коментар
        Comment comment = new Comment("Test comment", issue, reporter);
        issue.addComment(comment);
        
        // Перевіряємо, що кількість коментарів збільшилась на 1
        assertThat(issue.getComments()).hasSize(before + 1);
    }
    
    @Test
    void testChangeStatus() {
        // Змінюємо статус завдання
        issue.setStatus(Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, issue.getStatus());
        
        issue.setStatus(Status.IN_REVIEW);
        assertEquals(Status.IN_REVIEW, issue.getStatus());
        
        issue.setStatus(Status.DONE);
        assertEquals(Status.DONE, issue.getStatus());
    }
    
    @Test
    void testAddComment() {
        // Перевіряємо, що спочатку немає коментарів
        assertTrue(issue.getComments().isEmpty());
        
        // Додаємо коментар
        Comment comment = new Comment("Test comment", issue, reporter);
        issue.addComment(comment);
        
        // Перевіряємо, що коментар додано
        assertEquals(1, issue.getComments().size());
        assertEquals("Test comment", issue.getComments().get(0).getText());
        assertEquals(reporter, issue.getComments().get(0).getAuthor());
        
        // Додаємо ще один коментар
        Comment comment2 = new Comment("Second comment", issue, assignee);
        issue.addComment(comment2);
        
        // Перевіряємо, що обидва коментарі є
        assertEquals(2, issue.getComments().size());
    }
    
    @Test
    void testRemoveComment() {
        // Додаємо два коментарі
        Comment comment1 = new Comment("First comment", issue, reporter);
        Comment comment2 = new Comment("Second comment", issue, assignee);
        issue.addComment(comment1);
        issue.addComment(comment2);
        
        // Перевіряємо, що обидва коментарі є
        assertEquals(2, issue.getComments().size());
        
        // Видаляємо перший коментар
        issue.removeComment(comment1);
        
        // Перевіряємо, що залишився тільки другий коментар
        assertEquals(1, issue.getComments().size());
        assertEquals("Second comment", issue.getComments().get(0).getText());
    }
    
    @Test
    void testChangeAssignee() {
        // Створюємо нового користувача
        User newAssignee = new User("newassignee", "password", "new@example.com", "New Assignee");
        Role role = new Role("Tester", "Tester role");
        newAssignee.setRole(role);
        
        // Додаємо його до проекту
        project.addMember(newAssignee);
        
        // Змінюємо виконавця завдання
        issue.setAssignee(newAssignee);
        
        // Перевіряємо, що виконавець змінився
        assertEquals(newAssignee, issue.getAssignee());
        assertNotEquals(assignee, issue.getAssignee());
    }
} 