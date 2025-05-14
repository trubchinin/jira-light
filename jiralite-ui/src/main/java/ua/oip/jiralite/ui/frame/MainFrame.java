package ua.oip.jiralite.ui.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.listener.IssueCardMouseAdapter;
import ua.oip.jiralite.ui.panel.BoardColumnPanel;
import ua.oip.jiralite.ui.panel.NotificationPanel;
import ua.oip.jiralite.ui.panel.ProjectTreePanel;
import ua.oip.jiralite.ui.panel.ProjectTreePanel.ProjectSelectionListener;
import ua.oip.jiralite.ui.panel.SearchPanel;
import ua.oip.jiralite.ui.panel.SearchResultPanel;
import ua.oip.jiralite.ui.util.ResponsiveHelper;
import ua.oip.jiralite.ui.util.SwingHelper;
import ua.oip.jiralite.ui.util.ThemeManager;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Головне вікно програми
 */
public class MainFrame extends JFrame implements ProjectSelectionListener {
    
    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);
    
    // Сервіси
    private final BoardService boardService;
    private final ThemeManager themeManager;
    
    // Ресурси локалізації
    private final ResourceBundle messages;
    
    // Компоненти інтерфейсу
    private JPanel boardPanel;
    private JPanel columnsPanel;
    private JPanel mainPanel;
    private JLabel boardTitleLabel;
    private ProjectTreePanel projectTreePanel;
    
    // Поточний стан
    private final User currentUser;
    private Board currentBoard;
    
    // Обробники подій
    private final IssueCardMouseAdapter cardDragHandler;
    
    // Колонки дошки (статус -> панель)
    private final Map<Status, BoardColumnPanel> columns = new HashMap<>();
    
    // Новые компоненты
    private SearchPanel searchPanel;
    private SearchResultPanel searchResultPanel;
    private NotificationPanel notificationPanel;
    private JPanel sidePanel;
    
    /**
     * Конструктор головного вікна
     * 
     * @param boardService сервіс дошок
     * @param messages ресурси локалізації
     * @param currentUser поточний користувач
     */
    public MainFrame(BoardService boardService, ResourceBundle messages, User currentUser) {
        this.boardService = boardService;
        this.messages = messages;
        this.currentUser = currentUser;
        this.cardDragHandler = new IssueCardMouseAdapter();
        this.themeManager = ThemeManager.getInstance();
        
        // Застосовуємо поточну тему
        themeManager.applyCurrentTheme();
        
        initializeMainWindow();
        configureListeners();
    }
    
    /**
     * Ініціалізація головного вікна програми
     */
    private void initializeMainWindow() {
        try {
            System.out.println("### Початок ініціалізації головного вікна ###");
            
            // Базові налаштування вікна
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setTitle(messages.getString("app.title") + " - " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
            ResponsiveHelper.setupFrameSize(this);
            setLocationRelativeTo(null);
            
            System.out.println("Базові налаштування вікна виконано");
            
            // Створюємо головну панель, що міститиме всі інші компоненти
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            // Верхня панель з заголовком
            JPanel headerPanel = new JPanel(new BorderLayout());
            boardTitleLabel = new JLabel("Jira Light - оберіть дошку");
            boardTitleLabel.setFont(UiConstants.HEADER_FONT);
            boardTitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            headerPanel.add(boardTitleLabel, BorderLayout.CENTER);
            
            // Информация о текущем пользователе
            JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel userLabel = new JLabel("Користувач: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
            userLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JButton logoutButton = new JButton("Вийти");
            logoutButton.addActionListener(e -> logout());
            userInfoPanel.add(userLabel);
            userInfoPanel.add(logoutButton);
            headerPanel.add(userInfoPanel, BorderLayout.EAST);
            
            contentPanel.add(headerPanel, BorderLayout.NORTH);
            
            System.out.println("Заголовок створено");
            
            try {
                // Основні розміри для компонентів
                int sideBarWidth = 250;
                
                // Панель для дошки (права частина)
                mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBorder(BorderFactory.createTitledBorder("Дошка"));
                
                // Панель колонок
                columnsPanel = new JPanel();
                columnsPanel.setLayout(new BoxLayout(columnsPanel, BoxLayout.X_AXIS));
                JScrollPane columnsScrollPane = new JScrollPane(columnsPanel);
                
                // Створюємо базову панель дошки
                boardPanel = new JPanel(new BorderLayout());
                boardPanel.add(columnsScrollPane, BorderLayout.CENTER);
                mainPanel.add(boardPanel, BorderLayout.CENTER);
                
                // Створюємо ліву панель з вертикальним BoxLayout
                JPanel leftPanel = new JPanel();
                leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
                leftPanel.setBorder(BorderFactory.createTitledBorder("Навігація"));
                
                // Ініціалізуємо дерево проектів
                System.out.println("Ініціалізація дерева проектів...");
                projectTreePanel = new ProjectTreePanel(currentUser, boardService, messages);
                projectTreePanel.setSelectionListener(this);
                
                // Додаємо дерево проектів в окремий контейнер
                JPanel projectsContainer = new JPanel(new BorderLayout());
                projectsContainer.setBorder(BorderFactory.createTitledBorder("Проекти"));
                projectsContainer.add(projectTreePanel, BorderLayout.CENTER);
                projectsContainer.setMinimumSize(new Dimension(sideBarWidth, 100));
                projectsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                // Ініціалізуємо панель пошуку
                System.out.println("Ініціалізація компонентів пошуку...");
                searchPanel = new SearchPanel(messages, boardService);
                searchPanel.setBorder(BorderFactory.createTitledBorder("Пошук"));
                searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                // Ініціалізуємо панель результатів пошуку
                searchResultPanel = new SearchResultPanel(messages);
                searchResultPanel.setBorder(BorderFactory.createTitledBorder("Результати"));
                searchResultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                // Зв'язуємо пошук і результати
                searchPanel.setSearchResultHandler(issues -> {
                    searchResultPanel.displayResults(issues);
                });
                
                // Обробник вибору задачі в результатах
                searchResultPanel.setOnIssueSelectedHandler(issue -> {
                    openIssueDialog(issue);
                });
                
                // Ініціалізуємо панель сповіщень
                notificationPanel = new NotificationPanel(messages);
                notificationPanel.setBorder(BorderFactory.createTitledBorder("Повідомлення"));
                notificationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                // Додаємо компоненти у ліву панель
                leftPanel.add(projectsContainer);
                leftPanel.add(Box.createVerticalStrut(10));
                leftPanel.add(searchPanel);
                leftPanel.add(Box.createVerticalStrut(10));
                leftPanel.add(searchResultPanel);
                leftPanel.add(Box.createVerticalStrut(10));
                leftPanel.add(notificationPanel);
                
                // Створюємо скрол для лівої панелі
                JScrollPane leftScrollPane = new JScrollPane(leftPanel);
                leftScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                
                // Створюємо розділову панель
                JSplitPane splitPane = new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        leftScrollPane,
                        mainPanel);
                splitPane.setDividerLocation(sideBarWidth + 20);
                
                contentPanel.add(splitPane, BorderLayout.CENTER);
                
                System.out.println("Компоненти UI створено успішно");
            } catch (Exception e) {
                System.err.println("ПОМИЛКА при створенні UI компонентів: " + e.getMessage());
                e.printStackTrace();
                
                // Аварійне створення простого інтерфейсу
                JPanel emergencyPanel = new JPanel(new BorderLayout());
                emergencyPanel.add(new JLabel("Помилка ініціалізації: " + e.getMessage()), BorderLayout.CENTER);
                contentPanel.add(emergencyPanel, BorderLayout.CENTER);
            }
            
            // Додаємо головну панель до вікна
            getContentPane().add(contentPanel);
            
            // Створюємо головне меню
            createMainMenu();
            
            System.out.println("### Ініціалізація головного вікна завершена ###");
        } catch (Exception e) {
            System.err.println("КРИТИЧНА ПОМИЛКА при ініціалізації головного вікна: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Створення головного меню програми
     */
    private void createMainMenu() {
        // Створюємо рядок меню
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        
        // Меню File
        javax.swing.JMenu fileMenu = new javax.swing.JMenu(messages.getString("menu.file"));
        
        // Пункт Settings
        javax.swing.JMenuItem settingsItem = new javax.swing.JMenuItem("Налаштування");
        settingsItem.addActionListener(e -> showSettingsDialog());
        fileMenu.add(settingsItem);
        
        // Додаємо роздільник
        fileMenu.addSeparator();
        
        // Пункт Logout
        javax.swing.JMenuItem logoutItem = new javax.swing.JMenuItem("Вийти з облікового запису");
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(logoutItem);
        
        // Пункт Exit
        javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem(messages.getString("menu.exit"));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Меню View
        javax.swing.JMenu viewMenu = new javax.swing.JMenu("Вигляд");
        
        // Пункт для перемикання теми
        javax.swing.JMenuItem toggleThemeItem = new javax.swing.JMenuItem(
                themeManager.getCurrentTheme() == ThemeManager.Theme.DARK ? 
                "Світла тема" : "Темна тема");
        toggleThemeItem.addActionListener(e -> {
            themeManager.toggleTheme();
            SwingUtilities.updateComponentTreeUI(this);
            toggleThemeItem.setText(
                    themeManager.getCurrentTheme() == ThemeManager.Theme.DARK ? 
                    "Світла тема" : "Темна тема");
        });
        viewMenu.add(toggleThemeItem);
        
        // Підменю масштабу
        javax.swing.JMenu scaleSubmenu = new javax.swing.JMenu("Масштаб");
        
        // Додаємо пункти масштабу
        String[] scaleLabels = {"Малий (80%)", "Середній (100%)", "Великий (120%)", "Дуже великий (150%)"};
        ThemeManager.UiScale[] scales = {
            ThemeManager.UiScale.SMALL,
            ThemeManager.UiScale.MEDIUM,
            ThemeManager.UiScale.LARGE,
            ThemeManager.UiScale.EXTRA_LARGE
        };
        
        for (int i = 0; i < scaleLabels.length; i++) {
            final ThemeManager.UiScale scale = scales[i];
            javax.swing.JMenuItem scaleItem = new javax.swing.JMenuItem(scaleLabels[i]);
            scaleItem.addActionListener(e -> {
                themeManager.setScale(scale);
                SwingUtilities.updateComponentTreeUI(this);
            });
            scaleSubmenu.add(scaleItem);
        }
        
        viewMenu.add(scaleSubmenu);
        
        // Меню Help
        javax.swing.JMenu helpMenu = new javax.swing.JMenu(messages.getString("menu.help"));
        
        // Пункт About
        javax.swing.JMenuItem aboutItem = new javax.swing.JMenuItem(messages.getString("menu.about"));
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        // Додаємо меню в рядок меню
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        // Встановлюємо меню для вікна
        setJMenuBar(menuBar);
    }
    
    /**
     * Показує діалог "Про програму"
     */
    private void showAboutDialog() {
        AboutDialog dialog = new AboutDialog(this, messages);
        dialog.setVisible(true);
    }
    
    /**
     * Показує діалог налаштувань
     */
    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this, messages);
        dialog.setVisible(true);
    }
    
    /**
     * Конфігурація обробників подій
     */
    private void configureListeners() {
        // Обробник закриття вікна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Закриваємо з'єднання з сервісами при закритті вікна
                dispose();
            }
            
            @Override
            public void windowOpened(WindowEvent e) {
                System.out.println("MainFrame: вікно відкрито");
                if (!projectTreePanel.isInitialized()) {
                    // Ініціалізуємо лише якщо потрібно
                    initializeProjectTree();
                }
            }
        });
        
        // Додаємо обробник зміни розмірів вікна
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Оновлюємо розміри колонок при зміні розміру вікна
                if (currentBoard != null) {
                    SwingUtilities.invokeLater(() -> {
                        updateColumnSizes();
                    });
                }
            }
        });
    }
    
    /**
     * Оновлення розмірів колонок при зміні розміру вікна
     */
    private void updateColumnSizes() {
        try {
            // Розраховуємо ширину колонки на основі нового розміру вікна
            int containerWidth = Math.max(getWidth() - 300, 600);
            int columnWidth = ResponsiveHelper.calculateColumnWidth(containerWidth, Status.values().length);
            
            // Встановлюємо мінімальну допустиму ширину колонки
            int minColumnWidth = 200;
            columnWidth = Math.max(columnWidth, minColumnWidth);
            
            System.out.println("updateColumnSizes: ширина колонки = " + columnWidth);
            
            // Оновлюємо ширину кожної колонки
            for (BoardColumnPanel column : columns.values()) {
                column.setPreferredSize(new Dimension(columnWidth, column.getPreferredSize().height));
            }
            
            // Оновлюємо інтерфейс
            columnsPanel.revalidate();
            columnsPanel.repaint();
        } catch (Exception e) {
            System.err.println("updateColumnSizes: помилка при оновленні розмірів: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Відкриття діалогу перегляду/редагування задачі
     * 
     * @param issue задача для перегляду/редагування
     */
    private void openIssueDialog(Issue issue) {
        if (currentBoard == null) {
            SwingHelper.showErrorDialog(this, 
                    messages.getString("app.error"), 
                    messages.getString("board.select_prompt"));
            return;
        }
        
        // Показуємо діалог для редагування задачі
        IssueDialog dialog = new IssueDialog(
            this, 
            issue, 
            currentBoard, 
            currentUser,
            boardService,
            messages
        );
        dialog.setVisible(true);
        
        // Якщо задачу було оновлено, оновлюємо дошку
        if (dialog.isIssueUpdated()) {
            log.info("Issue updated successfully");
            loadIssues(currentBoard);
            
            // Додаємо сповіщення про оновлення задачі
            String notificationMessage = "Задачу " + issue.getKey() + " '" + issue.getTitle() + 
                                       "' було оновлено";
            NotificationPanel.Notification notification = new NotificationPanel.Notification(
                    "Задачу оновлено", 
                    notificationMessage, 
                    NotificationPanel.NotificationType.ISSUE_UPDATED,
                    issue.getId());
            notificationPanel.addNotification(notification);
        }
    }
    
    /**
     * Створення панелі колонок для дошки
     * 
     * @param board поточна дошка
     */
    private void createBoardColumns(Board board) {
        try {
            System.out.println("createBoardColumns: початок створення колонок для дошки " + board.getName());
            
            // Очищаємо панель колонок
            columnsPanel.removeAll();
            columns.clear();
            
            // Якщо користувач має права адміністратора, додаємо кнопку створення нової задачі
            if (currentUser.isAdmin()) {
                JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JButton addIssueButton = new JButton("Створити задачу");
                addIssueButton.addActionListener(e -> showCreateIssueDialog());
                controlPanel.add(addIssueButton);
                
                // Додаємо панель з кнопками в нижню частину дошки
                boardPanel.add(controlPanel, BorderLayout.SOUTH);
            }
            
            // Додаємо відступ між краєм вікна та першою колонкою
            columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
            
            // Створюємо колонки для кожного статусу
            Status[] statuses = Status.values();
            System.out.println("createBoardColumns: будемо створювати " + statuses.length + " колонок");
            
            // Розраховуємо ширину колонки на основі розміру вікна
            int containerWidth = Math.max(getWidth() - 300, 600); // враховуємо ширину бічної панелі
            int columnWidth = ResponsiveHelper.calculateColumnWidth(containerWidth, statuses.length);
            System.out.println("createBoardColumns: ширина колонки = " + columnWidth);
            
            // Встановлюємо мінімальну допустиму ширину колонки
            int minColumnWidth = 200;
            columnWidth = Math.max(columnWidth, minColumnWidth);
            
            for (Status status : statuses) {
                try {
                    System.out.println("createBoardColumns: створюємо колонку для статусу " + status);
                    
                    BoardColumnPanel column = new BoardColumnPanel(
                            status,
                            messages,
                            cardDragHandler,
                            boardService
                    );
                    
                    // Встановлюємо адаптивну ширину колонки
                    column.setPreferredSize(new Dimension(columnWidth, 400));
                    
                    // Важливо: явно встановлюємо BoardService для колонки
                    column.setBoardService(boardService);
                    
                    columns.put(status, column);
                    columnsPanel.add(column);
                    
                    // Додаємо відступ між колонками
                    if (status.ordinal() < statuses.length - 1) {
                        columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
                    }
                    
                    System.out.println("createBoardColumns: колонку " + status + " успішно створено");
                } catch (Exception e) {
                    System.err.println("createBoardColumns: помилка при створенні колонки " + status + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Додаємо відступ між останньою колонкою та краєм вікна
            columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
            
            // Оновлюємо UI
            columnsPanel.revalidate();
            columnsPanel.repaint();
            boardPanel.revalidate();
            boardPanel.repaint();
            mainPanel.revalidate();
            mainPanel.repaint();
            
            System.out.println("createBoardColumns: колонки успішно створено");
        } catch (Exception e) {
            System.err.println("createBoardColumns: критична помилка при створенні колонок: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Загрузка задач на доску
     */
    private void loadIssues(Board board) {
        try {
            System.out.println("MainFrame.loadIssues: завантажуємо задачі для дошки " + board.getName());
            System.out.println("MainFrame.loadIssues: розмір колекції колонок: " + columns.size());
            
            // Очищаем все колонки перед загрузкой
            for (BoardColumnPanel column : columns.values()) {
                column.clear();
            }
            
            // Получаем задачи с сервиса вместо создания демо-задач
            List<ua.oip.jiralite.domain.Issue> issues = boardService.getBoardIssues(board);
            System.out.println("MainFrame.loadIssues: отримано " + issues.size() + " задач");
            
            // Обновляем панель поиска с новым списком задач
            if (searchPanel != null) {
                System.out.println("MainFrame.loadIssues: оновлюємо панель пошуку");
                searchPanel.setIssues(issues);
                
                // Перевіряємо, чи налаштований обробник результатів
                if (searchResultPanel != null) {
                    System.out.println("MainFrame.loadIssues: налаштовуємо обробник результатів пошуку");
                    searchPanel.setSearchResultHandler(foundIssues -> {
                        searchResultPanel.displayResults(foundIssues);
                    });
                }
            }
            
            // Выводим весь список задач для отладки
            System.out.println("MainFrame.loadIssues: список всіх задач:");
            for (ua.oip.jiralite.domain.Issue issue : issues) {
                System.out.println("  - Задача: ID=" + issue.getId() + 
                    ", назва='" + issue.getTitle() + 
                    "', статус=" + issue.getStatus() + 
                    ", ключ=" + issue.getKey());
            }
            
            // Распределяем задачи по колонкам в зависимости от статуса
            for (ua.oip.jiralite.domain.Issue issue : issues) {
                try {
                    // Получаем статус задачи и соответствующую колонку
                    Status status = issue.getStatus();
                    
                    // Если статус не задан, используем TO_DO
                    if (status == null) {
                        System.out.println("MainFrame.loadIssues: задача " + issue.getTitle() + 
                            " не має статусу, встановлюю TO_DO");
                        status = Status.TO_DO;
                        issue.setStatus(status);
                    }
                    
                    // Получаем колонку для данного статуса
                    BoardColumnPanel column = columns.get(status);
                    
                    if (column != null) {
                        System.out.println("MainFrame.loadIssues: додаємо задачу " + issue.getTitle() + " до колонки " + status);
                        // Добавляем задачу в колонку
                        column.addIssue(issue, cardDragHandler);
                    } else {
                        System.out.println("MainFrame.loadIssues: колонка для статусу " + status + " не знайдена");
                    }
                } catch (Exception e) {
                    System.err.println("MainFrame.loadIssues: помилка при додаванні задачі: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Выводим количество компонентов в каждой колонке для отладки
            for (Map.Entry<Status, BoardColumnPanel> entry : columns.entrySet()) {
                JPanel cards = null;
                for (Component comp : entry.getValue().getComponents()) {
                    if (comp instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) comp;
                        Component view = scrollPane.getViewport().getView();
                        if (view instanceof JPanel) {
                            cards = (JPanel) view;
                            break;
                        }
                    }
                }
                if (cards != null) {
                    System.out.println("MainFrame.loadIssues: колонка " + entry.getKey() + 
                        " містить " + cards.getComponentCount() + " компонентів");
                }
            }
            
            // Оновлюємо інтерфейс
            columnsPanel.revalidate();
            columnsPanel.repaint();
            
        } catch (Exception e) {
            System.err.println("MainFrame.loadIssues: помилка при завантаженні задач: " + e.getMessage());
            e.printStackTrace();
            SwingHelper.showErrorDialog(this, 
                    messages.getString("app.error"), 
                    messages.getString("board.load_error") + ": " + e.getMessage());
        }
    }
    
    /**
     * Відображення діалогу створення задачі
     */
    private void showCreateIssueDialog() {
        if (currentBoard == null) {
            SwingHelper.showErrorDialog(this, 
                    messages.getString("app.error"), 
                    messages.getString("board.select_prompt"));
            return;
        }
        
        // Створюємо нову задачу
        ua.oip.jiralite.domain.Issue issue = new ua.oip.jiralite.domain.Issue();
        issue.setStatus(Status.TO_DO);  // Початковий статус - "To Do"
        issue.setProject(currentBoard.getProject());  // Прив'язуємо до поточного проекту
        
        // Показуємо діалог для редагування задачі
        IssueDialog dialog = new IssueDialog(
            this, 
            null, 
            currentBoard, 
            currentUser,
            boardService,
            messages
        );
        dialog.setVisible(true);
        
        // Якщо користувач зберіг задачу, оновлюємо дошку
        if (dialog.isIssueCreated()) {
            log.info("Issue created successfully");
            loadIssues(currentBoard);
            
            // Додаємо сповіщення про створення задачі
            Issue createdIssue = dialog.getIssue();
            if (createdIssue != null) {
                String notificationMessage = "Задачу " + createdIssue.getKey() + " '" + 
                                           createdIssue.getTitle() + "' було створено";
                NotificationPanel.Notification notification = new NotificationPanel.Notification(
                        "Задачу створено", 
                        notificationMessage, 
                        NotificationPanel.NotificationType.ISSUE_CREATED,
                        createdIssue.getId());
                notificationPanel.addNotification(notification);
            }
        }
    }
    
    @Override
    public void onProjectSelected(Project project) {
        log.debug("Project selected: {}", project.getName());
        
        // Очищаємо дошку при зміні проекту
        currentBoard = null;
        boardTitleLabel.setText("");
        columnsPanel.removeAll();
        columnsPanel.revalidate();
        columnsPanel.repaint();
    }
    
    @Override
    public void onBoardSelected(Board board) {
        try {
            log.debug("Board selected: {}", board);
            System.out.println("Вибрано дошку: " + board.getName());
            
            // Якщо дошка не має імені, встановлюємо його
            if (board.getName() == null) {
                board.setName("Kanban Board");
            }
            
            // Зберігаємо посилання на поточну дошку
            currentBoard = board;
            
            // Оновлюємо заголовок
            boardTitleLabel.setText(
                    "Дошка: " + board.getName() + 
                    " (" + board.getProject().getName() + ")");
            
            // Створюємо колонки для дошки
            createBoardColumns(board);
            
            // Завантажуємо та відображаємо задачі
            loadIssues(board);
            
            // Додаємо повідомлення про вибір дошки
            String notificationMessage = "Вибрано дошку '" + board.getName() + "' проекту '" + 
                                       board.getProject().getName() + "'";
            NotificationPanel.Notification notification = new NotificationPanel.Notification(
                    "Дошка вибрана", 
                    notificationMessage, 
                    NotificationPanel.NotificationType.GENERAL);
            notificationPanel.addNotification(notification);
            
            System.out.println("Дошку успішно завантажено");
        } catch (Exception e) {
            System.err.println("Помилка при виборі дошки: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Явно ініціалізує дерево проектів
     */
    private void initializeProjectTree() {
        try {
            System.out.println("Явна ініціалізація дерева проектів...");
            
            // Отримуємо проекти користувача
            List<Project> projects = boardService.getProjectsByUser(currentUser);
            if (projects.isEmpty()) {
                System.out.println("УВАГА: Список проектів порожній!");
            }
            
            // Перевіряємо, чи правильно працює дерево проектів
            if (projectTreePanel != null) {
                // Перевіряємо видимість панелі
                System.out.println("Стан панелі проектів: " + 
                    "visible=" + projectTreePanel.isVisible() + 
                    ", showing=" + projectTreePanel.isShowing());
                
                // Оновлюємо вигляд панелі
                projectTreePanel.revalidate();
                projectTreePanel.repaint();
            } else {
                System.out.println("ПОМИЛКА: projectTreePanel є null!");
            }
        } catch (Exception e) {
            System.err.println("Помилка при ініціалізації дерева проектів: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Вихід з поточного облікового запису та повернення до вікна авторизації
     */
    private void logout() {
        try {
            // Отримуємо екземпляр сервісу аутентифікації
            AuthService authService = AuthService.getInstance();
            
            // Виходимо з системи
            authService.logout();
            
            // Закриваємо всі вікна Java та створюємо нове вікно авторизації
            closePreviousWindowsAndRestartLogin(authService);
        } catch (Exception e) {
            System.err.println("Помилка при виході з системи: " + e.getMessage());
            e.printStackTrace();
            
            try {
                // Навіть у випадку помилки спробуємо закрити всі вікна
                closePreviousWindowsAndRestartLogin(AuthService.getInstance());
            } catch (Exception ex) {
                ex.printStackTrace();
                
                // Остання спроба - просто створення нового вікна
                new LoginFrame(AuthService.getInstance()).setVisible(true);
            }
        }
    }
    
    /**
     * Метод для закриття всіх існуючих вікон та перезапуску вікна логіну
     */
    private void closePreviousWindowsAndRestartLogin(AuthService authService) {
        try {
            // Ховаємо поточне вікно негайно
            setVisible(false);
            
            // Отримуємо всі активні фрейми
            java.awt.Frame[] frames = java.awt.Frame.getFrames();
            
            // Закриваємо всі фрейми, крім того, з яким зараз працюємо
            for (java.awt.Frame frame : frames) {
                if (frame != this && frame.isDisplayable()) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            }
            
            // Звільняємо ресурси поточного вікна
            removeAll();
            dispose();
            
            // Запускаємо збирач сміття
            System.gc();
            
            // Створюємо нове вікно логіну в потоці EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    // Невелика пауза
                    Thread.sleep(100);
                    
                    // Створюємо та показуємо вікно авторизації
                    LoginFrame loginFrame = new LoginFrame(authService);
                    loginFrame.setVisible(true);
                    
                } catch (Exception e) {
                    System.err.println("Помилка при створенні вікна входу: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Помилка при закритті вікон: " + e.getMessage());
            e.printStackTrace();
            
            // Якщо все зовсім погано - просто створюємо нове вікно
            LoginFrame loginFrame = new LoginFrame(authService);
            loginFrame.setVisible(true);
        }
    }
} 