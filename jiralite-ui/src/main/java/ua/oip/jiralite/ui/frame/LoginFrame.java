package ua.oip.jiralite.ui.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.ui.util.SwingHelper;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Вікно входу в систему.
 * Перша точка взаємодії користувача з програмою.
 */
public final class LoginFrame extends JFrame {
    
    private final AuthService authService;
    private ResourceBundle messages;
    
    private final JTextField loginField = new JTextField(20);
    private final JPasswordField passFld = new JPasswordField(20);
    private JButton btnSignIn;
    
    /**
     * Конструктор вікна логіну
     * 
     * @param authService сервіс авторизації
     */
    public LoginFrame(AuthService authService) {
        super("Jira-Lite");
        this.authService = authService;
        
        // Завантаження ресурсів локалізації
        loadResources(Locale.getDefault());
        
        initializeUI();
        setupEventHandlers();
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
        // Налаштування основної панелі
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(UiConstants.PANEL_BACKGROUND);
        
        // Заголовок
        JLabel titleLabel = new JLabel("Jira Lite");
        titleLabel.setFont(UiConstants.HEADER_FONT);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setForeground(UiConstants.INFO_COLOR);
        
        // Підзаголовок
        JLabel subtitleLabel = new JLabel(messages.getString("login.title"));
        subtitleLabel.setFont(UiConstants.SUBHEADER_FONT);
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);
        subtitleLabel.setForeground(UiConstants.TEXT_SECONDARY);
        
        // Панель з полями вводу
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(10, 5, 10, 10);
        
        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.insets = new Insets(10, 0, 10, 5);
        
        // Поле логіну
        labelConstraints.gridy = 0;
        fieldConstraints.gridy = 0;
        
        JLabel loginLabel = new JLabel(messages.getString("login.username") + ":");
        loginLabel.setFont(UiConstants.DEFAULT_FONT);
        formPanel.add(loginLabel, labelConstraints);
        
        loginField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiConstants.INFO_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        formPanel.add(loginField, fieldConstraints);
        
        // Поле пароля
        labelConstraints.gridy = 1;
        fieldConstraints.gridy = 1;
        
        JLabel passLabel = new JLabel(messages.getString("login.password") + ":");
        passLabel.setFont(UiConstants.DEFAULT_FONT);
        formPanel.add(passLabel, labelConstraints);
        
        passFld.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiConstants.INFO_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        formPanel.add(passFld, fieldConstraints);
        
        // Кнопка входу
        btnSignIn = new JButton(messages.getString("login.sign_in"));
        btnSignIn.setForeground(Color.WHITE);
        btnSignIn.setBackground(UiConstants.INFO_COLOR);
        btnSignIn.setOpaque(true);
        btnSignIn.setFocusPainted(false);
        btnSignIn.setBorderPainted(false);
        btnSignIn.setAlignmentX(CENTER_ALIGNMENT);
        btnSignIn.setPreferredSize(new Dimension(150, 35));
        
        // Додавання компонентів на головну панель
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(formPanel);
        mainPanel.add(btnSignIn);
        
        // Нижній блок з інформацією
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel infoLabel = new JLabel("* Натисніть Enter для входу");
        infoLabel.setFont(UiConstants.SMALL_FONT);
        infoLabel.setForeground(UiConstants.TEXT_SECONDARY);
        infoPanel.add(infoLabel);
        
        mainPanel.add(infoPanel);
        
        // Налаштування вікна
        setContentPane(mainPanel);
        getRootPane().setDefaultButton(btnSignIn);
        setResizable(false);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Налаштування обробників подій
     */
    private void setupEventHandlers() {
        btnSignIn.addActionListener(this::handleSignIn);
        
        // Додаємо обробник клавіатури для поля паролю
        passFld.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSignIn(new ActionEvent(passFld, ActionEvent.ACTION_PERFORMED, "SignIn"));
                }
            }
        });
        
        // Додаємо обробник клавіатури для поля логіну
        loginField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSignIn(new ActionEvent(loginField, ActionEvent.ACTION_PERFORMED, "SignIn"));
                }
            }
        });
    }

    /**
     * Обробка спроби входу
     */
    private void handleSignIn(ActionEvent e) {
        String login = loginField.getText().trim();
        String pass = new String(passFld.getPassword());
        
        if (login.isEmpty()) {
            SwingHelper.showError(this, "Будь ласка, введіть логін");
            loginField.requestFocus();
            return;
        }
        
        if (pass.isEmpty()) {
            SwingHelper.showError(this, "Будь ласка, введіть пароль");
            passFld.requestFocus();
            return;
        }

        try {
            User u = authService.signIn(login, pass);
            /* → відкриваємо головне вікно */
            SwingUtilities.invokeLater(() -> {
                dispose();
                
                // Створюємо головне вікно
                ResourceBundle messages = ResourceBundle.getBundle("i18n.labels");
                // Отримуємо екземпляр BoardService
                ua.oip.jiralite.service.BoardService boardService = ua.oip.jiralite.service.BoardService.getInstance();
                // Створюємо головне вікно з правильними параметрами
                new MainFrame(boardService, messages, u).setVisible(true);
            });
        } catch (AuthService.AuthException ex) {
            SwingHelper.showError(this, ex.getMessage());
            passFld.setText("");
            passFld.requestFocus();
        }
    }
} 