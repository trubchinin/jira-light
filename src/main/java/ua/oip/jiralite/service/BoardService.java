package ua.oip.jiralite.service;

import java.util.List;
import java.util.Optional;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.BoardColumn;
import ua.oip.jiralite.domain.Comment;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.domain.user.Permission;
import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.repository.IssueRepository;
import ua.oip.jiralite.repository.ProjectRepository;

public class BoardService {
    
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final AuthService authService;
    
    public BoardService(ProjectRepository projectRepository, IssueRepository issueRepository, AuthService authService) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.authService = authService;
    }
    
    public List<Project> getUserProjects() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        
        return projectRepository.findByMembersContaining(currentUser);
    }
    
    public Project createProject(String name, String key, String description) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        
        if (!currentUser.hasPermission(Permission.CREATE_PROJECT)) {
            throw new IllegalStateException("User does not have permission to create projects");
        }
        
        if (projectRepository.existsByKey(key)) {
            throw new IllegalArgumentException("Project key '" + key + "' already exists");
        }
        
        Project project = new Project(name, key, description);
        project.addMember(currentUser);
        
        return projectRepository.save(project);
    }
    
    public Board createBoard(Project project, String name, String description) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        
        if (!currentUser.hasPermission(Permission.EDIT_PROJECT)) {
            throw new IllegalStateException("User does not have permission to edit projects");
        }
        
        if (!project.getMembers().contains(currentUser)) {
            throw new IllegalStateException("User is not a member of this project");
        }
        
        Board board = new Board(name, description);
        project.addBoard(board);
        
        // Создаем стандартные колонки
        createDefaultColumns(board);
        
        projectRepository.save(project);
        return board;
    }
    
    private void createDefaultColumns(Board board) {
        BoardColumn todoColumn = new BoardColumn("To Do", 0);
        BoardColumn inProgressColumn = new BoardColumn("In Progress", 1);
        BoardColumn inReviewColumn = new BoardColumn("In Review", 2);
        BoardColumn doneColumn = new BoardColumn("Done", 3);
        
        board.addColumn(todoColumn);
        board.addColumn(inProgressColumn);
        board.addColumn(inReviewColumn);
        board.addColumn(doneColumn);
    }
    
    public Issue createIssue(Project project, String title, String description, User assignee) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        
        if (!currentUser.hasPermission(Permission.CREATE_ISSUE)) {
            throw new IllegalStateException("User does not have permission to create issues");
        }
        
        if (!project.getMembers().contains(currentUser)) {
            throw new IllegalStateException("User is not a member of this project");
        }
        
        // Создаем задачу со статусом TO_DO
        Issue issue = new Issue(title, description, Status.TO_DO, null, project, currentUser);
        
        if (assignee != null) {
            issue.setAssignee(assignee);
        }
        
        // Добавляем задачу в колонку "To Do" по умолчанию
        if (!project.getBoards().isEmpty()) {
            Board board = project.getBoards().get(0);
            Optional<BoardColumn> todoColumn = board.getColumns().stream()
                .filter(col -> col.getName().equals("To Do"))
                .findFirst();
            
            todoColumn.ifPresent(issue::setBoardColumn);
        }
        
        project.addIssue(issue);
        return issueRepository.save(issue);
    }
    
    public Issue updateIssueStatus(Issue issue, Status newStatus) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        
        if (!currentUser.hasPermission(Permission.EDIT_ISSUE)) {
            throw new IllegalStateException("User does not have permission to edit issues");
        }
        
        if (!issue.getProject().getMembers().contains(currentUser)) {
            throw new IllegalStateException("User is not a member of this project");
        }
        
        issue.setStatus(newStatus);
        
        // Обновляем колонку в соответствии с новым статусом
        if (issue.getBoardColumn() != null) {
            Board board = issue.getBoardColumn().getBoard();
            String columnName = getColumnNameForStatus(newStatus);
            
            Optional<BoardColumn> newColumn = board.getColumns().stream()
                .filter(col -> col.getName().equals(columnName))
                .findFirst();
            
            newColumn.ifPresent(issue::setBoardColumn);
        }
        
        return issueRepository.save(issue);
    }
    
    private String getColumnNameForStatus(Status status) {
        switch (status) {
            case TO_DO:
                return "To Do";
            case IN_PROGRESS:
                return "In Progress";
            case IN_REVIEW:
                return "In Review";
            case DONE:
                return "Done";
            default:
                return "To Do";
        }
    }
    
    public List<Issue> getProjectIssues(Project project) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        
        if (!currentUser.hasPermission(Permission.VIEW_ISSUE)) {
            throw new IllegalStateException("User does not have permission to view issues");
        }
        
        if (!project.getMembers().contains(currentUser)) {
            throw new IllegalStateException("User is not a member of this project");
        }
        
        return issueRepository.findByProject(project);
    }
    
    public List<Issue> getIssuesByStatus(Project project, Status status) {
        return issueRepository.findByProjectAndStatus(project, status);
    }
    
    public Comment addComment(Issue issue, String text) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not logged in");
        }
        
        if (!currentUser.hasPermission(Permission.CREATE_COMMENT)) {
            throw new IllegalStateException("User does not have permission to create comments");
        }
        
        if (!issue.getProject().getMembers().contains(currentUser)) {
            throw new IllegalStateException("User is not a member of this project");
        }
        
        Comment comment = new Comment(text, issue, currentUser);
        issue.addComment(comment);
        
        issueRepository.save(issue);
        return comment;
    }
} 