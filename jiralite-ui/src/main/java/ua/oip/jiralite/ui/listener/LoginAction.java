package ua.oip.jiralite.ui.listener;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.ui.frame.LoginFrame;
import ua.oip.jiralite.ui.frame.MainFrame;
import ua.oip.jiralite.ui.util.SwingHelper;

/**
 * Обробник натискання кнопки входу в систему.
 * Перевіряє авторизацію користувача та відкриває головне вікно.
 */
public class LoginAction implements ActionListener {
    
    private static final Logger log = LoggerFactory.getLogger(LoginAction.class);
    
    private final LoginFrame loginFrame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final AuthService authService;
    private final ResourceBundle messages;
    
    /**
     * Конструктор обробника логіну
     * 
     * @param loginFrame вікно логіну
     * @param usernameField поле імені користувача
     * @param passwordField поле паролю
     * @param loginButton кнопка входу в систему
     * @param authService сервіс авторизації
     * @param messages ресурси локалізації
     */
    public LoginAction(LoginFrame loginFrame, JTextField usernameField, 
            JPasswordField passwordField, JButton loginButton, 
            AuthService authService, ResourceBundle messages) {
        this.loginFrame = loginFrame;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.loginButton = loginButton;
        this.authService = authService;
        this.messages = messages;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Отримуємо дані з полів
        final String username = usernameField.getText().trim();
        final char[] password = passwordField.getPassword();
        
        // Перевірка, що поля не порожні
        if (username.isEmpty()) {
            SwingHelper.showErrorDialog(loginFrame, 
                    messages.getString("app.error"), 
                    messages.getString("login.username_required"));
            usernameField.requestFocus();
            return;
        }
        
        if (password.length == 0) {
            SwingHelper.showErrorDialog(loginFrame, 
                    messages.getString("app.error"), 
                    messages.getString("login.password_required"));
            passwordField.requestFocus();
            return;
        }
        
        // Блокуємо форму на час авторизації
        loginButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        loginFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Виконуємо авторизацію в окремому потоці
        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                // Викликаємо метод авторизації з сервісу
                return authService.authenticate(username, new String(password));
            }
            
            @Override
            protected void done() {
                try {
                    // Отримуємо результат авторизації
                    final User user = get();
                    
                    if (user != null) {
                        // Авторизація успішна - запускаємо головне вікно
                        log.info("User {} authenticated successfully", username);
                        
                        // Закриваємо вікно логіну
                        loginFrame.dispose();
                        
                        // Створюємо та відображаємо головне вікно
                        SwingUtilities.invokeLater(() -> {
                            // Отримуємо екземпляр BoardService
                            ua.oip.jiralite.service.BoardService boardService = ua.oip.jiralite.service.BoardService.getInstance();
                            // Створюємо головне вікно з правильними параметрами
                            MainFrame mainFrame = new MainFrame(boardService, messages, user);
                            mainFrame.setVisible(true);
                        });
                    } else {
                        // Авторизація невдала
                        log.warn("Authentication failed for user {}", username);
                        
                        SwingHelper.showErrorDialog(loginFrame, 
                                messages.getString("app.error"), 
                                messages.getString("login.error"));
                                
                        // Очищаємо поле пароля
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    // Помилка авторизації
                    log.error("Error during authentication", ex);
                    
                    SwingHelper.showErrorDialog(loginFrame, 
                            messages.getString("app.error"), 
                            ex.getMessage());
                } finally {
                    // Розблоковуємо форму
                    loginButton.setEnabled(true);
                    usernameField.setEnabled(true);
                    passwordField.setEnabled(true);
                    loginFrame.setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
} 