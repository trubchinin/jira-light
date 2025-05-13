package ua.oip.jiralite.repository.impl.jpa;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.repository.UserRepository;

public class UserJpaRepository implements UserRepository {
    
    private final EntityManagerFactory emf;
    
    public UserJpaRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }
    
    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    @Override
    public User save(User entity) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.getTransaction().begin();
            if (entity.getId() == null) {
                entityManager.persist(entity);
                entityManager.getTransaction().commit();
                return entity;
            } else {
                User merged = entityManager.merge(entity);
                entityManager.getTransaction().commit();
                return merged;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }
    
    @Override
    public Optional<User> findById(Long id) {
        EntityManager entityManager = getEntityManager();
        try {
            return Optional.ofNullable(entityManager.find(User.class, id));
        } finally {
            entityManager.close();
        }
    }
    
    @Override
    public List<User> findAll() {
        EntityManager entityManager = getEntityManager();
        try {
            TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u", User.class);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }
    
    @Override
    public void delete(User entity) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
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
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
        } finally {
            entityManager.close();
        }
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        EntityManager entityManager = getEntityManager();
        try {
            TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            try {
                return Optional.of(query.getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        } finally {
            entityManager.close();
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        EntityManager entityManager = getEntityManager();
        try {
            TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            try {
                return Optional.of(query.getSingleResult());
            } catch (NoResultException e) {
                return Optional.empty();
            }
        } finally {
            entityManager.close();
        }
    }
    
    @Override
    public boolean existsByUsername(String username) {
        EntityManager entityManager = getEntityManager();
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        } finally {
            entityManager.close();
        }
    }
    
    @Override
    public boolean existsByEmail(String email) {
        EntityManager entityManager = getEntityManager();
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.getSingleResult() > 0;
        } finally {
            entityManager.close();
        }
    }
} 