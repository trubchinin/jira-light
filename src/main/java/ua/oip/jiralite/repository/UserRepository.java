package ua.oip.jiralite.repository;

import java.util.Optional;

import ua.oip.jiralite.domain.user.User;

public interface UserRepository extends AbstractRepository<User> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
} 