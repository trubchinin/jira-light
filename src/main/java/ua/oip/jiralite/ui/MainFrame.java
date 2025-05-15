package ua.oip.jiralite.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
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
        JPanel leftPanelContainer = new JPanel();
        leftPanelContainer.setLayout(new BoxLayout(leftPanelContainer, BoxLayout.Y_AXIS));
        leftPanelContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanelContainer.setPreferredSize(new Dimension(130, getHeight()));
        leftPanelContainer.setMinimumSize(new Dimension(100, 0));

        // --- Projects Section ---
        JLabel projectsLabel = new JLabel("Projects");
        projectsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        projectsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanelContainer.add(projectsLabel);

        projectListModel = new DefaultListModel<>();
        projectList = new JList<>(projectListModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane projectListScrollPane = new JScrollPane(projectList);
        projectListScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        projectListScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        projectListScrollPane.setPreferredSize(new Dimension(Short.MAX_VALUE, 150));
        leftPanelContainer.add(projectListScrollPane);

        // Обробник події вибору проекту
        projectList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Project selectedProject = projectList.getSelectedValue();
                if (selectedProject != null) {
                    showProjectDetails(selectedProject);
                }
            }
        });
        
        leftPanelContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- Search Section ---
        JPanel searchFilterPanel = new JPanel(new GridBagLayout());
        searchFilterPanel.setBorder(BorderFactory.createTitledBorder("Пошук"));
        searchFilterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Поиск: [JTextField]
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        searchFilterPanel.add(new JLabel("Поиск:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField searchTextField = new JTextField(0);
        searchTextField.setMinimumSize(new Dimension(10, searchTextField.getPreferredSize().height));
        searchFilterPanel.add(searchTextField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        // Row 1: [JCheckBox Искать в описании] (spans 2 columns)
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JCheckBox searchInDescriptionCheckBox = new JCheckBox("Искать в описании");
        searchInDescriptionCheckBox.setSelected(true);
        searchFilterPanel.add(searchInDescriptionCheckBox, gbc);
        gbc.gridwidth = 1;

        // Row 2: Статус: [JComboBox Все]
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        searchFilterPanel.add(new JLabel("Статус:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Все"});
        statusComboBox.setMinimumSize(new Dimension(10, statusComboBox.getPreferredSize().height));
        searchFilterPanel.add(statusComboBox, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Row 3: Приоритет: [JComboBox Все]
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        searchFilterPanel.add(new JLabel("Приоритет:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JComboBox<String> priorityComboBox = new JComboBox<>(new String[]{"Все"});
        priorityComboBox.setMinimumSize(new Dimension(10, priorityComboBox.getPreferredSize().height));
        searchFilterPanel.add(priorityComboBox, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Row 4: Исполнитель: [JComboBox Все]
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        searchFilterPanel.add(new JLabel("Исполнитель:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JComboBox<String> assigneeComboBox = new JComboBox<>(new String[]{"Все"});
        assigneeComboBox.setMinimumSize(new Dimension(10, assigneeComboBox.getPreferredSize().height));
        searchFilterPanel.add(assigneeComboBox, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Row 5: Buttons [Сбросить] [Найти]
        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        JButton resetButton = new JButton("Сбросить");
        JButton findButton = new JButton("Найти");
        searchButtonsPanel.add(resetButton);
        searchButtonsPanel.add(findButton);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        searchFilterPanel.add(searchButtonsPanel, gbc);

        leftPanelContainer.add(searchFilterPanel);
        
        leftPanelContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- Результати Section ---
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Результати"));
        resultsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noResultsLabel = new JLabel("Нет результатов");
        noResultsLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        resultsPanel.add(noResultsLabel, BorderLayout.NORTH);
        resultsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        resultsPanel.setPreferredSize(new Dimension(Short.MAX_VALUE, 60));
        leftPanelContainer.add(resultsPanel);

        leftPanelContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- Повідомлення Section ---
        JPanel notificationsPanel = new JPanel(new BorderLayout(0,5));
        notificationsPanel.setBorder(BorderFactory.createTitledBorder("Повідомлення"));
        notificationsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel systemNotificationsLabel = new JLabel("Системні сповіщення");
        systemNotificationsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        notificationsPanel.add(systemNotificationsLabel, BorderLayout.NORTH);

        JPanel actualNotificationButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actualNotificationButtons.add(new JButton("Всі"));
        actualNotificationButtons.add(new JButton("Налаштування"));
        notificationsPanel.add(actualNotificationButtons, BorderLayout.CENTER);
        notificationsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        notificationsPanel.setPreferredSize(new Dimension(Short.MAX_VALUE, 80));
        leftPanelContainer.add(notificationsPanel);

        // Fill remaining space to push status label to bottom
        leftPanelContainer.add(Box.createVerticalGlue()); 
        
        // --- Bottom status in left panel ---
        JLabel leftPanelStatusLabel = new JLabel("Дошка вибрана");
        leftPanelStatusLabel.setFont(leftPanelStatusLabel.getFont().deriveFont(Font.ITALIC));
        leftPanelStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanelStatusLabel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
        leftPanelContainer.add(leftPanelStatusLabel);
        
        return leftPanelContainer;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel statusLabel = new JLabel("Готово");
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
                    "Помилка завантаження проектів: " + e.getMessage(),
                    "Помилка",
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
        descriptionArea.setBorder(BorderFactory.createTitledBorder("Опис"));
        projectPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        
        // Панель действий проекта
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewBoardButton = new JButton("Переглянути дошку");
        JButton createIssueButton = new JButton("Створити задачу");
        JButton projectSettingsButton = new JButton("Налаштування проекту");
        
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
        // Очищаємо попередній вміст
        contentPanel.removeAll();

        // Головна панель для відображення дошки
        JPanel boardDisplayPanel = new JPanel(new BorderLayout(10, 10));
        boardDisplayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок дошки
        JLabel boardTitleLabel = new JLabel("Дошка: Kanban Board (" + project.getName() + ")");
        boardTitleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        boardTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        boardDisplayPanel.add(boardTitleLabel, BorderLayout.NORTH);

        // Панель для розміщення колонок
        JPanel columnsContainerPanel = new JPanel();
        columnsContainerPanel.setLayout(new BoxLayout(columnsContainerPanel, BoxLayout.X_AXIS));
        columnsContainerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Визначимо статуси для колонок (на основі скріншоту та загальної практики)
        // Перші два зі скріншоту, третій - припущення "Готово"
        String[] columnTitles = {"To Do", "In Progress", "Готово"};
        String[] buttonLabels = {"+ Додати в To Do", "+ Додати в In Progress", "+"};


        for (int i = 0; i < columnTitles.length; i++) {
            String columnTitle = columnTitles[i];
            String buttonLabel = buttonLabels[i];

            // Контейнер для однієї колонки та її кнопки
            JPanel individualColumnHolder = new JPanel(new BorderLayout(0, 5));
            individualColumnHolder.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            // Тут буде знаходитись BoardColumnPanel
            // TODO: Замінити цей плейсхолдер на реальний BoardColumnPanel з задачами
            JPanel placeholderBoardColumnPanel = new JPanel();
            placeholderBoardColumnPanel.setBorder(BorderFactory.createTitledBorder(columnTitle));
            placeholderBoardColumnPanel.setPreferredSize(new Dimension(280, 400));
            // Наприклад:
            // BoardColumnPanel actualColumnPanel = new BoardColumnPanel(columnTitle, boardService, messagesBundle);
            // Тут потрібно буде завантажити задачі для цієї колонки і додати їх до actualColumnPanel
            // actualColumnPanel.addIssue(...);

            JScrollPane columnScrollPane = new JScrollPane(placeholderBoardColumnPanel);
            columnScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


            individualColumnHolder.add(columnScrollPane, BorderLayout.CENTER);

            JButton addToColumnButton = new JButton(buttonLabel);
            // TODO: Додати обробник події для кнопки, наприклад, відкриття діалогу створення задачі з цим статусом
            // addToColumnButton.addActionListener(e -> showCreateIssueDialogWithStatus(project, columnTitle));
            
            JPanel columnButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            columnButtonPanel.add(addToColumnButton);
            individualColumnHolder.add(columnButtonPanel, BorderLayout.SOUTH);

            columnsContainerPanel.add(individualColumnHolder);
            if (i < columnTitles.length - 1) {
                columnsContainerPanel.add(Box.createHorizontalStrut(10));
            }
        }

        // Горизонтальна прокрутка для колонок, якщо їх буде багато
        JScrollPane columnsOuterScrollPane = new JScrollPane(columnsContainerPanel);
        columnsOuterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        columnsOuterScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        columnsOuterScrollPane.setBorder(null);

        boardDisplayPanel.add(columnsOuterScrollPane, BorderLayout.CENTER);

        // Кнопка "Створити задачу" внизу дошки
        JButton createTaskButton = new JButton("Створити задачу");
        // TODO: Додати обробник події для кнопки
        // createTaskButton.addActionListener(e -> showCreateIssueDialog(project));
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomButtonPanel.add(createTaskButton);
        boardDisplayPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        // Додаємо панель дошки до головної контент-панелі
        contentPanel.add(boardDisplayPanel, BorderLayout.CENTER);

        // Оновлюємо UI
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showCreateIssueDialog(Project project) {
        // TODO: Реализовать диалог создания задачи
        JOptionPane.showMessageDialog(this,
            "Діалог створення задачі ще не реалізовано",
            "Не реалізовано",
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