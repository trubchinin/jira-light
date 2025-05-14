package ua.oip.jiralite.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.ui.model.IssueCardModel;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Панель для отображения карточки задачи на Kanban-доске
 */
public final class IssueCardPanel extends JPanel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final transient DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    private final IssueCardModel issueModel;
    private transient final ResourceBundle messages;
    
    private transient final JLabel titleLabel;
    private transient final JLabel statusLabel;
    private transient final JLabel assigneeLabel;
    
    /**
     * Конструктор карточки задачи
     * 
     * @param issue модель задачи
     * @param messages ресурсы локализации
     */
    public IssueCardPanel(IssueCardModel issue, ResourceBundle messages) {
        this.issueModel = issue;
        this.messages = messages;
        
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(200, 120));
        setMinimumSize(new Dimension(200, 120));
        setMaximumSize(new Dimension(200, 120));
        
        // Верхняя панель с ключом и статусом
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.setOpaque(false);
        
        JLabel keyLabel = new JLabel(issueModel.getKey());
        keyLabel.setForeground(Color.BLUE);
        keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD));
        
        // Ограничиваем ширину метки ключа, чтобы длинные ключи не перекрывали статус
        keyLabel.setPreferredSize(new Dimension(110, keyLabel.getPreferredSize().height));
        keyLabel.setMinimumSize(new Dimension(110, keyLabel.getPreferredSize().height));
        keyLabel.setMaximumSize(new Dimension(110, keyLabel.getPreferredSize().height));
        
        statusLabel = new JLabel(UiConstants.statusCaption(issueModel.getStatus()));
        // Добавляем отступ справа для статуса
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        
        // Встановлюємо колір статусу
        switch (issueModel.getStatus()) {
            case TO_DO:
                statusLabel.setForeground(Color.GRAY);
                break;
            case IN_PROGRESS:
                statusLabel.setForeground(Color.BLUE);
                break;
            case DONE:
                statusLabel.setForeground(Color.GREEN.darker());
                break;
            default:
                statusLabel.setForeground(Color.BLACK);
        }
        
        topPanel.add(keyLabel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.EAST);
        
        // Контейнер для основного содержимого
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Название задачи
        titleLabel = new JLabel(issueModel.getTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Метка для назначенного пользователя
        assigneeLabel = new JLabel();
        assigneeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        assigneeLabel.setForeground(Color.DARK_GRAY);
        assigneeLabel.setFont(assigneeLabel.getFont().deriveFont(Font.ITALIC));
        
        // Устанавливаем текст назначенного пользователя
        if (issueModel.getAssigneeName() != null && !issueModel.getAssigneeName().isEmpty()) {
            assigneeLabel.setText(messages.getString("issue.assignee") + ": " + issueModel.getAssigneeName());
        } else {
            assigneeLabel.setText(messages.getString("issue.not_assigned"));
        }
        
        // Добавляем компоненты в контентную панель
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Отступ
        contentPanel.add(assigneeLabel);
        
        // Добавление компонентов на панель
        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        // Индикатор приоритета в нижней части карточки
        if (issueModel.getPriority() != null) {
            add(createPriorityIndicator(issueModel.getPriority()), BorderLayout.SOUTH);
        }
        
        // Добавляем рамку
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2), 
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        
        // Добавляем контекстное меню
        addContextMenu();
                
        setVisible(true);
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
     * Обработчик редактирования задачи
     */
    private void editIssue() {
        // Проверка прав пользователя
        ua.oip.jiralite.service.AuthService authService = ua.oip.jiralite.service.AuthService.getInstance();
        if (!authService.canEditIssue()) {
            javax.swing.JFrame mainFrame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
            ua.oip.jiralite.ui.util.SwingHelper.showErrorDialog(
                mainFrame,
                messages.getString("app.error"),
                messages.getString("permission.not_allowed")
            );
            return;
        }
        
        // Находим главное окно для открытия диалога
        javax.swing.JFrame mainFrame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        if (mainFrame != null) {
            try {
                // Находим текущую доску через рефлексию
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
                
                // Для пользователей с ролью USER проверяем, могут ли они редактировать эту задачу
                if (currentUser != null && 
                    currentUser.getRole() == ua.oip.jiralite.domain.enums.Role.USER) {
                    // Получаем информацию о задаче, чтобы определить исполнителя
                    boolean canEdit = false;
                    
                    // Ищем задачу в списке globalIssues через рефлексию
                    java.lang.reflect.Field globalIssuesField = boardService.getClass().getDeclaredField("globalIssues");
                    globalIssuesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.List<ua.oip.jiralite.domain.Issue> globalIssues = 
                        (java.util.List<ua.oip.jiralite.domain.Issue>) globalIssuesField.get(null);
                    
                    for (ua.oip.jiralite.domain.Issue issue : globalIssues) {
                        if (issue.getId().equals(issueModel.getId())) {
                            System.out.println("IssueCardPanel.editIssue: Проверка прав на редактирование задачи " + issue.getId());
                            
                            // Если задача без исполнителя, разрешаем редактирование
                            if (issue.getAssignee() == null) {
                                System.out.println("IssueCardPanel.editIssue: У задачи нет исполнителя, разрешаем редактирование");
                                canEdit = true;
                                break;
                            }
                            
                            // Если пользователь является исполнителем, разрешаем редактирование
                            if (issue.getAssignee().getId() != null && 
                                currentUser.getId() != null &&
                                issue.getAssignee().getId().equals(currentUser.getId())) {
                                System.out.println("IssueCardPanel.editIssue: Пользователь является исполнителем задачи");
                                canEdit = true;
                            } else {
                                System.out.println("IssueCardPanel.editIssue: Пользователь НЕ является исполнителем задачи");
                            }
                            break;
                        }
                    }
                    
                    if (!canEdit) {
                        System.err.println("IssueCardPanel.editIssue: Пользователь с ролью USER не может редактировать задачу другого исполнителя");
                        ua.oip.jiralite.ui.util.SwingHelper.showErrorDialog(
                            mainFrame,
                            messages.getString("app.error"),
                            messages.getString("permission.cannot_edit_others_issues")
                        );
                        return;
                    }
                }
                
                // Сначала получаем полную доменную модель задачи из BoardService,
                // чтобы не потерять данные, которые могут отсутствовать в UI модели
                ua.oip.jiralite.domain.Issue existingIssue = null;
                
                // Ищем задачу в списке globalIssues через рефлексию
                java.lang.reflect.Field globalIssuesField = boardService.getClass().getDeclaredField("globalIssues");
                globalIssuesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.List<ua.oip.jiralite.domain.Issue> globalIssues = 
                    (java.util.List<ua.oip.jiralite.domain.Issue>) globalIssuesField.get(null); // поле статическое
                
                // Ищем задачу с нужным ID в globalIssues
                for (ua.oip.jiralite.domain.Issue issue : globalIssues) {
                    if (issue.getId().equals(issueModel.getId())) {
                        existingIssue = issue;
                        break;
                    }
                }
                
                // Если задача не найдена, создаем новую
                if (existingIssue == null) {
                    existingIssue = new ua.oip.jiralite.domain.Issue();
                    existingIssue.setId(issueModel.getId());
                    existingIssue.setTitle(issueModel.getTitle());
                    existingIssue.setDescription(issueModel.getDescription());
                    existingIssue.setStatus(issueModel.getStatus());
                    existingIssue.setPriority(issueModel.getPriority());
                    existingIssue.setKey(issueModel.getKey());
                }
                
                if (currentBoard != null && currentBoard.getProject() != null) {
                    existingIssue.setProject(currentBoard.getProject());
                }
                
                // Открываем диалог редактирования
                ua.oip.jiralite.ui.frame.IssueDialog dialog = new ua.oip.jiralite.ui.frame.IssueDialog(
                    mainFrame, 
                    existingIssue,  // Используем полную модель задачи
                    currentBoard, 
                    currentUser,
                    boardService,
                    messages
                );
                dialog.setVisible(true);
                
                // Обновляем доску, если задача изменена
                if (dialog.isIssueCreated()) {
                    // Вызываем метод loadIssues через рефлексию
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
     * Обработчик удаления задачи
     */
    private void deleteIssue() {
        // Проверка прав пользователя
        ua.oip.jiralite.service.AuthService authService = ua.oip.jiralite.service.AuthService.getInstance();
        if (!authService.canDeleteIssue()) {
            javax.swing.JFrame mainFrame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
            ua.oip.jiralite.ui.util.SwingHelper.showErrorDialog(
                mainFrame,
                messages.getString("app.error"),
                messages.getString("permission.not_allowed")
            );
            return;
        }
        
        // Находим главное окно для показа диалога подтверждения
        javax.swing.JFrame mainFrame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
        if (mainFrame != null) {
            try {
                // Находим BoardService через рефлексию
                java.lang.reflect.Field serviceField = mainFrame.getClass().getDeclaredField("boardService");
                serviceField.setAccessible(true);
                ua.oip.jiralite.service.BoardService boardService = (ua.oip.jiralite.service.BoardService) serviceField.get(mainFrame);
                
                // Находим текущего пользователя через рефлексию
                java.lang.reflect.Field userField = mainFrame.getClass().getDeclaredField("currentUser");
                userField.setAccessible(true);
                ua.oip.jiralite.domain.User currentUser = (ua.oip.jiralite.domain.User) userField.get(mainFrame);
                
                // Для пользователей с ролью USER проверяем, могут ли они удалять эту задачу
                if (currentUser != null && 
                    currentUser.getRole() == ua.oip.jiralite.domain.enums.Role.USER) {
                    // Получаем информацию о задаче, чтобы определить исполнителя
                    boolean canDelete = false;
                    
                    // Ищем задачу в списке globalIssues через рефлексию
                    java.lang.reflect.Field globalIssuesField = boardService.getClass().getDeclaredField("globalIssues");
                    globalIssuesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.List<ua.oip.jiralite.domain.Issue> globalIssues = 
                        (java.util.List<ua.oip.jiralite.domain.Issue>) globalIssuesField.get(null);
                    
                    for (ua.oip.jiralite.domain.Issue issue : globalIssues) {
                        if (issue.getId().equals(issueModel.getId())) {
                            System.out.println("IssueCardPanel.deleteIssue: Проверка прав на удаление задачи " + issue.getId());
                            
                            // Если задача без исполнителя, разрешаем удаление
                            if (issue.getAssignee() == null) {
                                System.out.println("IssueCardPanel.deleteIssue: У задачи нет исполнителя, разрешаем удаление");
                                canDelete = true;
                                break;
                            }
                            
                            // Если пользователь является исполнителем, разрешаем удаление
                            if (issue.getAssignee().getId() != null && 
                                currentUser.getId() != null &&
                                issue.getAssignee().getId().equals(currentUser.getId())) {
                                System.out.println("IssueCardPanel.deleteIssue: Пользователь является исполнителем задачи");
                                canDelete = true;
                            } else {
                                System.out.println("IssueCardPanel.deleteIssue: Пользователь НЕ является исполнителем задачи");
                            }
                            break;
                        }
                    }
                    
                    if (!canDelete) {
                        System.err.println("IssueCardPanel.deleteIssue: Пользователь с ролью USER не может удалять задачу другого исполнителя");
                        ua.oip.jiralite.ui.util.SwingHelper.showErrorDialog(
                            mainFrame,
                            messages.getString("app.error"),
                            messages.getString("permission.cannot_edit_others_issues")
                        );
                        return;
                    }
                }
                
                // Показываем диалог подтверждения
                boolean confirmed = ua.oip.jiralite.ui.util.SwingHelper.showConfirmDialog(
                    mainFrame,
                    messages.getString("app.confirm"),
                    messages.getString("issue.delete_confirm")
                );
                
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
                        (java.util.List<ua.oip.jiralite.domain.Issue>) globalIssuesField.get(null);
                    
                    // Ищем и удаляем задачу из глобального списка
                    boolean removed = globalIssues.removeIf(issue -> issue.getId().equals(issueModel.getId()));
                    
                    System.out.println("Задача с ID " + issueModel.getId() + " удалена: " + removed);
                    
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
                }
            } catch (Exception ex) {
                System.err.println("Ошибка при удалении задачи: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Создает индикатор приоритета
     */
    private JPanel createPriorityIndicator(Priority priority) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel label = new JLabel("  ");
        label.setOpaque(true);
        
        switch (priority) {
            case HIGHEST:
                label.setBackground(UiConstants.DANGER_COLOR);
                label.setToolTipText(messages.getString("priority.highest"));
                break;
            case HIGH:
                label.setBackground(UiConstants.WARNING_COLOR);
                label.setToolTipText(messages.getString("priority.high"));
                break;
            case MEDIUM:
                label.setBackground(UiConstants.INFO_COLOR);
                label.setToolTipText(messages.getString("priority.medium"));
                break;
            case LOW:
                label.setBackground(UiConstants.SUCCESS_COLOR);
                label.setToolTipText(messages.getString("priority.low"));
                break;
            case LOWEST:
                label.setBackground(UiConstants.TEXT_SECONDARY);
                label.setToolTipText(messages.getString("priority.lowest"));
                break;
        }
        
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Добавляет обработчик перетаскивания карточки
     */
    public void addCardMouseListener(MouseAdapter adapter) {
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        
        // Встановлюємо Cursor, щоб показати, що картку можна перетягувати
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }
    
    /**
     * Обновляет карточку на основе данных задачи
     */
    public void refresh(Issue issue) {
        try {
            // Обновляем заголовок
            titleLabel.setText(issue.getTitle());
            
            // Обновляем статус
            statusLabel.setText(UiConstants.statusCaption(issue.getStatus()));
            
            // Обновляем назначенного пользователя
            if (issue.getAssignee() != null) {
                assigneeLabel.setText(messages.getString("issue.assignee") + ": " + issue.getAssignee().getFullName());
            } else {
                assigneeLabel.setText(messages.getString("issue.not_assigned"));
            }
            
            // Обновляем статус задачи со цветовым обозначением
            switch (issue.getStatus()) {
                case TO_DO:
                    statusLabel.setForeground(Color.GRAY);
                    break;
                case IN_PROGRESS:
                    statusLabel.setForeground(Color.BLUE);
                    break;
                case DONE:
                    statusLabel.setForeground(Color.GREEN.darker());
                    break;
                default:
                    statusLabel.setForeground(Color.BLACK);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении карточки: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Обновляем отображение
        revalidate();
        repaint();
    }
    
    /**
     * Возвращает модель задачи
     */
    public IssueCardModel getIssueModel() {
        return issueModel;
    }
} 