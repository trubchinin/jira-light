package ua.oip.jiralite.repository;

import java.util.List;
import java.util.Optional;

import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.user.User;

public interface ProjectRepository extends AbstractRepository<Project> {
    
    Optional<Project> findByKey(String key);
    
    List<Project> findByMembersContaining(User user);
    
    boolean existsByKey(String key);
} 