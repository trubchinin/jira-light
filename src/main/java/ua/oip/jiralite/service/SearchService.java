package ua.oip.jiralite.service;

import java.util.List;
import java.util.stream.Collectors;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.repository.IssueRepository;
import ua.oip.jiralite.repository.ProjectRepository;

/**
 * Сервіс для пошуку задач та проектів
 * 
 * TODO: Реалізувати повнотекстовий пошук
 * TODO: Додати фільтрацію за статусом
 * TODO: Додати пошук по assignee
 */
public class SearchService {
    
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final AuthService authService;
    
    public SearchService(IssueRepository issueRepository, 
                        ProjectRepository projectRepository, 
                        AuthService authService) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.authService = authService;
    }
    
    /**
     * Пошук задач за ключовими словами
     */
    public List<Issue> searchIssues(String query) {
        // WIP: Тимчасова реалізація, потрібно доопрацювати
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        return issueRepository.findAll().stream()
            .filter(issue -> issue.getTitle().toLowerCase().contains(query.toLowerCase())
                || issue.getDescription().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * Пошук проектів за назвою
     */
    public List<Project> searchProjects(String query) {
        // TODO: Додати перевірку прав доступу
        return projectRepository.findAll().stream()
            .filter(project -> project.getName().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }
} 