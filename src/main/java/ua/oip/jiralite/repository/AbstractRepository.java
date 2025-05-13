package ua.oip.jiralite.repository;

import java.util.List;
import java.util.Optional;

import ua.oip.jiralite.domain.BaseEntity;

public interface AbstractRepository<T extends BaseEntity> {
    
    T save(T entity);
    
    Optional<T> findById(Long id);
    
    List<T> findAll();
    
    void delete(T entity);
    
    void deleteById(Long id);
    
    boolean existsById(Long id);
    
    long count();
} 