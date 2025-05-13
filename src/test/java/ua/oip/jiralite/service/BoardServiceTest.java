package ua.oip.jiralite.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.domain.user.Permission;
import ua.oip.jiralite.domain.user.Role;
import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.repository.IssueRepository;
import ua.oip.jiralite.repository.ProjectRepository;

class BoardServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private IssueRepository issueRepository;
    
    @Mock
    private AuthService authService;
    
    private BoardService boardService;
    private User currentUser;
    private Role adminRole;
    private Project project;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Создаем тестовые данные
        adminRole = new Role("Admin", "Administrator Role");
        adminRole.setPermissions(new HashSet<>(Arrays.asList(
            Permission.CREATE_PROJECT,
            Permission.EDIT_PROJECT,
            Permission.DELETE_PROJECT,
            Permission.VIEW_PROJECT,
            Permission.CREATE_ISSUE,
            Permission.EDIT_ISSUE,
            Permission.DELETE_ISSUE,
            Permission.VIEW_ISSUE,
            Permission.CREATE_COMMENT,
            Permission.EDIT_COMMENT,
            Permission.DELETE_COMMENT,
            Permission.MANAGE_USERS
        )));
        
        currentUser = new User("admin", "password", "admin@example.com", "Admin User");
        currentUser.setRole(adminRole);
        
        project = new Project("Test Project", "TEST", "Test project description");
        project.addMember(currentUser);
        
        // Настраиваем моки
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(projectRepository.findByMembersContaining(currentUser)).thenReturn(Collections.singletonList(project));
        
        // Создаем тестируемый объект
        boardService = new BoardService(projectRepository, issueRepository, authService);
    }
    
    @Test
    void getUserProjects() {
        // Вызываем тестируемый метод
        List<Project> projects = boardService.getUserProjects();
        
        // Проверяем результат
        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertEquals(project, projects.get(0));
        
        // Проверяем, что вызваны нужные методы
        verify(authService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).findByMembersContaining(currentUser);
    }
    
    @Test
    void createProject() {
        // Подготавливаем тестовые данные
        String name = "New Project";
        String key = "NEW";
        String description = "New project description";
        
        when(projectRepository.existsByKey(key)).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Вызываем тестируемый метод
        Project createdProject = boardService.createProject(name, key, description);
        
        // Проверяем результат
        assertNotNull(createdProject);
        assertEquals(name, createdProject.getName());
        assertEquals(key, createdProject.getKey());
        assertEquals(description, createdProject.getDescription());
        assertTrue(createdProject.getMembers().contains(currentUser));
        
        // Проверяем, что вызваны нужные методы
        verify(authService, times(1)).getCurrentUser();
        verify(projectRepository, times(1)).existsByKey(key);
        verify(projectRepository, times(1)).save(any(Project.class));
    }
    
    @Test
    void createIssue() {
        // Подготавливаем тестовые данные
        String title = "Test Issue";
        String description = "Test issue description";
        
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Вызываем тестируемый метод
        Issue createdIssue = boardService.createIssue(project, title, description, null);
        
        // Проверяем результат
        assertNotNull(createdIssue);
        assertEquals(title, createdIssue.getTitle());
        assertEquals(description, createdIssue.getDescription());
        assertEquals(Status.TO_DO, createdIssue.getStatus());
        assertEquals(project, createdIssue.getProject());
        assertEquals(currentUser, createdIssue.getReporter());
        assertNull(createdIssue.getAssignee());
        
        // Проверяем, что вызваны нужные методы
        verify(authService, times(1)).getCurrentUser();
        verify(issueRepository, times(1)).save(any(Issue.class));
    }
    
    @Test
    void updateIssueStatus() {
        // Подготавливаем тестовые данные
        Issue issue = new Issue("Test Issue", "Test description", Status.TO_DO, Priority.MEDIUM, project, currentUser);
        Status newStatus = Status.IN_PROGRESS;
        
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);
        
        // Вызываем тестируемый метод
        Issue updatedIssue = boardService.updateIssueStatus(issue, newStatus);
        
        // Проверяем результат
        assertNotNull(updatedIssue);
        assertEquals(newStatus, updatedIssue.getStatus());
        
        // Проверяем, что вызваны нужные методы
        verify(authService, times(1)).getCurrentUser();
        verify(issueRepository, times(1)).save(any(Issue.class));
    }
    
    @Test
    void getProjectIssues() {
        // Подготавливаем тестовые данные
        Issue issue1 = new Issue("Issue 1", "Description 1", Status.TO_DO, Priority.LOW, project, currentUser);
        Issue issue2 = new Issue("Issue 2", "Description 2", Status.IN_PROGRESS, Priority.MEDIUM, project, currentUser);
        List<Issue> issues = Arrays.asList(issue1, issue2);
        
        when(issueRepository.findByProject(project)).thenReturn(issues);
        
        // Вызываем тестируемый метод
        List<Issue> projectIssues = boardService.getProjectIssues(project);
        
        // Проверяем результат
        assertNotNull(projectIssues);
        assertEquals(2, projectIssues.size());
        assertTrue(projectIssues.contains(issue1));
        assertTrue(projectIssues.contains(issue2));
        
        // Проверяем, что вызваны нужные методы
        verify(authService, times(1)).getCurrentUser();
        verify(issueRepository, times(1)).findByProject(project);
    }
} 