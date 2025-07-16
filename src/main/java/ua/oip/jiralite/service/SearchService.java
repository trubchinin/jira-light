package ua.oip.jiralite.service;

import java.util.List;
import java.util.stream.Collectors;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.repository.IssueRepository;
import ua.oip.jiralite.repository.ProjectRepository;

/**
 * Сервіс для пошуку задач та проектів
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
     * Розширений пошук задач з фільтрами
     */
    public List<Issue> searchIssuesAdvanced(String query, Status status, User assignee) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        return issueRepository.findAll().stream()
            .filter(issue -> {
                // Текстовий пошук
                boolean matchesQuery = query == null || query.isEmpty() ||
                    issue.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    issue.getDescription().toLowerCase().contains(query.toLowerCase());
                
                // Фільтр за статусом
                boolean matchesStatus = status == null || issue.getStatus().equals(status);
                
                // Фільтр за assignee
                boolean matchesAssignee = assignee == null || 
                    (issue.getAssignee() != null && issue.getAssignee().equals(assignee));
                
                return matchesQuery && matchesStatus && matchesAssignee;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Пошук проектів за назвою
     */
    public List<Project> searchProjects(String query) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        return projectRepository.findAll().stream()
            .filter(project -> project.getName().toLowerCase().contains(query.toLowerCase()))
            .filter(project -> project.getMembers().contains(currentUser)) // Тільки проекти користувача
            .collect(Collectors.toList());
    }
    
    /**
     * Автокомпліт для пошуку
     */
    public List<String> getSearchSuggestions(String partial) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        
        return issueRepository.findAll().stream()
            .map(Issue::getTitle)
            .filter(title -> title.toLowerCase().startsWith(partial.toLowerCase()))
            .distinct()
            .limit(10)
            .collect(Collectors.toList());
    }
} 