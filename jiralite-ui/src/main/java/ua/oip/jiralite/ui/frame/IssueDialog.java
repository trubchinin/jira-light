package ua.oip.jiralite.ui.frame;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Comment;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.BoardService;
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
    
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Priority> priorityComboBox;
    private JComboBox<Status> statusComboBox;
    private JComboBox<User> assigneeComboBox;
    private JTextArea commentArea;
    private JList<Comment> commentsList;
    
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
            this.issueModel.setStatus(Status.TO_DO);  // За замовчуванням нова задача має статус "To Do"
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
        
        if (isNewIssue) {
            // Для нової задачі просто очищаємо поле - коментар буде додано при збереженні
            commentArea.setText("");
        } else {
            // Для існуючої задачі додаємо коментар через сервіс
            Comment comment = boardService.addComment(issue, currentUser, commentText);
            
            // Додаємо коментар у список
            DefaultListModel<Comment> model = (DefaultListModel<Comment>) commentsList.getModel();
            model.addElement(comment);
            
            // Очищаємо поле введення
            commentArea.setText("");
        }
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
        
        // Оновлюємо модель даними з форми
        issueModel.setTitle(title);
        issueModel.setDescription(descriptionArea.getText());
        issueModel.setPriority((Priority) priorityComboBox.getSelectedItem());
        issueModel.setStatus((Status) statusComboBox.getSelectedItem());
        
        try {
            if (isNewIssue) {
                // Створюємо нову задачу
                boardService.createIssue(board, currentUser, 
                        (User) assigneeComboBox.getSelectedItem(), issueModel);
                
                // Додаємо початковий коментар, якщо є
                String commentText = commentArea.getText().trim();
                if (!commentText.isEmpty()) {
                    boardService.addComment(issue, currentUser, commentText);
                }
                
                SwingHelper.showInfoDialog(this, 
                        messages.getString("app.info"), 
                        messages.getString("issue.created_success"));
            } else {
                // Оновлюємо існуючу задачу
                boardService.updateIssue(issue, issueModel, 
                        (User) assigneeComboBox.getSelectedItem());
                
                SwingHelper.showInfoDialog(this, 
                        messages.getString("app.info"), 
                        messages.getString("issue.updated_success"));
            }
            
            // Закриваємо діалог
            dispose();
            
        } catch (Exception e) {
            log.error("Error saving issue", e);
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
            
            if (value instanceof Comment) {
                Comment comment = (Comment) value;
                
                // Заголовок коментаря (автор та дата)
                JLabel headerLabel = new JLabel(comment.getAuthor().getFullName() + 
                        " - " + comment.getCreatedAt().toString());
                headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));
                
                // Текст коментаря
                JTextArea textArea = new JTextArea(comment.getText());
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setEditable(false);
                textArea.setBackground(panel.getBackground());
                textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                
                panel.add(headerLabel, BorderLayout.NORTH);
                panel.add(textArea, BorderLayout.CENTER);
                
                // Стилізація для виділення
                if (isSelected) {
                    panel.setBackground(list.getSelectionBackground());
                    headerLabel.setForeground(list.getSelectionForeground());
                    textArea.setForeground(list.getSelectionForeground());
                } else {
                    panel.setBackground(list.getBackground());
                    headerLabel.setForeground(list.getForeground());
                    textArea.setForeground(list.getForeground());
                }
            }
            
            return panel;
        }
    }
} 