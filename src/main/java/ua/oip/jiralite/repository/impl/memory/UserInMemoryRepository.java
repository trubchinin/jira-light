package ua.oip.jiralite.repository.impl.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.repository.UserRepository;

public class UserInMemoryRepository implements UserRepository {
    
    private final Map<Long, User> usersById = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public User save(User entity) {
        if (entity.getId() == null) {
            entity.setId(idGenerator.getAndIncrement());
        }
        usersById.put(entity.getId(), entity);
        return entity;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(usersById.get(id));
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }
    
    @Override
    public void delete(User entity) {
        deleteById(entity.getId());
    }
    
    @Override
    public void deleteById(Long id) {
        usersById.remove(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return usersById.containsKey(id);
    }
    
    @Override
    public long count() {
        return usersById.size();
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return usersById.values().stream()
            .filter(user -> user.getUsername().equals(username))
            .findFirst();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return usersById.values().stream()
            .filter(user -> user.getEmail().equals(email))
            .findFirst();
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return usersById.values().stream()
            .anyMatch(user -> user.getUsername().equals(username));
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return usersById.values().stream()
            .anyMatch(user -> user.getEmail().equals(email));
    }
} 