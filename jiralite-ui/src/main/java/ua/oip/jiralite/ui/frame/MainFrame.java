package ua.oip.jiralite.ui.frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.listener.IssueCardMouseAdapter;
import ua.oip.jiralite.ui.panel.BoardColumnPanel;
import ua.oip.jiralite.ui.panel.ProjectTreePanel;
import ua.oip.jiralite.ui.panel.ProjectTreePanel.ProjectSelectionListener;
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
        // Налаштовуємо заголовок вікна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(messages.getString("app.title"));
        
        // Встановлюємо адаптивні розміри в залежності від розміру екрану
        ResponsiveHelper.setupFrameSize(this);
        
        // Розташовуємо вікно по центру екрану
        setLocationRelativeTo(null);
        
        // Створюємо загальну структуру вікна:
        // 1. Верхня панель з заголовком
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        // Заголовок дошки
        boardTitleLabel = new JLabel("");
        boardTitleLabel.setFont(UiConstants.HEADER_FONT);
        boardTitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.add(boardTitleLabel, BorderLayout.CENTER);
        
        // 2. Ліва панель з деревом проектів
        projectTreePanel = new ProjectTreePanel(currentUser, boardService, messages);
        projectTreePanel.setSelectionListener(this);
        
        // 3. Головна панель з колонками дошки
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Створюємо панель для колонок з горизонтальним box layout
        columnsPanel = new JPanel();
        columnsPanel.setLayout(new BoxLayout(columnsPanel, BoxLayout.X_AXIS));
        
        // Додаємо скроллінг для колонок
        JScrollPane columnsScrollPane = new JScrollPane(columnsPanel);
        columnsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        columnsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        columnsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Панель для дошки
        boardPanel = new JPanel(new BorderLayout());
        boardPanel.add(columnsScrollPane, BorderLayout.CENTER);
        
        // Додаємо панель дошки до головної панелі
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        
        // Зліва розміщуємо дерево проектів
        JScrollPane projectTreeScrollPane = new JScrollPane(projectTreePanel);
        
        // Встановлюємо адаптивну ширину для панелі проектів
        int projectPanelWidth = (int)(200 * themeManager.getCurrentScale().getFactor());
        projectTreeScrollPane.setPreferredSize(new Dimension(projectPanelWidth, 600));
        
        // Створюємо розділену панель з деревом проектів та дошкою
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, 
                projectTreeScrollPane, 
                mainPanel);
        splitPane.setDividerLocation(projectPanelWidth);
        splitPane.setOneTouchExpandable(true);
        
        // Додаємо компоненти до головного вікна
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        
        // Создаем главное меню
        createMainMenu();
    }
    
    /**
     * Создание главного меню приложения
     */
    private void createMainMenu() {
        // Создаем строку меню
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        
        // Меню File
        javax.swing.JMenu fileMenu = new javax.swing.JMenu(messages.getString("menu.file"));
        
        // Пункт Settings
        javax.swing.JMenuItem settingsItem = new javax.swing.JMenuItem("Налаштування");
        settingsItem.addActionListener(e -> showSettingsDialog());
        fileMenu.add(settingsItem);
        
        // Додаємо роздільник
        fileMenu.addSeparator();
        
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
        
        // Добавляем меню в строку меню
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        // Устанавливаем меню для окна
        setJMenuBar(menuBar);
    }
    
    /**
     * Показывает диалог "О программе"
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
                log.info("Application is closing...");
                System.exit(0);
            }
        });
    }
    
    /**
     * Створення панелі колонок для дошки
     * 
     * @param board поточна дошка
     */
    private void createBoardColumns(Board board) {
        // Очищаємо панель колонок
        columnsPanel.removeAll();
        columns.clear();
        
        log.debug("Створюємо колонки для дошки {}", board.getName());
        
        // Якщо користувач має права адміністратора, додаємо кнопку створення нової задачі
        if (currentUser.isAdmin()) {
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addIssueButton = new JButton(messages.getString("issue.create"));
            addIssueButton.addActionListener(e -> showCreateIssueDialog());
            controlPanel.add(addIssueButton);
            
            // Додаємо панель з кнопками в верхню частину дошки
            boardPanel.add(controlPanel, BorderLayout.SOUTH);
        }
        
        // Додаємо відступ між краєм вікна та першою колонкою
        columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
        
        // Створюємо колонки для кожного статусу
        Status[] statuses = Status.values();
        
        // Розраховуємо ширину колонки на основі розміру вікна
        int containerWidth = getWidth() - 250; // враховуємо ширину панелі проектів
        int columnWidth = ResponsiveHelper.calculateColumnWidth(containerWidth, statuses.length);
        
        for (Status status : statuses) {
            log.debug("Створюємо колонку для статусу {}", status);
            
            BoardColumnPanel column = new BoardColumnPanel(
                    status,
                    messages,
                    cardDragHandler,
                    boardService
            );
            
            // Встановлюємо адаптивну ширину колонки
            column.setPreferredSize(new Dimension(columnWidth, 0));
            
            // Важливо: явно встановлюємо BoardService для колонки
            column.setBoardService(boardService);
            log.debug("Встановлено BoardService для колонки {}", status);
            
            columns.put(status, column);
            columnsPanel.add(column);
            
            // Додаємо відступ між колонками
            if (status.ordinal() < statuses.length - 1) {
                columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
            }
        }
        
        // Додаємо відступ між останньою колонкою та краєм вікна
        columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
        
        columnsPanel.revalidate();
        columnsPanel.repaint();
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
        log.debug("Board selected: {}", board);
        
        // Якщо дошка не має імені, встановлюємо його
        if (board.getName() == null) {
            board.setName("Kanban Board");
        }
        
        // Зберігаємо посилання на поточну дошку
        currentBoard = board;
        
        // Оновлюємо заголовок
        boardTitleLabel.setText(
                messages.getString("board.title") + ": " + board.getName() + 
                " (" + board.getProject().getName() + ")");
        
        // Створюємо колонки для дошки
        createBoardColumns(board);
        
        // Загружаем и отображаем задачи
        loadIssues(board);
    }
} 