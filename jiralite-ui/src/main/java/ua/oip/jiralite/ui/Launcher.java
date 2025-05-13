package ua.oip.jiralite.ui;

import java.awt.EventQueue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.ui.frame.LoginFrame;
import ua.oip.jiralite.ui.util.SwingHelper;

/**
 * Точка входу в програму Jira Lite.
 * Ініціалізує загальні налаштування та запускає вікно логіну.
 */
public class Launcher {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        setupLookAndFeel();
        
        EventQueue.invokeLater(() -> {
            try {
                // Створюємо та ініціалізуємо необхідні сервіси
                AuthService authService = new AuthService();
                
                // Запускаємо вікно логіну
                LoginFrame loginFrame = new LoginFrame(authService);
                loginFrame.setVisible(true);
                
                log.info("Jira Lite application started successfully");
            } catch (Exception e) {
                log.error("Error starting application", e);
                SwingHelper.showErrorDialog(null, "Помилка запуску застосунку", e.getMessage());
            }
        });
    }
    
    /**
     * Налаштування Look and Feel для відображення елементів інтерфейсу в стилі ОС
     */
    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            log.debug("System Look & Feel applied");
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            log.warn("Could not set system Look & Feel, using default", e);
        }
    }
} 