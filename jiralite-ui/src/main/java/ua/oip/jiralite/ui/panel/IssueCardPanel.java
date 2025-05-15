package ua.oip.jiralite.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.ui.model.IssueCardModel;
import ua.oip.jiralite.ui.util.ThemeManager;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Панель для отображения карточки задачи на Kanban-доске
 */
public final class IssueCardPanel extends JPanel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final transient DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    private final IssueCardModel issueModel;
    private transient final ResourceBundle messages;
    private transient final ThemeManager themeManager;
    
    private transient final JTextArea titleTextArea;
    private transient final JLabel statusLabel;
    private transient final JLabel assigneeLabel;
    
    // Константи дизайну
    private static final int CORNER_RADIUS = 8;
    private static final int SHADOW_SIZE = 4;
    
    // Відстежуємо первіначальний колір фону для коректної роботи ховера
    private Color initialBackgroundColor;
    
    /**
     * Конструктор карточки задачі
     * 
     * @param issue модель задачі
     * @param messages ресурси локалізації
     */
    public IssueCardPanel(IssueCardModel issue, ResourceBundle messages) {
        this.issueModel = issue;
        this.messages = messages;
        this.themeManager = ThemeManager.getInstance();
        
        // Використовуємо null layout для точного позиціонування
        setLayout(null);
        
        // Робимо панель прозорою для правильного відображення тіні та скруглених кутів
        setOpaque(false);
        
        // Створюємо вкладену панель для контенту з відповідним макетом
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(5, 5));
        contentPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        contentPanel.setOpaque(true);
        
        // Використовуємо кольори з менеджера тем
        contentPanel.setBackground(themeManager.getCurrentScheme().cardBackground);
        this.initialBackgroundColor = themeManager.getCurrentScheme().cardBackground;
        
        // Адаптивні розміри на основі масштабу
        float scale = themeManager.getCurrentScale().getFactor();
        // Збільшуємо розмір картки для кращого відображення тексту
        int cardWidth = (int)(270 * scale);
        int cardHeight = (int)(200 * scale);
        
        setPreferredSize(new Dimension(cardWidth, cardHeight));
        setMinimumSize(new Dimension(cardWidth, cardHeight));
        setMaximumSize(new Dimension(cardWidth, cardHeight));
        
        // Розміщуємо контентну панель з урахуванням тіні
        contentPanel.setBounds(0, 0, cardWidth - SHADOW_SIZE, cardHeight - SHADOW_SIZE);
        add(contentPanel);
        
        // Верхняя панель с ключом и статусом
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.setOpaque(false);
        
        JLabel keyLabel = new JLabel(issueModel.getKey());
        keyLabel.setForeground(themeManager.getCurrentScheme().accentColor);
        keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD, 12f * scale));
        
        // Ограничиваем ширину метки ключа, чтобы длинные ключи не перекрывали статус
        keyLabel.setPreferredSize(new Dimension((int)(110 * scale), keyLabel.getPreferredSize().height));
        keyLabel.setMinimumSize(new Dimension((int)(110 * scale), keyLabel.getPreferredSize().height));
        keyLabel.setMaximumSize(new Dimension((int)(110 * scale), keyLabel.getPreferredSize().height));
        
        // Добавляем прослушиватель мыши к keyLabel
        addMouseProxyListenerToComponent(keyLabel);
        
        statusLabel = new JLabel(UiConstants.statusCaption(issueModel.getStatus()));
        // Добавляем отступ справа для статуса
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f * scale));
        
        // Добавляем прослушиватель мыши к statusLabel
        addMouseProxyListenerToComponent(statusLabel);
        
        // Убираем текстовый статус "In", оставляем только для завершенных задач
        switch (issueModel.getStatus()) {
            case TO_DO:
                statusLabel.setForeground(themeManager.getCurrentScheme().textSecondary);
                statusLabel.setText(""); // Убираем текст
                break;
            case IN_PROGRESS:
                statusLabel.setForeground(themeManager.getCurrentScheme().info);
                statusLabel.setText(""); // Убираем текст
                break;
            case DONE:
                statusLabel.setForeground(themeManager.getCurrentScheme().success);
                statusLabel.setText("✓"); // Значок галочки для завершенных задач
                break;
            default:
                statusLabel.setForeground(themeManager.getCurrentScheme().textPrimary);
                statusLabel.setText("");
        }
        
        // Создаем центральную панель для размещения индикатора приоритета
        JPanel centerTopPanel = new JPanel();
        centerTopPanel.setLayout(new BoxLayout(centerTopPanel, BoxLayout.X_AXIS));
        centerTopPanel.setOpaque(false);
        
        // Добавляем индикатор приоритета в верхнюю часть карточки
        // Формируем мини-значок приоритета
        Priority priority = issueModel.getPriority();
        if (priority != null) {
            System.out.println("IssueCardPanel: Створюємо значок пріоритету для " + issueModel.getKey() + ", пріоритет: " + issueModel.getPriority());
            JPanel priorityBadge = createPriorityBadge(priority);
            centerTopPanel.add(priorityBadge);
            centerTopPanel.add(Box.createHorizontalGlue());
        } else {
            System.out.println("IssueCardPanel: Пріоритет для " + issueModel.getKey() + " не задано, використовуємо значення за замовчуванням");
            // Если приоритет не задан, показываем значок с приоритетом MEDIUM
            JPanel priorityBadge = createPriorityBadge(Priority.MEDIUM);
            centerTopPanel.add(priorityBadge);
            centerTopPanel.add(Box.createHorizontalGlue());
        }
        
        topPanel.add(keyLabel, BorderLayout.WEST);
        topPanel.add(centerTopPanel, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.EAST);
        
        // Контейнер для основного содержимого
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setOpaque(false);
        
        // Замінюємо JLabel на JTextArea для заголовка, щоб підтримувати перенос тексту
        String titleText = issueModel.getTitle();
        // Ограничиваем длину заголовка, если он слишком длинный, используя многоточие в середине
        if (titleText.length() > 50) {
            int halfLength = 23; // Половина от максимальной длины (50/2 - 2 символа для многоточия)
            titleText = titleText.substring(0, halfLength) + "..." + 
                       titleText.substring(titleText.length() - halfLength);
        }
        titleTextArea = new JTextArea(titleText);
        titleTextArea.setFont(titleTextArea.getFont().deriveFont(Font.BOLD, 13f * scale));
        titleTextArea.setForeground(themeManager.getCurrentScheme().textPrimary);
        titleTextArea.setBackground(themeManager.getCurrentScheme().cardBackground);
        titleTextArea.setEditable(false);
        titleTextArea.setWrapStyleWord(true);
        titleTextArea.setLineWrap(true);
        titleTextArea.setRows(2);
        titleTextArea.setBorder(BorderFactory.createEmptyBorder());
        titleTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Отключаем возможность выделения текста для решения проблемы перетаскивания
        titleTextArea.setHighlighter(null);
        titleTextArea.setFocusable(false);
        // Делаем текстовые компоненты прозрачными для событий мыши
        titleTextArea.setEnabled(true);
        titleTextArea.enableInputMethods(false);
        
        // Добавляем прослушиватель мыши
        addMouseProxyListenerToComponent(titleTextArea);
        
        // Обмежуємо ширину текстового поля
        int textWidth = cardWidth - 50; // Враховуємо відступи
        titleTextArea.setPreferredSize(new Dimension(textWidth, (int)(40 * scale)));
        titleTextArea.setMaximumSize(new Dimension(textWidth, (int)(40 * scale)));
        
        // Метка для назначенного пользователя
        assigneeLabel = new JLabel();
        assigneeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        assigneeLabel.setForeground(themeManager.getCurrentScheme().textSecondary);
        assigneeLabel.setFont(assigneeLabel.getFont().deriveFont(Font.ITALIC, 12f * scale));
        
        // Добавляем прослушиватель мыши к assigneeLabel
        addMouseProxyListenerToComponent(assigneeLabel);
        
        // Встановлюємо текст призначеного користувача
        if (issueModel.getAssigneeName() != null && !issueModel.getAssigneeName().isEmpty() &&
            !issueModel.getAssigneeName().equals(messages.getString("issue.not_assigned"))) {
            assigneeLabel.setText(messages.getString("issue.assignee") + ": " + issueModel.getAssigneeName());
        } else {
            assigneeLabel.setText(messages.getString("issue.not_assigned"));
        }
        
        // Додаємо короткий опис задачі у вигляді JTextArea для кращого відображення
        final JTextArea descriptionTextArea;
        
        // Добавляем описание (если есть)
        if (issueModel.getDescription() != null && !issueModel.getDescription().isEmpty()) {
            String description = issueModel.getDescription();
            // Обрезаем описание, если оно слишком длинное
            if (description.length() > 50) {
                description = description.substring(0, 47) + "...";
            }
            System.out.println("IssueCardPanel: Опис задачі " + issueModel.getKey() + ": " +
                description);
        }
        
        // Всегда создаем текстовое поле с описанием, даже если описание пустое
        String shortDesc = messages.getString("issue.no_description");
        if (issueModel.getDescription() != null && !issueModel.getDescription().isEmpty()) {
            shortDesc = issueModel.getDescription();
            // Ограничиваем длину описания более строго, используя эллипсис в середине для очень длинных описаний
            if (shortDesc.length() > 75) {
                int halfLength = 35; // Половина от максимальной длины (75/2 - 2 символа для многоточия)
                shortDesc = shortDesc.substring(0, halfLength) + "..." + 
                           shortDesc.substring(shortDesc.length() - halfLength);
            }
        }
        
        final JTextArea textArea = new JTextArea(shortDesc);
        textArea.setForeground(themeManager.getCurrentScheme().textSecondary);
        // Сделаем шрифт немного крупнее для лучшей видимости
        textArea.setFont(textArea.getFont().deriveFont(12f * scale));
        textArea.setBackground(themeManager.getCurrentScheme().cardBackground);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setRows(3); // Увеличиваем количество строк с 2 до 3
        textArea.setBorder(BorderFactory.createEmptyBorder());
        textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Отключаем возможность выделения текста
        textArea.setHighlighter(null);
        textArea.setFocusable(false);
        textArea.enableInputMethods(false);
        
        // Добавляем прослушиватель мыши
        addMouseProxyListenerToComponent(textArea);
        
        // Обмежуємо ширину текстового поля
        textArea.setPreferredSize(new Dimension(textWidth, (int)(55 * scale))); // Увеличиваем высоту с 45 до 55
        textArea.setMaximumSize(new Dimension(textWidth, (int)(55 * scale)));
        
        descriptionTextArea = textArea;
        
        // Добавляем компоненты в контентную панель
        mainContentPanel.add(titleTextArea);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Увеличиваем отступ с 5 до 8
        
        // Всегда добавляем описание
        mainContentPanel.add(descriptionTextArea);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Увеличиваем отступ с 5 до 8
        
        mainContentPanel.add(assigneeLabel);
        
        // Добавление компонентов на панель
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(mainContentPanel, BorderLayout.CENTER);
        
        // Добавляем контекстное меню
        addContextMenu();
        
        // Встановлюємо стилі ховеру для картки
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Используем текущую схему цветов для определения цвета при наведении
                Color hoverColor;
                if (themeManager.getCurrentTheme() == ThemeManager.Theme.DARK) {
                    // В темной теме делаем карточку светлее при наведении
                    hoverColor = new Color(
                        Math.min(initialBackgroundColor.getRed() + 30, 255),
                        Math.min(initialBackgroundColor.getGreen() + 30, 255),
                        Math.min(initialBackgroundColor.getBlue() + 30, 255)
                    );
                } else {
                    // В светлой теме делаем карточку темнее при наведении
                    hoverColor = initialBackgroundColor.brighter();
                }
                
                contentPanel.setBackground(hoverColor);
                titleTextArea.setBackground(hoverColor);
                if (descriptionTextArea != null) {
                    descriptionTextArea.setBackground(hoverColor);
                }
                repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Вернуть исходный цвет из текущей схемы
                initialBackgroundColor = themeManager.getCurrentScheme().cardBackground;
                contentPanel.setBackground(initialBackgroundColor);
                titleTextArea.setBackground(initialBackgroundColor);
                if (descriptionTextArea != null) {
                    descriptionTextArea.setBackground(initialBackgroundColor);
                }
                repaint();
            }
        });
        
        setVisible(true);
        
        // Добавляем слушатель изменения темы
        themeManager.addThemeChangeListener(new ThemeManager.ThemeChangeListener() {
            @Override
            public void onThemeChanged() {
                updateTheme(contentPanel, titleTextArea, descriptionTextArea);
            }
        });
    }
    
    /**
     * Обновляет цвета карточки при изменении темы
     */
    private void updateTheme(JPanel contentPanel, JTextArea titleTextArea, JTextArea descriptionTextArea) {
        initialBackgroundColor = themeManager.getCurrentScheme().cardBackground;
        
        // Обновляем фон компонентов
        contentPanel.setBackground(initialBackgroundColor);
        titleTextArea.setBackground(initialBackgroundColor);
        if (descriptionTextArea != null) {
            descriptionTextArea.setBackground(initialBackgroundColor);
        }
        
        // Обновляем цвет текста
        titleTextArea.setForeground(themeManager.getCurrentScheme().textPrimary);
        if (descriptionTextArea != null) {
            descriptionTextArea.setForeground(themeManager.getCurrentScheme().textSecondary);
        }
        
        // Обновляем цвет текста для меток
        statusLabel.setForeground(getStatusColor(issueModel.getStatus()));
        assigneeLabel.setForeground(themeManager.getCurrentScheme().textSecondary);
        
        // Обновляем индикатор приоритета
        updatePriorityBadge();
        
        // Обновляем размеры карточки при изменении масштаба
        updateCardSize(contentPanel, titleTextArea, descriptionTextArea);
        
        repaint();
    }
    
    /**
     * Обновляет размеры карточки в соответствии с текущим масштабом
     */
    private void updateCardSize(JPanel contentPanel, JTextArea titleTextArea, JTextArea descriptionTextArea) {
        float scale = themeManager.getCurrentScale().getFactor();
        
        // Обновляем размеры карточки
        int cardWidth = (int)(270 * scale);
        int cardHeight = (int)(200 * scale);
        
        setPreferredSize(new Dimension(cardWidth, cardHeight));
        setMinimumSize(new Dimension(cardWidth, cardHeight));
        setMaximumSize(new Dimension(cardWidth, cardHeight));
        
        // Обновляем размеры контентной панели
        contentPanel.setBounds(0, 0, cardWidth - SHADOW_SIZE, cardHeight - SHADOW_SIZE);
        
        // Обновляем шрифты
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f * scale));
        titleTextArea.setFont(titleTextArea.getFont().deriveFont(Font.BOLD, 13f * scale));
        assigneeLabel.setFont(assigneeLabel.getFont().deriveFont(Font.ITALIC, 12f * scale));
        
        if (descriptionTextArea != null) {
            descriptionTextArea.setFont(descriptionTextArea.getFont().deriveFont(12f * scale));
        }
        
        // Обновляем размеры текстовых полей
        int textWidth = cardWidth - 50; // Учитываем отступы
        titleTextArea.setPreferredSize(new Dimension(textWidth, (int)(40 * scale)));
        titleTextArea.setMaximumSize(new Dimension(textWidth, (int)(40 * scale)));
        
        if (descriptionTextArea != null) {
            descriptionTextArea.setPreferredSize(new Dimension(textWidth, (int)(55 * scale)));
            descriptionTextArea.setMaximumSize(new Dimension(textWidth, (int)(55 * scale)));
        }
        
        // Сообщаем родительскому контейнеру о необходимости перерисовки
        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }
    
    /**
     * Обновляет индикатор приоритета в соответствии с текущей темой
     */
    private void updatePriorityBadge() {
        // Находим компонент с индикатором приоритета в верхней панели
        for (Component component : getComponents()) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                for (Component innerComponent : panel.getComponents()) {
                    if (innerComponent instanceof JPanel) {
                        JPanel innerPanel = (JPanel) innerComponent;
                        for (Component topComponent : innerPanel.getComponents()) {
                            if (topComponent instanceof JPanel && topComponent.getName() != null && 
                                topComponent.getName().equals("priorityBadge")) {
                                // Удаляем старый индикатор и создаем новый
                                innerPanel.remove(topComponent);
                                innerPanel.add(createPriorityBadge(issueModel.getPriority()));
                                innerPanel.revalidate();
                                innerPanel.repaint();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Возвращает цвет для статуса в соответствии с текущей темой
     */
    private Color getStatusColor(ua.oip.jiralite.domain.enums.Status status) {
        switch (status) {
            case TO_DO:
                return themeManager.getCurrentScheme().textSecondary;
            case IN_PROGRESS:
                return themeManager.getCurrentScheme().info;
            case DONE:
                return themeManager.getCurrentScheme().success;
            default:
                return themeManager.getCurrentScheme().textPrimary;
        }
    }
    
    /**
     * Малюємо картку із скругленими кутами та тінню
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth() - SHADOW_SIZE;
        int height = getHeight() - SHADOW_SIZE;
        
        // Малюємо тінь с учетом текущей темы
        Color shadowColor = themeManager.getCurrentScheme().shadowColor;
        g2d.setColor(shadowColor);
        g2d.fill(new RoundRectangle2D.Float(SHADOW_SIZE, SHADOW_SIZE, width, height, CORNER_RADIUS, CORNER_RADIUS));
        
        g2d.dispose();
    }
    
    /**
     * Создает и добавляет контекстное меню к карточке
     */
    private void addContextMenu() {
        javax.swing.JPopupMenu popupMenu = new javax.swing.JPopupMenu();
        
        // Получаем сервис аутентификации
        ua.oip.jiralite.service.AuthService authService = ua.oip.jiralite.service.AuthService.getInstance();
        
        // Пункт "Редактировать" (только если есть права)
        if (authService.canEditIssue()) {
            javax.swing.JMenuItem editItem = new javax.swing.JMenuItem(messages.getString("app.edit"));
            editItem.addActionListener(e -> editIssue());
            popupMenu.add(editItem);
        }
        
        // Пункт "Удалить" (только если есть права)
        if (authService.canDeleteIssue()) {
            javax.swing.JMenuItem deleteItem = new javax.swing.JMenuItem(messages.getString("app.delete"));
            deleteItem.addActionListener(e -> deleteIssue());
            popupMenu.add(deleteItem);
        }
        
        // Добавляем слушатель для показа контекстного меню только если есть пункты меню
        if (popupMenu.getComponentCount() > 0) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopup(e);
                    }
                }
                
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showPopup(e);
                    }
                }
                
                private void showPopup(java.awt.event.MouseEvent e) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            });
        }
    }
    
    /**
     * Редактирование задачи
     */
    private void editIssue() {
        javax.swing.JFrame mainFrame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        if (mainFrame != null) {
            try {
                // Находим текущую доску через рефлексию - такой подход уже используется в проекте
                java.lang.reflect.Field boardField = mainFrame.getClass().getDeclaredField("currentBoard");
                boardField.setAccessible(true);
                ua.oip.jiralite.domain.Board currentBoard = (ua.oip.jiralite.domain.Board) boardField.get(mainFrame);
                
                // Находим BoardService через рефлексию
                java.lang.reflect.Field serviceField = mainFrame.getClass().getDeclaredField("boardService");
                serviceField.setAccessible(true);
                ua.oip.jiralite.service.BoardService boardService = (ua.oip.jiralite.service.BoardService) serviceField.get(mainFrame);
                
                // Находим текущего пользователя через рефлексию
                java.lang.reflect.Field userField = mainFrame.getClass().getDeclaredField("currentUser");
                userField.setAccessible(true);
                ua.oip.jiralite.domain.User currentUser = (ua.oip.jiralite.domain.User) userField.get(mainFrame);
                
                // Сначала получаем полную доменную модель задачи
                ua.oip.jiralite.domain.Issue existingIssue = null;
                
                // Ищем задачу в списке globalIssues через рефлексию
                java.lang.reflect.Field globalIssuesField = boardService.getClass().getDeclaredField("globalIssues");
                globalIssuesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.List<ua.oip.jiralite.domain.Issue> globalIssues = 
                    (java.util.List<ua.oip.jiralite.domain.Issue>) globalIssuesField.get(boardService);
                
                // Ищем задачу с нужным ID в globalIssues
                for (ua.oip.jiralite.domain.Issue issue : globalIssues) {
                    if (issue.getId().equals(issueModel.getId())) {
                        existingIssue = issue;
                        break;
                    }
                }
                
                if (existingIssue == null) {
                    ua.oip.jiralite.ui.util.SwingHelper.showError(
                        mainFrame, 
                        messages.getString("issue.not_found"));
                    return;
                }
                
                // Создаем и показываем диалог редактирования с используя существующий метод
                ua.oip.jiralite.ui.frame.IssueDialog dialog = new ua.oip.jiralite.ui.frame.IssueDialog(
                    mainFrame, 
                    existingIssue,
                    currentBoard,
                    currentUser,
                    boardService,
                    messages
                );
                dialog.setVisible(true);
                
                // Обработка результатов диалога
                if (dialog.isIssueCreated()) {
                    // Вызываем метод loadIssues через рефлексию для обновления доски
                    java.lang.reflect.Method loadIssuesMethod = mainFrame.getClass().getDeclaredMethod("loadIssues", ua.oip.jiralite.domain.Board.class);
                    loadIssuesMethod.setAccessible(true);
                    loadIssuesMethod.invoke(mainFrame, currentBoard);
                }
            } catch (Exception ex) {
                System.err.println("Ошибка при редактировании задачи: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Удаление задачи с подтверждением
     */
    private void deleteIssue() {
        javax.swing.JFrame mainFrame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        if (mainFrame != null) {
            try {
                // Находим BoardService через рефлексию
                java.lang.reflect.Field serviceField = mainFrame.getClass().getDeclaredField("boardService");
                serviceField.setAccessible(true);
                ua.oip.jiralite.service.BoardService boardService = (ua.oip.jiralite.service.BoardService) serviceField.get(mainFrame);
                
                // Запрашиваем подтверждение удаления
                boolean confirmed = ua.oip.jiralite.ui.util.SwingHelper.showConfirmDialog(
                    mainFrame, 
                    messages.getString("app.confirm"), 
                    messages.getString("issue.delete_confirm"));
                
                if (confirmed) {
                    // Находим текущую доску через рефлексию
                    java.lang.reflect.Field boardField = mainFrame.getClass().getDeclaredField("currentBoard");
                    boardField.setAccessible(true);
                    ua.oip.jiralite.domain.Board currentBoard = (ua.oip.jiralite.domain.Board) boardField.get(mainFrame);
                    
                    // Удаляем задачу из globalIssues через рефлексию
                    java.lang.reflect.Field globalIssuesField = boardService.getClass().getDeclaredField("globalIssues");
                    globalIssuesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.List<ua.oip.jiralite.domain.Issue> globalIssues = 
                        (java.util.List<ua.oip.jiralite.domain.Issue>) globalIssuesField.get(boardService);
                    
                    // Ищем и удаляем задачу из глобального списка
                    boolean success = globalIssues.removeIf(issue -> issue.getId().equals(issueModel.getId()));
                    
                    if (success) {
                        // Обновляем доску после удаления
                        java.lang.reflect.Method loadIssuesMethod = mainFrame.getClass().getDeclaredMethod("loadIssues", ua.oip.jiralite.domain.Board.class);
                        loadIssuesMethod.setAccessible(true);
                        loadIssuesMethod.invoke(mainFrame, currentBoard);
                        
                        // Показываем сообщение об успешном удалении
                        ua.oip.jiralite.ui.util.SwingHelper.showInfoDialog(
                            mainFrame,
                            messages.getString("app.info"),
                            messages.getString("issue.delete_success")
                        );
                    } else {
                        // Сообщение об ошибке удаления
                        ua.oip.jiralite.ui.util.SwingHelper.showError(
                            mainFrame, 
                            messages.getString("issue.delete_error"));
                    }
                }
            } catch (Exception ex) {
                System.err.println("Ошибка при удалении задачи: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Создает выразительный значок приоритета для размещения в верхней части карточки
     */
    private JPanel createPriorityBadge(Priority priority) {
        System.out.println("IssueCardPanel: Створення значка пріоритету: " + priority);
        
        JPanel badgePanel = new JPanel();
        badgePanel.setLayout(new BoxLayout(badgePanel, BoxLayout.X_AXIS));
        badgePanel.setOpaque(false);
        badgePanel.setName("priorityBadge"); // Встановлюємо ім'я для подальшого пошуку
        
        float scale = themeManager.getCurrentScale().getFactor();
        
        // Создаем закрашенную метку с текстом приоритета
        // Для Medium делаем более короткое отображение
        String priorityText;
        if (priority == Priority.MEDIUM) {
            priorityText = "⚠ Med";
        } else if (priority == Priority.HIGHEST) {
            priorityText = "⚠ High+";
        } else {
            priorityText = "⚠ " + getPriorityText(priority);
        }
        
        JLabel priorityLabel = new JLabel(priorityText);
        priorityLabel.setFont(priorityLabel.getFont().deriveFont(Font.BOLD, 12f * scale));
        priorityLabel.setOpaque(true);
        
        // Встановлюємо розмір рамки залежно від масштабу
        int borderSize = Math.max(1, (int)(1 * scale));
        int paddingTop = Math.max(2, (int)(4 * scale));
        int paddingLeft = Math.max(4, (int)(8 * scale));
        
        priorityLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, borderSize),
                BorderFactory.createEmptyBorder(paddingTop, paddingLeft, paddingTop, paddingLeft)));
        
        // Встановлюємо колір фону та тексту залежно від пріоритету
        switch (priority) {
            case LOW:
                priorityLabel.setBackground(new Color(76, 175, 80)); // Более яркий зеленый
                priorityLabel.setForeground(Color.WHITE);
                break;
            case MEDIUM:
                priorityLabel.setBackground(new Color(33, 150, 243)); // Более яркий синий
                priorityLabel.setForeground(Color.WHITE);
                break;
            case HIGH:
                priorityLabel.setBackground(new Color(255, 152, 0)); // Более яркий оранжевый
                priorityLabel.setForeground(Color.WHITE);
                break;
            case HIGHEST:
                priorityLabel.setBackground(new Color(244, 67, 54)); // Яркий красный
                priorityLabel.setForeground(Color.WHITE);
                break;
            default:
                priorityLabel.setBackground(Color.GRAY);
                priorityLabel.setForeground(Color.WHITE);
        }
        
        // Добавляем прослушиватель мыши
        addMouseProxyListenerToComponent(priorityLabel);
        
        badgePanel.add(priorityLabel);
        
        return badgePanel;
    }
    
    /**
     * Додає слухач подій миші для картки
     */
    public void addCardMouseListener(MouseAdapter adapter) {
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        
        // Додаємо стилі курсору для перетягування
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }
    
    /**
     * Оновлює картку після зміни задачі
     */
    public void refresh(Issue issue) {
        // Оновлюємо модель
        issueModel.setTitle(issue.getTitle());
        issueModel.setDescription(issue.getDescription());
        issueModel.setStatus(issue.getStatus());
        issueModel.setPriority(issue.getPriority());
        
        // Оновлюємо присвоєного користувача, якщо він є
        if (issue.getAssignee() != null) {
            issueModel.setAssigneeName(issue.getAssignee().getFullName());
        } else {
            issueModel.setAssigneeName(null);
        }
        
        // Оновлюємо відображення
        titleTextArea.setText(issueModel.getTitle());
        statusLabel.setText("");
        
        // Убираем текстовый статус "In", оставляем только для завершенных задач
        switch (issueModel.getStatus()) {
            case TO_DO:
                statusLabel.setForeground(themeManager.getCurrentScheme().textSecondary);
                statusLabel.setText(""); // Убираем текст
                break;
            case IN_PROGRESS:
                statusLabel.setForeground(themeManager.getCurrentScheme().info);
                statusLabel.setText(""); // Убираем текст
                break;
            case DONE:
                statusLabel.setForeground(themeManager.getCurrentScheme().success);
                statusLabel.setText("✓"); // Значок галочки для завершенных задач
                break;
            default:
                statusLabel.setForeground(themeManager.getCurrentScheme().textPrimary);
                statusLabel.setText("");
        }
        
        // Оновлюємо текст призначеного користувача
        if (issueModel.getAssigneeName() != null && !issueModel.getAssigneeName().isEmpty() &&
            !issueModel.getAssigneeName().equals(messages.getString("issue.not_assigned"))) {
            assigneeLabel.setText(messages.getString("issue.assignee") + ": " + issueModel.getAssigneeName());
        } else {
            assigneeLabel.setText(messages.getString("issue.not_assigned"));
        }
        
        // Обновляем цвет карточки согласно текущей теме
        initialBackgroundColor = themeManager.getCurrentScheme().cardBackground;
        
        // Перемальовуємо картку
        repaint();
    }
    
    /**
     * Повертає модель задачі
     */
    public IssueCardModel getIssueModel() {
        return issueModel;
    }
    
    /**
     * Повертає текстове представлення пріоритету
     */
    private String getPriorityText(Priority priority) {
        switch (priority) {
            case LOW:
                return messages.getString("priority.low");
            case MEDIUM:
                return messages.getString("priority.medium");
            case HIGH:
                return messages.getString("priority.high");
            default:
                return "Unknown";
        }
    }
    
    /**
     * Добавляет прослушиватель мыши к компоненту для передачи событий родительской панели
     *
     * @param component компонент, к которому добавляется прослушиватель
     */
    private void addMouseProxyListenerToComponent(Component component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                dispatchEvent(SwingUtilities.convertMouseEvent(component, e, IssueCardPanel.this));
                e.consume();
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                dispatchEvent(SwingUtilities.convertMouseEvent(component, e, IssueCardPanel.this));
                e.consume();
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                dispatchEvent(SwingUtilities.convertMouseEvent(component, e, IssueCardPanel.this));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                dispatchEvent(SwingUtilities.convertMouseEvent(component, e, IssueCardPanel.this));
            }
        });
        
        if (component instanceof JComponent) {
            ((JComponent)component).addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(java.awt.event.MouseEvent e) {
                    dispatchEvent(SwingUtilities.convertMouseEvent(component, e, IssueCardPanel.this));
                    e.consume();
                }
            });
        }
    }
} 