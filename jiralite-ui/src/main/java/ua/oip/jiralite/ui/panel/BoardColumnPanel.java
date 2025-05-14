package ua.oip.jiralite.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.frame.IssueDialog;
import ua.oip.jiralite.ui.listener.ColumnDropTarget;
import ua.oip.jiralite.ui.listener.IssueCardMouseAdapter;
import ua.oip.jiralite.ui.model.IssueCardModel;
import ua.oip.jiralite.ui.util.SwingHelper;
import ua.oip.jiralite.ui.util.UiConstants;
import ua.oip.jiralite.ui.util.ThemeManager;

/**
 * Панель для отображения одной колонки Kanban-доски.
 * Содержит карточки задач соответствующего статуса.
 */
public class BoardColumnPanel extends JPanel {
    
    private final Status status;
    private final ResourceBundle messages;
    private final Map<Long, IssueCardPanel> cards = new LinkedHashMap<>();
    
    private JPanel cardsPanel;
    
    private BoardService boardService;
    private AuthService authService;
    private ThemeManager themeManager;
    
    /**
     * Конструктор панели колонки
     * 
     * @param status статус задач в колонке
     * @param messages ресурсы локализации
     */
    public BoardColumnPanel(Status status, ResourceBundle messages) {
        this.status = status;
        this.messages = messages;
        this.themeManager = ThemeManager.getInstance();
        
        initializeUI();
        addThemeChangeListener();
    }
    
    /**
     * Конструктор панели колонки с расширенными параметрами
     * 
     * @param status статус задач в колонке
     * @param messages ресурсы локализации
     * @param dragHandler обработчик перетаскивания
     * @param boardService сервис доски
     */
    public BoardColumnPanel(Status status, ResourceBundle messages, IssueCardMouseAdapter dragHandler, BoardService boardService) {
        this.status = status;
        this.messages = messages;
        this.boardService = boardService;
        this.authService = AuthService.getInstance();
        this.themeManager = ThemeManager.getInstance();
        
        System.out.println("BoardColumnPanel: конструктор з boardService для колонки " + status);
        
        initializeUI();
        addThemeChangeListener();
        
        // Регистрируем обработчик перетаскивания для этой колонки
        new ColumnDropTarget(this, status, boardService, messages);
    }
    
    /**
     * Добавляет слушателя изменения темы
     */
    private void addThemeChangeListener() {
        themeManager.addThemeChangeListener(new ThemeManager.ThemeChangeListener() {
            @Override
            public void onThemeChanged() {
                updateTheme();
            }
        });
    }
    
    /**
     * Обновляет цвета компонентов в соответствии с текущей темой
     */
    private void updateTheme() {
        // Обновляем цвет фона панели карточек
        cardsPanel.setBackground(getColumnColor());
        
        // Обновляем рамку колонки
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().textSecondary, 1, true),
                getColumnTitle(status),
                TitledBorder.CENTER, 
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Перерисовываем панель
        revalidate();
        repaint();
    }
    
    /**
     * Возвращает цвет фона колонки в зависимости от статуса и текущей темы
     */
    private Color getColumnColor() {
        // Используем цвета из текущей цветовой схемы
        switch (status) {
            case TO_DO:
                return themeManager.getCurrentScheme().todoColumn;
            case IN_PROGRESS:
                return themeManager.getCurrentScheme().inProgressColumn;
            case DONE:
                return themeManager.getCurrentScheme().doneColumn;
            default:
                return themeManager.getCurrentScheme().background;
        }
    }
    
    /**
     * Инициализация UI компонентов
     */
    private void initializeUI() {
        // Определяем заголовок колонки в зависимости от статуса
        String title = getColumnTitle(status);
        
        // Використовуємо BorderLayout для розташування компонентів
        setLayout(new BorderLayout());
        
        // Встановлюємо граничну рамку з заголовком
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().textSecondary, 1, true),
                title,
                TitledBorder.CENTER, 
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Створюємо панель для карток з вертикальним розташуванням
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(getColumnColor());
        cardsPanel.setOpaque(true);
        
        // Додаємо прокрутку для панелі з картками
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Додаємо прокрутку в центр панелі колонки
        add(scrollPane, BorderLayout.CENTER);
        
        // Встановлюємо розміри колонки
        setPreferredSize(new Dimension(220, 500));
        setMinimumSize(new Dimension(220, 300));
        
        // Можна додати якусь кнопку вдолу колонки (наприклад, "Додати задачу")
        JButton addButton = new JButton("+ Додати в " + status);
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Встановлюємо мінімальну та бажану ширину кнопки
        addButton.setMinimumSize(new Dimension(100, addButton.getPreferredSize().height));
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addButton.getPreferredSize().height));
        
        // Встановлюємо явний розмір шрифту для кращої видимості
        addButton.setFont(addButton.getFont().deriveFont(12.0f));
        
        // // Кольори для кращої видимості (закоментовано, щоб використати стандартні кольори теми)
        // Color buttonBackground = new Color(14, 110, 238, 255);
        // Color buttonText = Color.WHITE;
        // 
        // // Встановлюємо кольори
        // addButton.setBackground(buttonBackground);
        // addButton.setForeground(buttonText);
        // addButton.setFocusPainted(false);
        // addButton.setOpaque(true); 
        
        addButton.addActionListener(e -> {
            System.out.println("BoardColumnPanel: натиснуто кнопку додавання задачі в колонці " + status);
            
            // Инициализируем authService если нужно
            if (authService == null) {
                authService = AuthService.getInstance();
            }
            
            // Проверяем права пользователя
            if (!authService.canCreateIssue()) {
                JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                SwingHelper.showErrorDialog(
                    mainFrame,
                    messages.getString("app.error"),
                    messages.getString("permission.not_allowed")
                );
                System.out.println("BoardColumnPanel: пользователь не имеет прав на создание задачи");
                return;
            }
            
            if (boardService != null) {
                System.out.println("BoardColumnPanel: boardService не null, продовжуємо");
                
                // Знаходимо батьківське вікно для показу діалогу
                JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (mainFrame != null) {
                    System.out.println("BoardColumnPanel: знайдено головне вікно");
                    
                    try {
                        System.out.println("BoardColumnPanel: спроба створити нову задачу");
                        
                        // Отримуємо поточну дошку з батьківського вікна MainFrame
                        Board currentBoard = null;
                        
                        // Спроба отримати поточну дошку через reflection з MainFrame
                        try {
                            java.lang.reflect.Field boardField = mainFrame.getClass().getDeclaredField("currentBoard");
                            boardField.setAccessible(true);
                            currentBoard = (Board) boardField.get(mainFrame);
                            System.out.println("BoardColumnPanel: отримано дошку через reflection: " + 
                                (currentBoard != null ? currentBoard.getName() : "null"));
                        } catch (Exception ex) {
                            System.out.println("BoardColumnPanel: не вдалося отримати дошку через reflection: " + ex.getMessage());
                        }
                        
                        // Якщо не вдалося отримати, створюємо нову
                        if (currentBoard == null) {
                            System.out.println("BoardColumnPanel: створюємо нову дошку");
                            currentBoard = new Board();
                            currentBoard.setId(1L);
                            currentBoard.setName("Kanban Board");
                            
                            // Створюємо демо-проект
                            Project project = new Project();
                            project.setId(1L);
                            project.setName("Demo Project");
                            project.setKey("DEMO");
                            
                            currentBoard.setProject(project);
                        }
                        
                        // Створюємо нову задачу з поточним статусом
                        Issue issue = new Issue();
                        issue.setStatus(status);
                        issue.setProject(currentBoard.getProject());
                        
                        System.out.println("BoardColumnPanel: показуємо діалог створення задачі зі статусом " + status);
                        
                        // Переконуємося, що статус задачі відповідає статусу колонки
                        if (issue.getStatus() != status) {
                            System.out.println("BoardColumnPanel: примусово встановлюємо статус " + status + " для нової задачі");
                            issue.setStatus(status);
                        }
                        
                        // Показуємо діалог для редагування задачі
                        IssueDialog dialog = new IssueDialog(
                            mainFrame, 
                            issue,  // Передаємо задачу зі статусом колонки
                            currentBoard, 
                            null, // Отримаємо поточного користувача з сесії
                            boardService,
                            ResourceBundle.getBundle("i18n.labels")
                        );
                        dialog.setVisible(true);
                        
                        // Якщо користувач зберіг задачу, оновлюємо дошку
                        if (dialog.isIssueCreated()) {
                            System.out.println("BoardColumnPanel: задачу створено, оновлюємо дошку");
                            
                            // Додаткова перевірка після створення задачі
                            // Перевіряємо, чи є нова задача з потрібним статусом у глобальному списку
                            System.out.println("BoardColumnPanel: перевіряємо оновлення статусів у глобальному списку");
                            try {
                                // Находим demoIssues через рефлексию
                                Field globalIssuesField = boardService.getClass().getDeclaredField("globalIssues");
                                globalIssuesField.setAccessible(true);
                                
                                System.out.println("BoardColumnPanel: перевіряємо оновлення статусів у глобальному списку");
                                
                                // Обновление UI
                                if (mainFrame != null) {
                                    Method loadIssuesMethod = mainFrame.getClass().getDeclaredMethod("loadIssues", Board.class);
                                    loadIssuesMethod.setAccessible(true);
                                    loadIssuesMethod.invoke(mainFrame, currentBoard);
                                    System.out.println("BoardColumnPanel: успішно викликано метод loadIssues з MainFrame");
                                }
                            } catch (Exception ex) {
                                System.out.println("BoardColumnPanel: помилка при роботі з демо-задачами: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        } else {
                            System.out.println("BoardColumnPanel: створення задачі скасовано користувачем");
                        }
                    } catch (Exception ex) {
                        System.err.println("Помилка при створенні нової задачі: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("BoardColumnPanel: не вдалося знайти головне вікно");
                }
            } else {
                System.out.println("BoardColumnPanel: boardService == null, не можемо продовжити");
            }
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setMinimumSize(new Dimension(100, 40));
        bottomPanel.setPreferredSize(new Dimension(getPreferredSize().width, 40));
        bottomPanel.add(addButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Добавляет задачу в колонку
     * 
     * @param issue модель задачи
     * @param dragHandler обработчик перетягування карток
     */
    public void addIssue(IssueCardModel issue, Consumer<IssueCardPanel> cardConsumer) {
        // Створюємо картку задачі
        IssueCardPanel card = new IssueCardPanel(issue, messages);
        card.setVisible(true);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Применяем потребителя к карточке, если он задан
        if (cardConsumer != null) {
            cardConsumer.accept(card);
        } else {
            // Якщо обробник не переданий, додаємо стандартний обробник для Drag and Drop
            IssueCardMouseAdapter dragHandler = new IssueCardMouseAdapter();
            card.addMouseListener(dragHandler);
            card.addMouseMotionListener(dragHandler);
            
            // Реєструємо DropTarget для карточки
            new DropTarget(card, DnDConstants.ACTION_MOVE, 
                new ColumnDropTarget(this, status, boardService, messages));
        }
        
        // Додаємо картку до колонки в начало (после первого компонента, если он есть)
        if (cardsPanel.getComponentCount() > 0) {
            cardsPanel.add(Box.createVerticalStrut(UiConstants.COMPONENT_SPACING), 0);
            cardsPanel.add(card, 1);
        } else {
            cardsPanel.add(card, 0);
        }
        
        cards.put(issue.getId(), card);
        
        // Не устанавливаем жёсткие размеры, позволяем карточке самой определять свой размер
        
        // Оновлюємо відображення
        cardsPanel.revalidate();
        cardsPanel.repaint();
        revalidate();
        repaint();
    }
    
    /**
     * Добавляет карточку задачи в колонку из доменной модели
     * 
     * @param issue доменная модель задачи
     * @param dragHandler обработчик перетаскивания
     */
    public void addIssue(ua.oip.jiralite.domain.Issue issue, IssueCardMouseAdapter dragHandler) {
        if (issue == null) return;
        
        System.out.println("BoardColumnPanel.addIssue: додаємо задачу " + issue.getTitle() + " до колонки " + status);
        
        // Создаем UI модель из доменной модели
        IssueCardModel issueModel = new IssueCardModel();
        issueModel.setId(issue.getId());
        
        // Устанавливаем ключ и название задачи
        issueModel.setKey(issue.getKey() != null ? issue.getKey() : "ISSUE-" + issue.getId());
        issueModel.setTitle(issue.getTitle());
        issueModel.setDescription(issue.getDescription());
        
        // Используем статус из задачи, если он задан, иначе используем статус колонки
        if (issue.getStatus() != null) {
            issueModel.setStatus(issue.getStatus());
        } else {
            issueModel.setStatus(status);
        }
        
        // Устанавливаем приоритет из задачи, если задан
        if (issue.getPriority() != null) {
            issueModel.setPriority(issue.getPriority());
        } else {
            issueModel.setPriority(ua.oip.jiralite.domain.enums.Priority.MEDIUM);
        }
        
        // Устанавливаем исполнителя, если он есть
        if (issue.getAssignee() != null) {
            issueModel.setAssigneeName(issue.getAssignee().getFullName());
        }
        
        // Добавляем карточку, используя лямбда-выражение для настройки MouseListener
        addIssue(issueModel, card -> {
            System.out.println("BoardColumnPanel.addIssue: встановлюємо обробник перетягування для картки " + issueModel.getTitle());
            
            // Активуємо перетягування для картки
            if (dragHandler != null) {
                // Використовуємо переданий обробник
                System.out.println("BoardColumnPanel.addIssue: використовуємо переданий обробник перетягування");
                
                // Додаємо картці підтримку перетягування через явний виклик
                dragHandler.addCardDragSupport(card);
                
                // Додаємо також слухачі подій миші
                card.addMouseListener(dragHandler);
                card.addMouseMotionListener(dragHandler);
            } else {
                // Створюємо новий обробник
                System.out.println("BoardColumnPanel.addIssue: створюємо новий обробник перетягування");
                IssueCardMouseAdapter newDragHandler = new IssueCardMouseAdapter();
                
                // Додаємо картці підтримку перетягування
                newDragHandler.addCardDragSupport(card);
                
                // Додаємо слухачі подій миші
                card.addMouseListener(newDragHandler);
                card.addMouseMotionListener(newDragHandler);
            }
            
            // Встановлюємо курсор
            card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            
            // Реєструємо картку як ціль перетягування
            new DropTarget(card, DnDConstants.ACTION_MOVE, 
                new ColumnDropTarget(this, status, boardService, messages));
        });
        
        System.out.println("BoardColumnPanel.addIssue: cardsPanel містить " + cardsPanel.getComponentCount() + " компонентів після додавання задачі");
    }
    
    /**
     * Удаляет задачу из колонки
     * 
     * @param issueId ID задачи для удаления
     */
    public void removeIssue(Long issueId) {
        IssueCardPanel cardToRemove = null;
        
        // Шукаємо картку для видалення
        for (IssueCardPanel card : cards.values()) {
            if (card.getIssueModel().getId().equals(issueId)) {
                cardToRemove = card;
                break;
            }
        }
        
        // Видаляємо картку, якщо знайшли
        if (cardToRemove != null) {
            int index = cardsPanel.getComponentCount() - 1; // Індекс останньої карти
            cardsPanel.remove(cardToRemove);
            
            // Видаляємо також відступ перед карткою, якщо це не перша картка
            if (index > 0 && index < cardsPanel.getComponentCount()) {
                cardsPanel.remove(index - 1); // Видаляємо відступ
            }
            
            cards.remove(issueId);
            
            // Оновлюємо відображення
            revalidate();
            repaint();
        }
    }
    
    /**
     * Очищает колонку от всех карточек
     */
    public void clear() {
        System.out.println("BoardColumnPanel.clear: очищаємо колонку " + status);
        
        // Видаляємо всі компоненти з панелі карток
        cardsPanel.removeAll();
        cards.clear();
        
        // Додаємо невидимий компонент для заповнення простору
        cardsPanel.add(Box.createVerticalGlue());
        
        // Оновлюємо відображення
        cardsPanel.revalidate();
        cardsPanel.repaint();
        revalidate();
        repaint();
        
        System.out.println("BoardColumnPanel.clear: колонка " + status + " містить " + cardsPanel.getComponentCount() + " компонентів після очищення");
    }
    
    /**
     * Возвращает статус колонки
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Возвращает заголовок колонки в зависимости от статуса
     */
    private String getColumnTitle(Status status) {
        switch (status) {
            case TO_DO:
                return messages.getString("column.todo");
            case IN_PROGRESS:
                return messages.getString("column.in_progress");
            case DONE:
                return messages.getString("column.done");
            default:
                return status.name();
        }
    }
    
    /**
     * Обновляет карточку задачи по данным из доменной модели
     */
    public void refreshCard(Issue issue) {
        IssueCardPanel card = cards.get(issue.getId());
        if (card != null) card.refresh(issue);
    }
    
    /**
     * Восстанавливает карточку в колонке, если она была удалена при перетаскивании
     * 
     * @param card карточка задачи для восстановления
     */
    public void restoreCard(IssueCardPanel card) {
        if (card != null && card.getIssueModel() != null) {
            Long issueId = card.getIssueModel().getId();
            
            // Если карточка отсутствует в коллекции, восстанавливаем ее
            if (!cards.containsKey(issueId)) {
                cards.put(issueId, card);
                
                // Добавляем физически карточку в панель с отступом
                if (!Arrays.asList(cardsPanel.getComponents()).contains(card)) {
                    cardsPanel.add(Box.createVerticalStrut(UiConstants.COMPONENT_SPACING));
                    cardsPanel.add(card);
                }
            }
            
            // В любом случае делаем карточку видимой
            card.setVisible(true);
            
            // Обновляем UI
            revalidate();
            repaint();
        }
    }
    
    /**
     * Добавляет задачу в колонку с созданием новой карточки из модели
     * Это предотвращает проблемы с одновременным нахождением компонента в разных контейнерах
     * 
     * @param issueModel модель задачи
     */
    public void addIssueWithNewCard(IssueCardModel issueModel) {
        if (issueModel == null) return;
        
        // Удаляем существующую карточку с таким же ID, если есть
        Long issueId = issueModel.getId();
        if (issueId != null && cards.containsKey(issueId)) {
            removeIssue(issueId);
        }
        
        // Добавляем карточку в колонку через стандартный метод
        addIssue(issueModel, card -> {
            // Добавляем обработчик перетаскивания
            IssueCardMouseAdapter dragHandler = new IssueCardMouseAdapter();
            
            // Додаємо підтримку перетягування
            dragHandler.addCardDragSupport(card);
            
            // Додаємо слухачів подій миші
            card.addMouseListener(dragHandler);
            card.addMouseMotionListener(dragHandler);
            
            // Встановлюємо курсор
            card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            
            // Реєструємо картку як ціль для перетягування
            new DropTarget(card, DnDConstants.ACTION_MOVE, 
                    new ColumnDropTarget(this, status, boardService, messages));
        });
    }
    
    /**
     * Добавляет задачу в начало колонки с созданием новой карточки из модели
     * 
     * @param issueModel модель задачи
     */
    public void addIssueToTop(IssueCardModel issueModel) {
        if (issueModel == null) return;
        
        // Удаляем существующую карточку с таким же ID, если есть
        Long issueId = issueModel.getId();
        if (issueId != null && cards.containsKey(issueId)) {
            removeIssue(issueId);
        }
        
        // Создаем карточку задачи
        IssueCardPanel card = new IssueCardPanel(issueModel, messages);
        card.setVisible(true);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Добавляем обработчик перетаскивания
        IssueCardMouseAdapter dragHandler = new IssueCardMouseAdapter();
        
        // Додаємо підтримку перетягування
        dragHandler.addCardDragSupport(card);
        
        // Додаємо слухачів подій миші
        card.addMouseListener(dragHandler);
        card.addMouseMotionListener(dragHandler);
        
        // Встановлюємо курсор
        card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        
        // Реєструємо картку як ціль для перетягування
        new DropTarget(card, DnDConstants.ACTION_MOVE, 
                new ColumnDropTarget(this, status, boardService, messages));
        
        // Не устанавливаем жёсткие размеры, позволяем карточке самой определять свой размер
        
        // Добавляем карточку в начало панели (после первого компонента, если он есть)
        if (cardsPanel.getComponentCount() > 0) {
            cardsPanel.add(Box.createVerticalStrut(UiConstants.COMPONENT_SPACING), 0);
            cardsPanel.add(card, 1);
        } else {
            cardsPanel.add(card, 0);
        }
        
        cards.put(issueModel.getId(), card);
        
        // Обновляем отображение
        cardsPanel.revalidate();
        cardsPanel.repaint();
        revalidate();
        repaint();
        
        System.out.println("BoardColumnPanel.addIssueToTop: добавлена карточка " + 
            issueModel.getTitle() + " в начало колонки " + status);
    }
    
    /**
     * Встановлює сервіс дошки для цієї колонки
     * 
     * @param boardService сервіс дошки
     */
    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }
} 