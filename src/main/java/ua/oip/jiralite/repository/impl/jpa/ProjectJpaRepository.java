package ua.oip.jiralite.repository.impl.jpa;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.repository.ProjectRepository;

public class ProjectJpaRepository implements ProjectRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public ProjectJpaRepository() {
    }
    
    public ProjectJpaRepository(EntityManagerFactory emf) {
        this.entityManager = emf.createEntityManager();
    }
    
    @Override
    public Project save(Project entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }
    
    @Override
    public Optional<Project> findById(Long id) {
        Project project = entityManager.find(Project.class, id);
        return Optional.ofNullable(project);
    }
    
    @Override
    public List<Project> findAll() {
        return entityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList();
    }
    
    @Override
    public void delete(Project entity) {
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
        return entityManager.createQuery("SELECT COUNT(p) FROM Project p", Long.class).getSingleResult();
    }
    
    @Override
    public Optional<Project> findByKey(String key) {
        try {
            TypedQuery<Project> query = entityManager.createQuery(
                "SELECT p FROM Project p WHERE p.key = :key", Project.class);
            query.setParameter("key", key);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Project> findByMembersContaining(User user) {
        TypedQuery<Project> query = entityManager.createQuery(
            "SELECT p FROM Project p JOIN p.members m WHERE m = :user", Project.class);
        query.setParameter("user", user);
        return query.getResultList();
    }
    
    @Override
    public boolean existsByKey(String key) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(p) FROM Project p WHERE p.key = :key", Long.class);
        query.setParameter("key", key);
        return query.getSingleResult() > 0;
    }
} 