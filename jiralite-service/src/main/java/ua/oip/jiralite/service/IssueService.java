package ua.oip.jiralite.service;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.repository.IssueRepository;
import ua.oip.jiralite.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с задачами
 */
public class IssueService {
    
    private static IssueService instance;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    
    /**
     * Возвращает Singleton экземпляр сервиса
     */
    public static IssueService getInstance() {
        if (instance == null) {
            instance = new IssueService();
        }
        return instance;
    }
    
    /**
     * Конструктор для внедрения зависимостей
     */
    public IssueService(IssueRepository issueRepository, UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Конструктор для Singleton
     */
    private IssueService() {
        this.issueRepository = null;
        this.userRepository = null;
    }
    
    /**
     * Получает пользователей, участвующих в проекте
     */
    public List<User> getProjectUsers(Long projectId) {
        // В реальном приложении здесь был бы запрос в БД
        // Для прототипа возвращаем фиктивных пользователей
        
        List<User> users = new ArrayList<>();
        
        User user1 = new User();
        user1.setId(1L);
        user1.setLogin("admin");
        user1.setFullName("Administrator");
        
        User user2 = new User();
        user2.setId(2L);
        user2.setLogin("user");
        user2.setFullName("Regular User");
        
        users.add(user1);
        users.add(user2);
        
        return users;
    }
    
    /**
     * Обновляет информацию о задаче
     */
    public void updateIssue(Issue issue) {
        // В реальном приложении здесь было бы сохранение в БД
        // Для прототипа ничего не делаем
    }
} 