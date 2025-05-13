package ua.oip.jiralite.service;

import java.util.Optional;

import ua.oip.jiralite.domain.user.Role;
import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.repository.UserRepository;

public class AuthService {
    
    private final UserRepository userRepository;
    private User currentUser;
    
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public boolean login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (user.getPassword().equals(password)) { // В реальном приложении нужно использовать хеширование паролей
                currentUser = user;
                return true;
            }
        }
        
        return false;
    }
    
    public void logout() {
        currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public User registerUser(String username, String password, String email, String fullName, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered");
        }
        
        User user = new User(username, password, email, fullName);
        user.setRole(role);
        
        return userRepository.save(user);
    }
    
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User with ID " + user.getId() + " not found");
        }
        
        // Проверка на уникальность username и email, исключая текущего пользователя
        userRepository.findByUsername(user.getUsername())
            .filter(u -> !u.getId().equals(user.getId()))
            .ifPresent(u -> {
                throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken");
            });
        
        userRepository.findByEmail(user.getEmail())
            .filter(u -> !u.getId().equals(user.getId()))
            .ifPresent(u -> {
                throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered");
            });
        
        return userRepository.save(user);
    }
} 