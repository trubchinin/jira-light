package ua.oip.jiralite.repository;

import java.util.List;

import ua.oip.jiralite.domain.BoardColumn;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.domain.user.User;

public interface IssueRepository extends AbstractRepository<Issue> {
    
    List<Issue> findByProject(Project project);
    
    List<Issue> findByProjectAndStatus(Project project, Status status);
    
    List<Issue> findByAssignee(User assignee);
    
    List<Issue> findByReporter(User reporter);
    
    List<Issue> findByBoardColumn(BoardColumn boardColumn);
    
    List<Issue> findByStatus(Status status);
    
    List<Issue> findByPriority(Priority priority);
    
    List<Issue> findByProjectAndPriority(Project project, Priority priority);
    
    long countByProject(Project project);
    
    long countByProjectAndStatus(Project project, Status status);
} 