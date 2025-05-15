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
        logger.info("Цей клас більше не використовується. Використовуйте ua.oip.jiralite.ui.Launcher для запуску додатку.");
        System.out.println("Цей клас більше не використовується. Використовуйте ua.oip.jiralite.ui.Launcher для запуску додатку.");
        
        // Не запускаємо старий код для уникнення дублювання вікон
        return;
    }
} 