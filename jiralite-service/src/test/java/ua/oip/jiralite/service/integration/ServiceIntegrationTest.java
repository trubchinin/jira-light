package ua.oip.jiralite.service.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.service.AuthService.AuthException;
import ua.oip.jiralite.service.BoardService;

/**
 * Интеграционные тесты для взаимодействия сервисов
 */
@TestMethodOrder(OrderAnnotation.class)
public class ServiceIntegrationTest {
    
    private AuthService authService;
    private BoardService boardService;
    private Project testProject;
    private Board testBoard;
    
    @BeforeEach
    public void setUp() {
        // Получаем синглтон-экземпляры сервисов
        authService = AuthService.getInstance();
        boardService = BoardService.getInstance();
        
        // Создаем тестовые данные
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Integration Test Project");
        testProject.setKey("INT");
        
        testBoard = new Board();
        testBoard.setId(1L);
        testBoard.setName("Integration Test Board");
        testBoard.setProject(testProject);
        
        // Сбрасываем текущего пользователя
        authService.logout();
    }
    
    @Test
    @Order(1)
    @DisplayName("ADMIN должен иметь возможность создавать задачи")
    public void testAdminCreateIssue() throws AuthException {
        // Дано
        User admin = authService.signIn("admin", "qwerty");
        
        // Проверяем, что текущий пользователь - администратор
        assertEquals(admin, authService.getCurrentUser(), "Текущий пользователь должен быть администратором");
        assertTrue(authService.canCreateIssue(), "Администратор должен иметь право создавать задачи");
        
        // Создаем задачу
        Issue issue = new Issue();
        issue.setTitle("Интеграционная задача админа");
        issue.setStatus(Status.TO_DO);
        
        // Когда
        Issue createdIssue = boardService.createIssue(testBoard, admin, admin, issue);
        
        // Тогда
        assertNotNull(createdIssue, "Созданная задача не должна быть null");
        assertEquals("Интеграционная задача админа", createdIssue.getTitle(), "Заголовок задачи должен совпадать");
    }
    
    @Test
    @Order(2)
    @DisplayName("USER должен иметь возможность редактировать свои задачи")
    public void testUserEditOwnIssue() throws AuthException {
        // Дано
        User user = authService.signIn("john", "1234");
        
        // Проверяем, что текущий пользователь - обычный пользователь
        assertEquals(user, authService.getCurrentUser(), "Текущий пользователь должен быть john");
        assertTrue(authService.canCreateIssue(), "Пользователь должен иметь право создавать задачи");
        
        // Создаем задачу от имени пользователя
        Issue userIssue = new Issue();
        userIssue.setTitle("Задача пользователя для редактирования");
        userIssue.setStatus(Status.TO_DO);
        Issue createdUserIssue = boardService.createIssue(testBoard, user, user, userIssue);
        
        // Проверяем, что пользователь может редактировать свою задачу
        assertTrue(boardService.canUserEditIssue(createdUserIssue), 
                "Пользователь должен иметь право редактировать свою задачу");
        
        // Когда
        createdUserIssue.setTitle("Обновленная задача пользователя");
        boardService.updateIssue(createdUserIssue);
        
        // Получаем список задач, чтобы проверить обновление
        List<Issue> issues = boardService.getBoardIssues(testBoard);
        
        // Тогда
        boolean found = false;
        for (Issue issue : issues) {
            if (issue.getId().equals(createdUserIssue.getId())) {
                assertEquals("Обновленная задача пользователя", issue.getTitle(), 
                        "Заголовок задачи должен быть обновлен");
                found = true;
                break;
            }
        }
        
        assertTrue(found, "Обновленная задача должна быть найдена в списке задач доски");
    }
    
    @Test
    @Order(3)
    @DisplayName("GUEST не должен иметь возможность создавать и редактировать задачи")
    public void testGuestRestrictions() throws AuthException {
        // Дано
        // Сначала создаем задачу от имени администратора
        User admin = authService.signIn("admin", "qwerty");
        Issue adminIssue = new Issue();
        adminIssue.setTitle("Задача админа для проверки ограничений гостя");
        adminIssue.setStatus(Status.TO_DO);
        Issue createdAdminIssue = boardService.createIssue(testBoard, admin, admin, adminIssue);
        
        // Затем входим как гость
        authService.logout();
        User guest = authService.signIn("guest", "guest");
        
        // Проверяем, что гость не может создавать и редактировать задачи
        assertFalse(authService.canCreateIssue(), "Гость не должен иметь право создавать задачи");
        assertFalse(authService.canEditIssue(), "Гость не должен иметь право редактировать задачи");
        assertFalse(boardService.canUserEditIssue(createdAdminIssue), 
                "Гость не должен иметь право редактировать задачу администратора");
        
        // При этом гость должен видеть список задач
        List<Issue> issues = boardService.getBoardIssues(testBoard);
        assertNotNull(issues, "Гость должен видеть список задач");
        assertFalse(issues.isEmpty(), "Список задач для гостя не должен быть пустым");
    }
    
    @Test
    @Order(4)
    @DisplayName("Пользователь не должен иметь возможность редактировать задачи других пользователей")
    public void testUserCannotEditOthersIssues() throws AuthException {
        // Дано
        // Сначала создаем задачу от имени администратора
        User admin = authService.signIn("admin", "qwerty");
        Issue adminIssue = new Issue();
        adminIssue.setTitle("Задача админа для проверки ограничений пользователя");
        adminIssue.setStatus(Status.TO_DO);
        adminIssue.setAssignee(admin);
        Issue createdAdminIssue = boardService.createIssue(testBoard, admin, admin, adminIssue);
        
        // Затем входим как обычный пользователь
        authService.logout();
        User user = authService.signIn("john", "1234");
        
        // Проверяем, что пользователь не может редактировать задачу админа
        assertFalse(boardService.canUserEditIssue(createdAdminIssue), 
                "Пользователь не должен иметь право редактировать задачу администратора");
    }
    
    @Test
    @Order(5)
    @DisplayName("Авторизация должна влиять на права пользователя")
    public void testAuthorizationAffectsPermissions() throws AuthException {
        // Дано
        // Получаем права без авторизации
        boolean canCreateBefore = authService.canCreateIssue();
        boolean canEditBefore = authService.canEditIssue();
        
        // Когда авторизуемся как администратор
        User admin = authService.signIn("admin", "qwerty");
        
        // Тогда права должны измениться
        assertTrue(authService.canCreateIssue(), "После авторизации как админ должно быть право на создание задач");
        assertTrue(authService.canEditIssue(), "После авторизации как админ должно быть право на редактирование задач");
        
        // Когда выходим из системы
        authService.logout();
        
        // Тогда права должны вернуться к изначальным
        assertEquals(canCreateBefore, authService.canCreateIssue(), 
                "После выхода права на создание задач должны вернуться к изначальным");
        assertEquals(canEditBefore, authService.canEditIssue(), 
                "После выхода права на редактирование задач должны вернуться к изначальным");
    }
} 