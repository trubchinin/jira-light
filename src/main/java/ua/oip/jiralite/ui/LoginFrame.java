package ua.oip.jiralite.ui;

import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.repository.UserRepository;
import ua.oip.jiralite.repository.impl.memory.UserInMemoryRepository;
import ua.oip.jiralite.repository.impl.jpa.UserJpaRepository;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import javax.swing.*;
import java.awt.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginFrame.class);
    
    private final AuthService authService;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    
    public LoginFrame(AuthService authService) {
        this.authService = authService;
        
        setTitle("Jira Lite - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Заголовок
        JLabel titleLabel = new JLabel("Jira Lite");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Форма входу
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loginButton = new JButton("Login");
        buttonPanel.add(loginButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Обробники подій
        loginButton.addActionListener(e -> login());
        
        // Обробка натискання Enter у полі пароля
        passwordField.addActionListener(e -> login());
        
        add(mainPanel);
    }
    
    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username and password must not be empty", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (authService.login(username, password)) {
            // Успішний вхід - відкриваємо головне вікно
            MainFrame mainFrame = new MainFrame(authService);
            mainFrame.setVisible(true);
            dispose(); // Закриваємо вікно входу
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            
            // Очищаємо поле пароля
            passwordField.setText("");
        }
    }
    
    /**
     * Точка входу в додаток для швидкого запуску через LoginFrame
     */
    public static void main(String[] args) {
        try {
            // Встановлюємо Look and Feel системи
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            logger.info("Ініціалізація EntityManagerFactory");
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("jiralitePU");
            
            // Створюємо репозиторій користувачів
            UserRepository userRepository = new UserJpaRepository(emf);
            
            // Створюємо сервіс автентифікації
            AuthService authService = new AuthService(userRepository);
            
            // Запускаємо вікно входу
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(authService);
                loginFrame.setVisible(true);
            });
            
            // Закриття EntityManagerFactory при виході
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Закриття EntityManagerFactory");
                emf.close();
            }));
            
        } catch (Exception e) {
            logger.error("Помилка при запуску додатка", e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error starting application: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 