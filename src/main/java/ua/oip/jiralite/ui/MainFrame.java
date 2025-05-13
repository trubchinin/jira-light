package ua.oip.jiralite.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.service.BoardService;

public class MainFrame extends JFrame {
    
    private final AuthService authService;
    private BoardService boardService;
    
    private JPanel contentPanel;
    private JList<Project> projectList;
    private DefaultListModel<Project> projectListModel;
    
    public MainFrame(AuthService authService) {
        this.authService = authService;
        
        setTitle("Jira Lite - Main");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        
        initComponents();
        loadUserProjects();
    }
    
    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }
    
    private void initComponents() {
        // Создаем главную панель с BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Верхняя панель с информацией о пользователе и кнопками
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Левая панель с проектами
        JPanel leftPanel = createLeftPanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);
        
        // Основное содержимое (изначально пустое)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Select a project from the list", SwingConstants.CENTER), BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Нижняя панель со статусом
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        User currentUser = authService.getCurrentUser();
        JLabel userLabel = new JLabel("Logged in as: " + currentUser.getUsername());
        panel.add(userLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newProjectButton = new JButton("New Project");
        JButton logoutButton = new JButton("Logout");
        buttonPanel.add(newProjectButton);
        buttonPanel.add(logoutButton);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        // Обработчики событий
        newProjectButton.addActionListener(e -> showNewProjectDialog());
        logoutButton.addActionListener(e -> logout());
        
        return panel;
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(200, getHeight()));
        
        JLabel projectsLabel = new JLabel("Projects");
        projectsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(projectsLabel, BorderLayout.NORTH);
        
        projectListModel = new DefaultListModel<>();
        projectList = new JList<>(projectListModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(projectList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Обработчик события выбора проекта
        projectList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Project selectedProject = projectList.getSelectedValue();
                if (selectedProject != null) {
                    showProjectDetails(selectedProject);
                }
            }
        });
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel statusLabel = new JLabel("Ready");
        panel.add(statusLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private void loadUserProjects() {
        if (boardService != null) {
            try {
                List<Project> projects = boardService.getUserProjects();
                projectListModel.clear();
                
                for (Project project : projects) {
                    projectListModel.addElement(project);
                }
                
                if (!projects.isEmpty()) {
                    projectList.setSelectedIndex(0);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading projects: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showProjectDetails(Project project) {
        // Очищаем контент панель
        contentPanel.removeAll();
        
        // Создаем панель детальной информации о проекте
        JPanel projectPanel = new JPanel(new BorderLayout(10, 10));
        projectPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Заголовок проекта
        JLabel titleLabel = new JLabel(project.getName() + " (" + project.getKey() + ")");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        projectPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Описание проекта
        JTextArea descriptionArea = new JTextArea(project.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createTitledBorder("Description"));
        projectPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        
        // Панель действий проекта
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewBoardButton = new JButton("View Board");
        JButton createIssueButton = new JButton("Create Issue");
        JButton projectSettingsButton = new JButton("Project Settings");
        
        actionsPanel.add(viewBoardButton);
        actionsPanel.add(createIssueButton);
        actionsPanel.add(projectSettingsButton);
        
        projectPanel.add(actionsPanel, BorderLayout.SOUTH);
        
        // Добавляем панель проекта в контент-панель
        contentPanel.add(projectPanel, BorderLayout.CENTER);
        
        // Обработчики событий
        viewBoardButton.addActionListener(e -> showBoard(project));
        createIssueButton.addActionListener(e -> showCreateIssueDialog(project));
        projectSettingsButton.addActionListener(e -> showProjectSettings(project));
        
        // Обновляем UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showBoard(Project project) {
        // TODO: Реализовать отображение доски проекта
        JOptionPane.showMessageDialog(this,
            "Board view is not implemented yet",
            "Not Implemented",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showCreateIssueDialog(Project project) {
        // TODO: Реализовать диалог создания задачи
        JOptionPane.showMessageDialog(this,
            "Create issue dialog is not implemented yet",
            "Not Implemented",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showProjectSettings(Project project) {
        // TODO: Реализовать настройки проекта
        JOptionPane.showMessageDialog(this,
            "Project settings is not implemented yet",
            "Not Implemented",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showNewProjectDialog() {
        // TODO: Реализовать диалог создания проекта
        JOptionPane.showMessageDialog(this,
            "New project dialog is not implemented yet",
            "Not Implemented",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        authService.logout();
        
        // Возвращаемся на экран логина
        LoginFrame loginFrame = new LoginFrame(authService);
        loginFrame.setVisible(true);
        
        // Закрываем текущее окно
        dispose();
    }
} 