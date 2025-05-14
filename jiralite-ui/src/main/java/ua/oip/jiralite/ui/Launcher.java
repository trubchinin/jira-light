package ua.oip.jiralite.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.ui.frame.LoginFrame;

/**
 * Точка входа в программу Jira Lite.
 * Запускает оконное приложение и настраивает основные параметры.
 */
public class Launcher {
    
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);
    
    public static void main(String[] args) {
        // Применяем системный Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Запускаем приложение в Swing потоке
            SwingUtilities.invokeLater(() -> {
                try {
                    // Инициализируем и запускаем окно логина
                    LoginFrame loginFrame = new LoginFrame(AuthService.getInstance());
                    loginFrame.setVisible(true);
                    
                    log.info("Jira Lite application started successfully");
                } catch (Exception e) {
                    log.error("Error starting application", e);
                }
            });
        } catch (Exception e) {
            log.error("Error setting look and feel", e);
        }
    }
} 