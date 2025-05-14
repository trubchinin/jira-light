package ua.oip.jiralite.ui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Comment;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.service.IssueService;
import ua.oip.jiralite.ui.model.IssueCardModel;
import ua.oip.jiralite.ui.util.SwingHelper;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Діалог редагування задачі.
 * Дозволяє переглядати та змінювати дані задачі.
 */
public class IssueDialog extends JDialog {
    
    private static final Logger log = LoggerFactory.getLogger(IssueDialog.class);
    
    private final IssueCardModel issueModel;
    private final Issue issue;
    private final Board board;
    private final User currentUser;
    private final BoardService boardService;
    private final ResourceBundle messages;
    private final boolean isNewIssue;
    
    private boolean issueCreated = false;
    
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Priority> priorityComboBox;
    private JComboBox<Status> statusComboBox;
    private JComboBox<User> assigneeComboBox;
    private JTextArea commentArea;
    private JList<Comment> commentsList;
    
    private final JTextField summaryFld   = new JTextField(30);
    private final JTextArea  description  = new JTextArea(5, 30);
    private final JComboBox<User> assignee = new JComboBox<>();
    private final JComboBox<Status> status =
            new JComboBox<>(Status.values());

    private final IssueService issueSvc = IssueService.getInstance();
    
    /**
     * Конструктор діалогу для редагування задачі
     * 
     * @param parent батьківське вікно
     * @param issue задача для редагування, або null для нової задачі
     * @param board дошка, до якої відноситься задача
     * @param currentUser поточний користувач
     * @param boardService сервіс дошок
     * @param messages ресурси локалізації
     */
    public IssueDialog(JFrame parent, Issue issue, Board board, 
            User currentUser, BoardService boardService, ResourceBundle messages) {
        super(parent, true);  // Модальний діалог
        
        this.issue = issue;
        this.board = board;
        this.currentUser = currentUser;
        this.boardService = boardService;
        this.messages = messages;
        this.isNewIssue = (issue == null);
        
        // Створюємо модель для нової задачі або на основі існуючої
        if (isNewIssue) {
            this.issueModel = new IssueCardModel();
            
            // Якщо об'єкт Issue був переданий, використовуємо його статус
            if (issue != null && issue.getStatus() != null) {
                this.issueModel.setStatus(issue.getStatus());
                System.out.println("IssueDialog: встановлено статус з переданої задачі: " + issue.getStatus());
            } else {
                this.issueModel.setStatus(Status.TO_DO);  // За замовчуванням нова задача має статус "To Do"
                System.out.println("IssueDialog: встановлено статус за замовчуванням: TO_DO");
            }
            
            this.issueModel.setPriority(Priority.MEDIUM);  // За замовчуванням середній пріоритет
            setTitle(messages.getString("issue.create"));
        } else {
            this.issueModel = IssueCardModel.fromIssue(issue);
            setTitle(issue.getKey() + " - " + issue.getTitle());
        }
        
        initializeUI();
    }
    
    /**
     * Ініціалізація компонентів інтерфейсу
     */
    private void initializeUI() {
        setSize(UiConstants.ISSUE_DIALOG_WIDTH, UiConstants.ISSUE_DIALOG_HEIGHT);
        setLocationRelativeTo(getOwner());  // Центрування відносно батьківського вікна
        
        // Головна панель з прокруткою
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(UiConstants.DEFAULT_BORDER);
        
        // Створення панелі форми для полів редагування
        JPanel formPanel = createFormPanel();
        
        // Створення панелі з коментарями
        JPanel commentsPanel = createCommentsPanel();
        
        // Розділяємо форму та коментарі через табовий компонент
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(messages.getString("issue.details"), formPanel);
        tabbedPane.addTab(messages.getString("issue.comments"), commentsPanel);
        
        // Додавання компонентів до головної панелі
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        // Встановлення панелі як вміст діалогу
        setContentPane(mainPanel);
    }
    
    /**
     * Створення панелі форми з полями для редагування
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(
                UiConstants.PANEL_PADDING, UiConstants.PANEL_PADDING, 
                UiConstants.PANEL_PADDING, UiConstants.PANEL_PADDING));
        
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(5, 5, 5, 10);
        
        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.insets = new Insets(5, 0, 5, 5);
        
        // Ідентифікатор задачі (тільки для існуючих задач)
        if (!isNewIssue) {
            labelConstraints.gridy = 0;
            fieldConstraints.gridy = 0;
            
            JLabel keyLabel = new JLabel(messages.getString("issue.key") + ":");
            JLabel keyValueLabel = new JLabel(issueModel.getKey());
            keyValueLabel.setFont(keyValueLabel.getFont().deriveFont(Font.BOLD));
            
            panel.add(keyLabel, labelConstraints);
            panel.add(keyValueLabel, fieldConstraints);
        }
        
        // Заголовок задачі
        labelConstraints.gridy = 1;
        fieldConstraints.gridy = 1;
        
        JLabel titleLabel = new JLabel(messages.getString("issue.summary") + ":");
        titleField = new JTextField(issueModel.getTitle());
        
        panel.add(titleLabel, labelConstraints);
        panel.add(titleField, fieldConstraints);
        
        // Опис задачі
        labelConstraints.gridy = 2;
        fieldConstraints.gridy = 2;
        fieldConstraints.weighty = 1.0;
        fieldConstraints.fill = GridBagConstraints.BOTH;
        
        JLabel descriptionLabel = new JLabel(messages.getString("issue.description") + ":");
        descriptionArea = new JTextArea(issueModel.getDescription());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(300, 150));
        
        panel.add(descriptionLabel, labelConstraints);
        panel.add(descriptionScroll, fieldConstraints);
        
        // Пріоритет
        labelConstraints.gridy = 3;
        fieldConstraints.gridy = 3;
        fieldConstraints.weighty = 0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel priorityLabel = new JLabel(messages.getString("issue.priority") + ":");
        priorityComboBox = new JComboBox<>(Priority.values());
        priorityComboBox.setSelectedItem(issueModel.getPriority());
        
        panel.add(priorityLabel, labelConstraints);
        panel.add(priorityComboBox, fieldConstraints);
        
        // Статус
        labelConstraints.gridy = 4;
        fieldConstraints.gridy = 4;
        
        JLabel statusLabel = new JLabel(messages.getString("issue.status") + ":");
        statusComboBox = new JComboBox<>(Status.values());
        statusComboBox.setSelectedItem(issueModel.getStatus());
        
        panel.add(statusLabel, labelConstraints);
        panel.add(statusComboBox, fieldConstraints);
        
        // Виконавець
        labelConstraints.gridy = 5;
        fieldConstraints.gridy = 5;
        
        JLabel assigneeLabel = new JLabel(messages.getString("issue.assignee") + ":");
        assigneeComboBox = new JComboBox<>();
        loadAssignees();
        
        panel.add(assigneeLabel, labelConstraints);
        panel.add(assigneeComboBox, fieldConstraints);
        
        return panel;
    }
    
    /**
     * Створення панелі для коментарів
     */
    private JPanel createCommentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(
                UiConstants.PANEL_PADDING, UiConstants.PANEL_PADDING, 
                UiConstants.PANEL_PADDING, UiConstants.PANEL_PADDING));
        
        // Список існуючих коментарів
        DefaultListModel<Comment> commentsModel = new DefaultListModel<>();
        
        // Додаємо існуючі коментарі, якщо задача не нова
        if (!isNewIssue && issue.getComments() != null) {
            for (Comment comment : issue.getComments()) {
                commentsModel.addElement(comment);
            }
        }
        
        commentsList = new JList<>(commentsModel);
        commentsList.setCellRenderer(new CommentCellRenderer());
        
        JScrollPane commentsScroll = new JScrollPane(commentsList);
        commentsScroll.setBorder(BorderFactory.createTitledBorder(
                messages.getString("issue.comments")));
        
        // Поле для додавання нового коментаря
        JPanel addCommentPanel = new JPanel(new BorderLayout());
        addCommentPanel.setBorder(BorderFactory.createTitledBorder(
                messages.getString("issue.add_comment")));
        
        commentArea = new JTextArea(3, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        
        JScrollPane commentScroll = new JScrollPane(commentArea);
        
        JButton addCommentButton = SwingHelper.createButton(
                messages.getString("issue.add_comment"), null);
        addCommentButton.addActionListener(e -> addComment());
        
        addCommentPanel.add(commentScroll, BorderLayout.CENTER);
        addCommentPanel.add(addCommentButton, BorderLayout.SOUTH);
        
        // Додаємо все на панель
        panel.add(commentsScroll, BorderLayout.CENTER);
        panel.add(addCommentPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Створення панелі з кнопками
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = SwingHelper.createButton(
                messages.getString("app.save"), null);
        saveButton.addActionListener(e -> saveIssue());
        
        JButton cancelButton = SwingHelper.createButton(
                messages.getString("app.cancel"), null);
        cancelButton.addActionListener(e -> dispose());
        
        panel.add(saveButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    /**
     * Завантаження списку можливих виконавців
     */
    private void loadAssignees() {
        // Очищаємо комбобокс
        assigneeComboBox.removeAllItems();
        
        // Додаємо порожній елемент (без виконавця)
        assigneeComboBox.addItem(null);
        
        // Отримуємо список учасників проєкту та додаємо їх у комбобокс
        if (board != null && board.getProject() != null) {
            List<User> projectMembers = boardService.getProjectMembers(board.getProject());
            for (User user : projectMembers) {
                assigneeComboBox.addItem(user);
            }
        }
        
        // Встановлюємо поточного виконавця, якщо є
        if (!isNewIssue && issue.getAssignee() != null) {
            assigneeComboBox.setSelectedItem(issue.getAssignee());
        }
    }
    
    /**
     * Додавання нового коментаря
     */
    private void addComment() {
        String commentText = commentArea.getText().trim();
        
        if (commentText.isEmpty()) {
            return;
        }
        
        // Создаем новый комментарий
        Comment comment = new Comment(commentText, isNewIssue ? null : issue, currentUser);
        comment.setCreatedAt(LocalDateTime.now());
        
        if (isNewIssue) {
            // Для новых задач просто показываем комментарий в списке, он будет сохранен при сохранении задачи
            DefaultListModel<Comment> model = (DefaultListModel<Comment>) commentsList.getModel();
            model.addElement(comment);
        } else {
            // Для существующей задачи добавляем комментарий в модель задачи
            if (issue.getComments() == null) {
                issue.setComments(new java.util.ArrayList<>());
            }
            issue.getComments().add(comment);
            
            // Добавляем комментарий в список UI
            DefaultListModel<Comment> model = (DefaultListModel<Comment>) commentsList.getModel();
            model.addElement(comment);
            
            // Сохраняем комментарий через сервис
            if (boardService != null) {
                boardService.addComment(issue, currentUser, commentText);
                System.out.println("IssueDialog.addComment: добавлен комментарий: " + 
                    commentText.substring(0, Math.min(30, commentText.length())) + "...");
            }
        }
        
        // Очищаем поле ввода
        commentArea.setText("");
    }
    
    /**
     * Збереження задачі
     */
    private void saveIssue() {
        // Перевіряємо, що заголовок не порожній
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            SwingHelper.showErrorDialog(this, 
                    messages.getString("app.error"), 
                    messages.getString("issue.title_required"));
            titleField.requestFocus();
            return;
        }
        
        // Получаем текст описания из поля ввода
        String description = descriptionArea.getText();
        
        // Получаем выбранного пользователя
        User assignee = (User) assigneeComboBox.getSelectedItem();
        
        // Оновлюємо модель даними з форми
        issueModel.setTitle(title);
        issueModel.setDescription(description);
        issueModel.setPriority((Priority) priorityComboBox.getSelectedItem());
        
        // Обновляем имя пользователя в UI модели
        if (assignee != null) {
            String fullName = assignee.getFullName();
            issueModel.setAssigneeName(fullName);
            System.out.println("IssueDialog.saveIssue: Установлен assigneeName = " + fullName);
        } else {
            issueModel.setAssigneeName(null);
            System.out.println("IssueDialog.saveIssue: Исполнитель не выбран (null)");
        }
        
        // Проверяем, что описание было корректно установлено в модель
        System.out.println("IssueDialog.saveIssue: Описание из поля ввода: " + 
            (description != null ? description.substring(0, Math.min(30, description.length())) + "..." : "null"));
        System.out.println("IssueDialog.saveIssue: Описание в модели после установки: " + 
            (issueModel.getDescription() != null ? 
                issueModel.getDescription().substring(0, Math.min(30, issueModel.getDescription().length())) + "..." : 
                "null"));
        
        // Важливо: Встановлюємо статус вибраний користувачем
        Status selectedStatus = (Status) statusComboBox.getSelectedItem();
        issueModel.setStatus(selectedStatus);
        
        System.out.println("IssueDialog.saveIssue: issueModel.status = " + issueModel.getStatus());
        
        try {
            Issue createdIssue = null;
            
            if (isNewIssue) {
                // Получаем данные из формы
                
                // Выводим отладковую информацию перед созданием
                System.out.println("IssueDialog.saveIssue: перед созданием issueModel.status = " + issueModel.getStatus());
                
                // Проверяем, нужно ли установить ID для новой задачи
                if (issueModel.getId() == null) {
                    // Генерируем новый ID для задачи (в реальном приложении это будет делать сервер)
                    long newId = System.currentTimeMillis() % 10000; // Для избежания больших чисел
                    issueModel.setId(newId);
                    System.out.println("IssueDialog.saveIssue: создаем новую задачу, установлен ID = " + newId);
                }
                
                // Переконуємося, що статус в моделі відповідає вибраному в комбобоксі
                if (selectedStatus != null && !selectedStatus.equals(issueModel.getStatus())) {
                    System.out.println("IssueDialog.saveIssue: корегуємо статус в моделі з " + issueModel.getStatus() + " на " + selectedStatus);
                    issueModel.setStatus(selectedStatus);
                }
                
                // Перевіряємо, чи модель має всі необхідні дані
                if (issueModel.getTitle() == null || issueModel.getTitle().isEmpty()) {
                    issueModel.setTitle(title);
                    System.out.println("IssueDialog.saveIssue: встановлено назву з поля: " + title);
                }
                
                // Створюємо нову задачу через сервіс
                createdIssue = boardService.createIssue(board, currentUser, assignee, issueModel);
                
                // Виводимо відладкову інформацію після створення
                System.out.println("IssueDialog.saveIssue: створено задачу з ID " + 
                    (createdIssue != null ? createdIssue.getId() : "null") + 
                    " та статусом " + (createdIssue != null ? createdIssue.getStatus() : "null") +
                    " и исполнителем " + (createdIssue != null && createdIssue.getAssignee() != null ? 
                                          createdIssue.getAssignee().getFullName() : "null"));
                
                // Вручну оновлюємо властивості задачі вже після створення
                if (createdIssue != null) {
                    // Встановлюємо властивості задачі з форми
                    createdIssue.setTitle(title);
                    createdIssue.setDescription(descriptionArea.getText());
                    createdIssue.setPriority((Priority) priorityComboBox.getSelectedItem());
                    createdIssue.setStatus(selectedStatus); // Явно встановлюємо статус
                    
                    // Явно устанавливаем назначенного пользователя
                    if (assignee != null) {
                        createdIssue.setAssignee(assignee);
                        System.out.println("IssueDialog.saveIssue: установлен исполнитель " + assignee.getFullName());
                    }
                    
                    // Зберігаємо задачу з усіма властивостями в глобальному списку
                    boardService.updateIssue(createdIssue);
                    
                    // Додатково оновлюємо статус окремим методом для певності
                    boardService.updateIssueStatus(createdIssue, selectedStatus);
                    
                    System.out.println("IssueDialog.saveIssue: повторно оновлено статус задачі " + 
                        createdIssue.getTitle() + " на " + selectedStatus);
                    
                    // Додаємо початковий коментар, якщо є
                    String commentText = commentArea.getText().trim();
                    if (!commentText.isEmpty()) {
                        Comment comment = new Comment(commentText, createdIssue, currentUser);
                        comment.setCreatedAt(LocalDateTime.now());
                        
                        // Инициализируем список комментариев, если он пустой
                        if (createdIssue.getComments() == null) {
                            createdIssue.setComments(new java.util.ArrayList<>());
                        }
                        
                        // Добавляем комментарий в список
                        createdIssue.getComments().add(comment);
                        
                        System.out.println("IssueDialog.saveIssue: добавлен комментарий к задаче: " + 
                            commentText.substring(0, Math.min(30, commentText.length())) + "...");
                    }
                }
                
                SwingHelper.showInfoDialog(this, 
                        messages.getString("app.info"), 
                        messages.getString("issue.created_success"));
                        
                issueCreated = true;
            } else {
                // Оновлюємо існуючу задачу
                issue.setTitle(title);
                issue.setDescription(descriptionArea.getText());
                issue.setPriority((Priority) priorityComboBox.getSelectedItem());
                issue.setStatus(selectedStatus); // Явно встановлюємо статус
                issue.setAssignee(assignee); // Явно встановлюємо виконавця
                
                // Додаємо новий коментар, якщо потрібно
                String commentText = commentArea.getText().trim();
                if (!commentText.isEmpty()) {
                    Comment comment = new Comment(commentText, issue, currentUser);
                    comment.setCreatedAt(LocalDateTime.now());
                    
                    // Инициализируем список комментариев, если он пустой
                    if (issue.getComments() == null) {
                        issue.setComments(new java.util.ArrayList<>());
                    }
                    
                    // Добавляем комментарий в список и обновляем модель
                    issue.getComments().add(comment);
                    
                    // Добавляем комментарий в UI модель
                    DefaultListModel<Comment> commentsModel = (DefaultListModel<Comment>) commentsList.getModel();
                    commentsModel.addElement(comment);
                    
                    // Очищаем поле ввода
                    commentArea.setText("");
                    
                    System.out.println("IssueDialog.saveIssue: добавлен комментарий к существующей задаче: " + 
                        commentText.substring(0, Math.min(30, commentText.length())) + "...");
                }
                
                // Логирование обновления
                System.out.println("IssueDialog.saveIssue: обновляем существующую задачу ID=" + issue.getId() + 
                    ", title='" + issue.getTitle() + "'" +
                    ", статус=" + issue.getStatus() + 
                    ", исполнитель=" + (issue.getAssignee() != null ? issue.getAssignee().getFullName() : "не назначен") +
                    ", описание=" + (issue.getDescription() != null ? 
                        issue.getDescription().substring(0, Math.min(30, issue.getDescription().length())) + "..." : "null"));
                
                boardService.updateIssue(issue);
                
                // Додатково оновлюємо статус окремим методом для певності
                boardService.updateIssueStatus(issue, selectedStatus);
                
                SwingHelper.showInfoDialog(this, 
                        messages.getString("app.info"), 
                        messages.getString("issue.updated_success"));
                        
                issueCreated = true;
            }
            
            // Закриваємо діалог
            dispose();
            
        } catch (Exception e) {
            log.error("Error saving issue", e);
            System.err.println("IssueDialog.saveIssue: помилка при збереженні задачі: " + e.getMessage());
            e.printStackTrace(); // Для відладки
            SwingHelper.showErrorDialog(this, 
                    messages.getString("app.error"), 
                    e.getMessage());
        }
    }
    
    /**
     * Рендерер для коментарів у списку
     */
    private class CommentCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            if (value instanceof Comment) {
                Comment comment = (Comment) value;
                
                // Заголовок комментария (автор и дата)
                String authorName = comment.getAuthor() != null ? comment.getAuthor().getFullName() : "Unknown";
                String dateStr = comment.getCreatedAt() != null ? 
                        comment.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : 
                        "";
                
                JLabel headerLabel = new JLabel(authorName + " - " + dateStr);
                headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));
                headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
                
                // Текст комментария
                JTextArea textArea = new JTextArea();
                if (comment.getText() != null) {
                    textArea.setText(comment.getText());
                }
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setEditable(false);
                textArea.setOpaque(false); // Делаем прозрачным для лучшего вида
                textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                
                panel.add(headerLabel, BorderLayout.NORTH);
                panel.add(textArea, BorderLayout.CENTER);
                
                // Стилизация для выделения
                if (isSelected) {
                    panel.setBackground(list.getSelectionBackground());
                    headerLabel.setForeground(list.getSelectionForeground());
                    textArea.setForeground(list.getSelectionForeground());
                } else {
                    panel.setBackground(new Color(250, 250, 250)); // Светло-серый фон для комментариев
                    headerLabel.setForeground(Color.DARK_GRAY);
                    textArea.setForeground(Color.BLACK);
                }
            }
            
            // Устанавливаем минимальную высоту для компонента
            panel.setPreferredSize(new Dimension(list.getWidth(), 80));
            return panel;
        }
    }

    /**
     * Возвращает признак создания новой задачи
     * 
     * @return true, если задача была создана или отредактирована; false - если операция отменена
     */
    public boolean isIssueCreated() {
        return issueCreated;
    }
    
    /**
     * Возвращает модель задачи с пользовательскими данными
     * 
     * @return модель задачи
     */
    public IssueCardModel getIssueModel() {
        return issueModel;
    }
} 