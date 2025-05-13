package ua.oip.jiralite;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.oip.jiralite.repository.UserRepository;
import ua.oip.jiralite.repository.ProjectRepository;
import ua.oip.jiralite.repository.IssueRepository;
import ua.oip.jiralite.repository.impl.jpa.UserJpaRepository;
import ua.oip.jiralite.repository.impl.jpa.ProjectJpaRepository;
import ua.oip.jiralite.repository.impl.jpa.IssueJpaRepository;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.LoginFrame;

/**
 * Головний клас додатку Jira Lite
 */
public class JiraLiteApp {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraLiteApp.class);
    
    public static void main(String[] args) {
        logger.info("Запуск додатку Jira Lite");
        
        try {
            // Ініціалізуємо EntityManagerFactory
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("jiralitePU");
            logger.info("EntityManagerFactory створено успішно");
            
            // Створюємо репозиторії
            UserRepository userRepository = new UserJpaRepository(emf);
            ProjectRepository projectRepository = new ProjectJpaRepository(emf);
            IssueRepository issueRepository = new IssueJpaRepository(emf);
            
            // Створюємо сервіси
            AuthService authService = new AuthService(userRepository);
            BoardService boardService = new BoardService(projectRepository, issueRepository, authService);
            
            // Запускаємо UI
            LoginFrame loginFrame = new LoginFrame(authService);
            loginFrame.setVisible(true);
            
            logger.info("Додаток успішно запущено");
            
            // Додаємо обробник для закриття EntityManagerFactory при завершенні додатку
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Закриття EntityManagerFactory");
                emf.close();
            }));
            
        } catch (Exception e) {
            logger.error("Помилка при запуску додатку", e);
            System.err.println("Помилка при запуску додатку: " + e.getMessage());
        }
    }
} 