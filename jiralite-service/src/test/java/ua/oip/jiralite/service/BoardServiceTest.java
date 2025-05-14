package ua.oip.jiralite.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.AuthService.AuthException;

/**
 * Модульные тесты для BoardService
 */
public class BoardServiceTest {
    
    private BoardService boardService;
    private AuthService authService;
    private Project testProject;
    private Board testBoard;
    
    @BeforeEach
    public void setUp() {
        // Инициализируем синглтон-экземпляры сервисов
        boardService = BoardService.getInstance();
        authService = AuthService.getInstance();
        
        // Сбрасываем текущего пользователя
        authService.logout();
        
        // Создаем проект для тестов
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setKey("TEST");
        
        // Создаем доску для тестов
        testBoard = new Board();
        testBoard.setId(1L);
        testBoard.setName("Test Board");
        testBoard.setProject(testProject);
    }
    
    @Test
    @DisplayName("Должен получать задачи доски")
    public void testGetBoardIssues() {
        // Когда
        List<Issue> issues = boardService.getBoardIssues(testBoard);
        
        // Тогда
        assertNotNull(issues, "Список задач не должен быть null");
        assertFalse(issues.isEmpty(), "Список задач не должен быть пустым");
        
        // Проверяем, что все задачи относятся к проекту доски
        for (Issue issue : issues) {
            assertNotNull(issue.getProject(), "Проект задачи не должен быть null");
            assertEquals(testProject.getId(), issue.getProject().getId(), 
                    "ID проекта задачи должен совпадать с ID проекта доски");
        }
    }
    
    @Test
    @DisplayName("Должен создавать новую задачу")
    public void testCreateIssue() throws AuthException {
        // Дано
        // Аутентифицируем пользователя с ролью ADMIN
        User admin = authService.signIn("admin", "qwerty");
        
        // Создаем модель задачи
        Issue newIssue = new Issue();
        newIssue.setTitle("Тестовая задача");
        newIssue.setDescription("Описание тестовой задачи");
        newIssue.setStatus(Status.TO_DO);
        newIssue.setPriority(Priority.MEDIUM);
        
        // Когда
        Issue createdIssue = boardService.createIssue(testBoard, admin, admin, newIssue);
        
        // Тогда
        assertNotNull(createdIssue, "Созданная задача не должна быть null");
        assertNotNull(createdIssue.getId(), "ID созданной задачи не должен быть null");
        assertEquals("Тестовая задача", createdIssue.getTitle(), "Заголовок задачи должен совпадать");
        assertEquals(Status.TO_DO, createdIssue.getStatus(), "Статус задачи должен быть TO_DO");
        assertEquals(admin, createdIssue.getAssignee(), "Исполнитель задачи должен быть admin");
        assertEquals(admin, createdIssue.getReporter(), "Автор задачи должен быть admin");
        assertNotNull(createdIssue.getKey(), "Ключ задачи не должен быть null");
        assertTrue(createdIssue.getKey().startsWith(testProject.getKey()), 
                "Ключ задачи должен начинаться с ключа проекта");
    }
    
    @Test
    @DisplayName("Должен обновлять статус задачи")
    public void testUpdateIssueStatus() throws AuthException {
        // Дано
        // Аутентифицируем пользователя с ролью ADMIN
        User admin = authService.signIn("admin", "qwerty");
        
        // Создаем модель задачи
        Issue newIssue = new Issue();
        newIssue.setTitle("Задача для обновления статуса");
        newIssue.setDescription("Описание задачи для обновления статуса");
        newIssue.setStatus(Status.TO_DO);
        
        // Создаем задачу
        Issue createdIssue = boardService.createIssue(testBoard, admin, admin, newIssue);
        assertNotNull(createdIssue, "Созданная задача не должна быть null");
        assertEquals(Status.TO_DO, createdIssue.getStatus(), "Статус задачи должен быть TO_DO");
        
        // Когда
        boardService.updateIssueStatus(createdIssue, Status.IN_PROGRESS);
        
        // Тогда
        assertEquals(Status.IN_PROGRESS, createdIssue.getStatus(), 
                "Статус задачи должен быть обновлен на IN_PROGRESS");
    }
    
    @Test
    @DisplayName("Администратор должен иметь право редактировать любые задачи")
    public void testAdminCanEditAnyIssue() throws AuthException {
        // Дано
        // Аутентифицируем администратора
        User admin = authService.signIn("admin", "qwerty");
        
        // Аутентифицируем пользователя
        authService.logout();
        User user = authService.signIn("john", "1234");
        
        // Создаем задачу от имени пользователя
        Issue userIssue = new Issue();
        userIssue.setTitle("Задача пользователя");
        userIssue.setStatus(Status.TO_DO);
        userIssue.setAssignee(user);
        Issue createdUserIssue = boardService.createIssue(testBoard, user, user, userIssue);
        
        // Выходим и входим как администратор
        authService.logout();
        authService.signIn("admin", "qwerty");
        
        // Когда/Тогда
        assertTrue(boardService.canUserEditIssue(createdUserIssue), 
                "Администратор должен иметь право редактировать задачу пользователя");
    }
    
    @Test
    @DisplayName("Пользователь должен иметь право редактировать только свои задачи")
    public void testUserCanEditOnlyOwnIssues() throws AuthException {
        // Дано
        // Аутентифицируем администратора и создаем задачу от его имени
        User admin = authService.signIn("admin", "qwerty");
        Issue adminIssue = new Issue();
        adminIssue.setTitle("Задача администратора");
        adminIssue.setStatus(Status.TO_DO);
        adminIssue.setAssignee(admin);
        Issue createdAdminIssue = boardService.createIssue(testBoard, admin, admin, adminIssue);
        
        // Аутентифицируем пользователя и создаем задачу от его имени
        authService.logout();
        User user = authService.signIn("john", "1234");
        Issue userIssue = new Issue();
        userIssue.setTitle("Задача пользователя");
        userIssue.setStatus(Status.TO_DO);
        userIssue.setAssignee(user);
        Issue createdUserIssue = boardService.createIssue(testBoard, user, user, userIssue);
        
        // Тогда
        assertFalse(boardService.canUserEditIssue(createdAdminIssue), 
                "Пользователь не должен иметь право редактировать задачу администратора");
        assertTrue(boardService.canUserEditIssue(createdUserIssue), 
                "Пользователь должен иметь право редактировать свою задачу");
    }
    
    @Test
    @DisplayName("Гость не должен иметь права редактировать задачи")
    public void testGuestCannotEditAnyIssue() throws AuthException {
        // Дано
        // Аутентифицируем администратора и создаем задачу от его имени
        User admin = authService.signIn("admin", "qwerty");
        Issue adminIssue = new Issue();
        adminIssue.setTitle("Задача администратора");
        adminIssue.setStatus(Status.TO_DO);
        adminIssue.setAssignee(admin);
        Issue createdAdminIssue = boardService.createIssue(testBoard, admin, admin, adminIssue);
        
        // Аутентифицируем гостя
        authService.logout();
        User guest = authService.signIn("guest", "guest");
        
        // Тогда
        assertFalse(boardService.canUserEditIssue(createdAdminIssue), 
                "Гость не должен иметь право редактировать задачу администратора");
    }
} 