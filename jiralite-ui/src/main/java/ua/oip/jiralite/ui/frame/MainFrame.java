package ua.oip.jiralite.ui.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.listener.ColumnDropTarget;
import ua.oip.jiralite.ui.listener.IssueCardMouseAdapter;
import ua.oip.jiralite.ui.panel.BoardColumnPanel;
import ua.oip.jiralite.ui.panel.ProjectTreePanel;
import ua.oip.jiralite.ui.panel.ProjectTreePanel.ProjectSelectionListener;
import ua.oip.jiralite.ui.util.SwingHelper;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Головне вікно програми Jira Lite.
 * Відображає Kanban-дошку з задачами та дерево проєктів.
 */
public class MainFrame extends JFrame implements ProjectSelectionListener {
    
    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);
    
    private final User currentUser;
    private final AuthService authService;
    private final BoardService boardService;
    private final ResourceBundle messages;
    
    private ProjectTreePanel projectTreePanel;
    private JPanel boardPanel;
    private JPanel columnsPanel;
    private JLabel boardTitleLabel;
    
    private Board currentBoard;
    
    /**
     * Конструктор головного вікна
     * 
     * @param currentUser поточний користувач
     * @param authService сервіс авторизації
     * @param messages ресурси локалізації
     */
    public MainFrame(User currentUser, AuthService authService, ResourceBundle messages) {
        this.currentUser = currentUser;
        this.authService = authService;
        this.messages = messages;
        
        // Ініціалізуємо сервіс дошок для роботи з даними
        this.boardService = new BoardService();
        
        initializeUI();
        configureListeners();
    }
    
    /**
     * Ініціалізація компонентів інтерфейсу
     */
    private void initializeUI() {
        // Налаштування вікна
        setTitle(messages.getString("app.title"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(UiConstants.MAIN_FRAME_WIDTH, UiConstants.MAIN_FRAME_HEIGHT);
        setLocationRelativeTo(null);  // Центрування вікна
        
        // Створюємо панель зі спліттером для розділення дерева проєктів та дошки
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setOneTouchExpandable(true);
        
        // Ліва частина - дерево проєктів
        projectTreePanel = new ProjectTreePanel(currentUser, boardService, messages);
        projectTreePanel.setSelectionListener(this);
        
        // Права частина - дошка з задачами
        boardPanel = createBoardPanel();
        
        // Додаємо компоненти у спліттер
        splitPane.setLeftComponent(projectTreePanel);
        splitPane.setRightComponent(boardPanel);
        
        // Додаємо меню
        setJMenuBar(createMenuBar());
        
        // Встановлюємо спліттер як головний компонент вікна
        setContentPane(splitPane);
    }
    
    /**
     * Налаштування обробників подій
     */
    private void configureListeners() {
        // Обробник закриття вікна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }
    
    /**
     * Створення панелі дошки задач
     */
    private JPanel createBoardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiConstants.BACKGROUND_COLOR);
        panel.setBorder(UiConstants.DEFAULT_BORDER);
        
        // Заголовок з інформацією про поточну дошку
        boardTitleLabel = new JLabel(messages.getString("board.select_prompt"));
        boardTitleLabel.setFont(UiConstants.HEADER_FONT);
        boardTitleLabel.setBorder(BorderFactory.createEmptyBorder(
                0, 0, UiConstants.COMPONENT_SPACING, 0));
        
        // Створення панелі для колонок Kanban
        columnsPanel = new JPanel();
        columnsPanel.setLayout(new BoxLayout(columnsPanel, BoxLayout.X_AXIS));
        columnsPanel.setBackground(UiConstants.BACKGROUND_COLOR);
        
        // Додавання компонентів на панель дошки
        panel.add(boardTitleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(columnsPanel), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Створення панелі колонок для дошки
     * 
     * @param board поточна дошка
     */
    private void createBoardColumns(Board board) {
        // Очищаємо панель колонок
        columnsPanel.removeAll();
        
        // Створюємо обробник перетягування карток
        IssueCardMouseAdapter dragHandler = new IssueCardMouseAdapter();
        
        // Додаємо відступ між краєм вікна та першою колонкою
        columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
        
        // Створюємо колонки для кожного статусу
        BoardColumnPanel todoColumn = new BoardColumnPanel(
                messages.getString("column.todo"), Status.TO_DO, messages);
        
        BoardColumnPanel inProgressColumn = new BoardColumnPanel(
                messages.getString("column.in_progress"), Status.IN_PROGRESS, messages);
        
        BoardColumnPanel doneColumn = new BoardColumnPanel(
                messages.getString("column.done"), Status.DONE, messages);
        
        // Встановлюємо обробники для перетягування карток
        todoColumn.setDropTarget(new ColumnDropTarget(Status.TO_DO, boardService, messages));
        inProgressColumn.setDropTarget(new ColumnDropTarget(Status.IN_PROGRESS, boardService, messages));
        doneColumn.setDropTarget(new ColumnDropTarget(Status.DONE, boardService, messages));
        
        // Додаємо колонки на панель
        columnsPanel.add(todoColumn);
        columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
        columnsPanel.add(inProgressColumn);
        columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
        columnsPanel.add(doneColumn);
        columnsPanel.add(Box.createHorizontalStrut(UiConstants.COMPONENT_SPACING));
        
        // Додаємо "пружину" для розтягування порожнього простору
        columnsPanel.add(Box.createHorizontalGlue());
        
        // Оновлюємо інтерфейс
        columnsPanel.revalidate();
        columnsPanel.repaint();
        
        // Завантажуємо та відображаємо задачі
        // loadIssues(board, todoColumn, inProgressColumn, doneColumn, dragHandler);
    }
    
    /**
     * Створення головного меню
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Меню "Файл"
        JMenu fileMenu = new JMenu(messages.getString("menu.file"));
        JMenuItem logoutItem = new JMenuItem(messages.getString("menu.logout"));
        logoutItem.addActionListener(e -> handleLogout());
        fileMenu.add(logoutItem);
        
        // Меню "Довідка"
        JMenu helpMenu = new JMenu(messages.getString("menu.help"));
        JMenuItem aboutItem = new JMenuItem(messages.getString("menu.about"));
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        // Додавання меню в меню-бар
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    /**
     * Обробка закриття вікна
     */
    private void handleWindowClosing() {
        boolean confirmed = SwingHelper.showConfirmDialog(this, 
                messages.getString("app.confirm"), 
                messages.getString("app.exit_confirm"));
        
        if (confirmed) {
            dispose();
            System.exit(0);
        }
    }
    
    /**
     * Обробка виходу з системи
     */
    private void handleLogout() {
        boolean confirmed = SwingHelper.showConfirmDialog(this, 
                messages.getString("app.confirm"), 
                messages.getString("app.logout_confirm"));
        
        if (confirmed) {
            log.info("User {} logged out", currentUser.getUsername());
            
            // Закриваємо головне вікно
            dispose();
            
            // Відкриваємо вікно логіну
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(authService);
                loginFrame.setVisible(true);
            });
        }
    }
    
    /**
     * Відображення інформації про програму
     */
    private void showAboutDialog() {
        String message = "Jira Lite v1.0.0\n" +
                "Система управління задачами\n\n" +
                "(c) 2023 OIP NU \"Zaporizhzhia Polytechnic\"";
        
        SwingHelper.showInfoDialog(this, 
                messages.getString("menu.about"), message);
    }
    
    // Реалізація інтерфейсу ProjectSelectionListener
    
    @Override
    public void onProjectSelected(Project project) {
        log.debug("Project selected: {}", project.getName());
        
        boardTitleLabel.setText(messages.getString("project.title") + ": " + project.getName());
        
        // Очищаємо панель колонок
        columnsPanel.removeAll();
        columnsPanel.revalidate();
        columnsPanel.repaint();
    }
    
    @Override
    public void onBoardSelected(Board board) {
        log.debug("Board selected: {}", board.getName());
        
        // Зберігаємо посилання на поточну дошку
        currentBoard = board;
        
        // Оновлюємо заголовок
        boardTitleLabel.setText(
                messages.getString("board.title") + ": " + board.getName() + 
                " (" + board.getProject().getName() + ")");
        
        // Створюємо колонки для дошки
        createBoardColumns(board);
    }
} 