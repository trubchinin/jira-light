package ua.oip.jiralite.repository.impl.jpa;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import ua.oip.jiralite.domain.BoardColumn;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.repository.IssueRepository;

public class IssueJpaRepository implements IssueRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public IssueJpaRepository() {
    }
    
    public IssueJpaRepository(EntityManagerFactory emf) {
        this.entityManager = emf.createEntityManager();
    }
    
    @Override
    public Issue save(Issue entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }
    
    @Override
    public Optional<Issue> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Issue.class, id));
    }
    
    @Override
    public List<Issue> findAll() {
        return entityManager.createQuery("SELECT i FROM Issue i", Issue.class).getResultList();
    }
    
    @Override
    public void delete(Issue entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }
    
    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }
    
    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }
    
    @Override
    public long count() {
        return entityManager.createQuery("SELECT COUNT(i) FROM Issue i", Long.class).getSingleResult();
    }
    
    @Override
    public List<Issue> findByProject(Project project) {
        TypedQuery<Issue> query = entityManager.createQuery(
            "SELECT i FROM Issue i WHERE i.project = :project", Issue.class);
        query.setParameter("project", project);
        return query.getResultList();
    }
    
    @Override
    public List<Issue> findByProjectAndStatus(Project project, Status status) {
        TypedQuery<Issue> query = entityManager.createQuery(
            "SELECT i FROM Issue i WHERE i.project = :project AND i.status = :status", Issue.class);
        query.setParameter("project", project);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    @Override
    public List<Issue> findByAssignee(User assignee) {
        TypedQuery<Issue> query = entityManager.createQuery(
            "SELECT i FROM Issue i WHERE i.assignee = :assignee", Issue.class);
        query.setParameter("assignee", assignee);
        return query.getResultList();
    }
    
    @Override
    public List<Issue> findByReporter(User reporter) {
        TypedQuery<Issue> query = entityManager.createQuery(
            "SELECT i FROM Issue i WHERE i.reporter = :reporter", Issue.class);
        query.setParameter("reporter", reporter);
        return query.getResultList();
    }
    
    @Override
    public List<Issue> findByBoardColumn(BoardColumn boardColumn) {
        TypedQuery<Issue> query = entityManager.createQuery(
            "SELECT i FROM Issue i WHERE i.boardColumn = :boardColumn", Issue.class);
        query.setParameter("boardColumn", boardColumn);
        return query.getResultList();
    }
    
    @Override
    public long countByProject(Project project) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(i) FROM Issue i WHERE i.project = :project", Long.class);
        query.setParameter("project", project);
        return query.getSingleResult();
    }
    
    @Override
    public long countByProjectAndStatus(Project project, Status status) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(i) FROM Issue i WHERE i.project = :project AND i.status = :status", Long.class);
        query.setParameter("project", project);
        query.setParameter("status", status);
        return query.getSingleResult();
    }
    
    @Override
    public List<Issue> findByStatus(Status status) {
        TypedQuery<Issue> query = entityManager.createQuery(
            "SELECT i FROM Issue i WHERE i.status = :status", Issue.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    @Override
    public List<Issue> findByPriority(Priority priority) {
        TypedQuery<Issue> query = entityManager.createQuery(
            "SELECT i FROM Issue i WHERE i.priority = :priority", Issue.class);
        query.setParameter("priority", priority);
        return query.getResultList();
    }
    
    @Override
    public List<Issue> findByProjectAndPriority(Project project, Priority priority) {
        TypedQuery<Issue> query = entityManager.createQuery(
            "SELECT i FROM Issue i WHERE i.project = :project AND i.priority = :priority", Issue.class);
        query.setParameter("project", project);
        query.setParameter("priority", priority);
        return query.getResultList();
    }
} 