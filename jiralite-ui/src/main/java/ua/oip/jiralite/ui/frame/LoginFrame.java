package ua.oip.jiralite.ui.frame;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.ui.listener.LoginAction;
import ua.oip.jiralite.ui.util.SwingHelper;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Вікно входу в систему.
 * Перша точка взаємодії користувача з програмою.
 */
public class LoginFrame extends JFrame {
    
    private final AuthService authService;
    private ResourceBundle messages;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JCheckBox rememberMeCheckbox;
    
    /**
     * Конструктор вікна логіну
     * 
     * @param authService сервіс авторизації
     */
    public LoginFrame(AuthService authService) {
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
        // Налаштування вікна
        setTitle(messages.getString("login.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UiConstants.LOGIN_FRAME_WIDTH, UiConstants.LOGIN_FRAME_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);  // Центрування вікна
        
        // Головна панель
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(UiConstants.DEFAULT_BORDER);
        mainPanel.setBackground(UiConstants.PANEL_BACKGROUND);
        
        // Заголовок
        JLabel headerLabel = SwingHelper.createHeader(messages.getString("app.welcome"));
        
        // Форма
        JPanel formPanel = new JPanel(new GridLayout(3, 2, UiConstants.COMPONENT_SPACING, UiConstants.COMPONENT_SPACING));
        formPanel.setOpaque(false);
        
        // Поле логіну
        JLabel usernameLabel = new JLabel(messages.getString("login.username") + ":");
        usernameField = new JTextField();
        usernameField.setPreferredSize(UiConstants.FIELD_SIZE);
        
        // Поле пароля
        JLabel passwordLabel = new JLabel(messages.getString("login.password") + ":");
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(UiConstants.FIELD_SIZE);
        
        // Додавання полів до форми
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        
        // Чекбокс "запам'ятати мене"
        rememberMeCheckbox = new JCheckBox(messages.getString("login.remember_me"));
        rememberMeCheckbox.setOpaque(false);
        
        // Панель з кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        loginButton = SwingHelper.createButton(messages.getString("login.sign_in"), null);
        loginButton.setPreferredSize(UiConstants.BUTTON_SIZE);
        
        // Обробник події кнопки входу
        LoginAction loginAction = new LoginAction(
                this, usernameField, passwordField, loginButton, 
                authService, messages);
        
        loginButton.addActionListener(loginAction);
        
        // Також застосовуємо обробник для клавіші Enter в полі пароля
        passwordField.addActionListener(loginAction);
        
        buttonPanel.add(loginButton);
        
        // Додавання всіх компонентів на головну панель
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(rememberMeCheckbox, BorderLayout.WEST);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        // Встановлення головної панелі у вікно
        setContentPane(mainPanel);
        
        // Встановлення початкового фокусу
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }
} 