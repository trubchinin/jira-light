package ua.oip.jiralite.ui.frame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.ui.util.SwingHelper;

/**
 * Вікно входу в систему.
 * Перша точка взаємодії користувача з програмою.
 */
public final class LoginFrame extends JFrame {
    
    private final AuthService authService;
    private ResourceBundle messages;
    
    private final JTextField loginField  = new JTextField(20);
    private final JPasswordField passFld = new JPasswordField(20);
    
    /**
     * Конструктор вікна логіну
     * 
     * @param authService сервіс авторизації
     */
    public LoginFrame(AuthService authService) {
        super("Jira-Lite — Sign in");
        this.authService = authService;
        
        // Завантаження ресурсів локалізації
        loadResources(Locale.getDefault());
        
        initializeUI();
    }
    
    /**
     * Завантаження ресурсів локалізації
     * 
     * @param locale поточна локаль
     */
    private void loadResources(Locale locale) {
        messages = ResourceBundle.getBundle("i18n.labels", locale);
    }
    
    /**
     * Ініціалізація компонентів інтерфейсу
     */
    private void initializeUI() {
        /* ---------- layout ---------- */
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.add(new JLabel(messages.getString("login.username") + ":"));
        form.add(loginField);
        form.add(new JLabel(messages.getString("login.password") + ":"));
        form.add(passFld);

        JButton btnSignIn = new JButton(messages.getString("login.sign_in"));
        btnSignIn.addActionListener(this::handleSignIn);

        setLayout(new BorderLayout(0, 10));
        add(form, BorderLayout.CENTER);
        add(btnSignIn, BorderLayout.SOUTH);

        /* ---------- window props ---------- */
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void handleSignIn(ActionEvent e) {
        String login = loginField.getText().trim();
        String pass  = new String(passFld.getPassword());

        try {
            User u = authService.signIn(login, pass);
            /* → відкриваємо головне вікно */
            SwingUtilities.invokeLater(() -> {
                dispose();
                
                // Создаем главное окно
                ResourceBundle messages = ResourceBundle.getBundle("i18n.labels");
                // Отримуємо екземпляр BoardService
                ua.oip.jiralite.service.BoardService boardService = ua.oip.jiralite.service.BoardService.getInstance();
                // Створюємо головне вікно з правильними параметрами
                new MainFrame(boardService, messages, u).setVisible(true);
            });
        } catch (AuthService.AuthException ex) {
            SwingHelper.showError(this, ex.getMessage());
            passFld.setText("");
        }
    }
} 