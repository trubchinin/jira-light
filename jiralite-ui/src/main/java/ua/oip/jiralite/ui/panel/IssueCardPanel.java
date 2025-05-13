package ua.oip.jiralite.ui.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.ui.model.IssueCardModel;
import ua.oip.jiralite.ui.util.SwingHelper;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Панель для відображення картки задачі на дошці Kanban.
 * Відображає основну інформацію про задачу і підтримує Drag-and-Drop.
 */
public class IssueCardPanel extends JPanel {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    private final IssueCardModel issue;
    private final ResourceBundle messages;
    
    private JLabel titleLabel;
    private JLabel keyLabel;
    private JLabel priorityLabel;
    private JLabel assigneeLabel;
    
    /**
     * Конструктор картки задачі
     * 
     * @param issue модель даних задачі
     * @param messages ресурси локалізації
     */
    public IssueCardPanel(IssueCardModel issue, ResourceBundle messages) {
        this.issue = issue;
        this.messages = messages;
        
        initializeUI();
    }
    
    /**
     * Ініціалізація компонентів інтерфейсу
     */
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(UiConstants.CARD_BORDER);
        setBackground(UiConstants.PANEL_BACKGROUND);
        
        // Заголовок з ключем та пріоритетом
        JPanel headerPanel = new JPanel(new BorderLayout(5, 0));
        headerPanel.setOpaque(false);
        
        keyLabel = new JLabel(issue.getKey());
        keyLabel.setFont(UiConstants.SMALL_FONT);
        keyLabel.setForeground(UiConstants.TEXT_SECONDARY);
        
        priorityLabel = createPriorityLabel(issue.getPriority());
        
        headerPanel.add(keyLabel, BorderLayout.WEST);
        headerPanel.add(priorityLabel, BorderLayout.EAST);
        
        // Заголовок задачі
        titleLabel = new JLabel(issue.getTitle());
        titleLabel.setFont(UiConstants.DEFAULT_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Виконавець задачі
        assigneeLabel = new JLabel(issue.getAssigneeName() != null 
                ? issue.getAssigneeName() : messages.getString("issue.unassigned"));
        assigneeLabel.setFont(UiConstants.SMALL_FONT);
        assigneeLabel.setForeground(UiConstants.TEXT_SECONDARY);
        
        // Додавання компонентів на панель
        add(headerPanel, BorderLayout.NORTH);
        add(titleLabel, BorderLayout.CENTER);
        add(assigneeLabel, BorderLayout.SOUTH);
        
        // Встановлення розміру
        setPreferredSize(new Dimension(200, 80));
    }
    
    /**
     * Створює мітку з пріоритетом задачі у вигляді кольорового кола
     */
    private JLabel createPriorityLabel(Priority priority) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(12, 12));
        label.setOpaque(true);
        
        // Встановлення кольору відповідно до пріоритету
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
        
        return label;
    }
    
    /**
     * Додає обробник натискання миші на картку
     * 
     * @param mouseAdapter обробник подій миші
     */
    public void addCardMouseListener(MouseAdapter mouseAdapter) {
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
        
        // Також додаємо слухачі для всіх дочірніх компонентів
        for (Component component : this.getComponents()) {
            component.addMouseListener(mouseAdapter);
            component.addMouseMotionListener(mouseAdapter);
        }
    }
    
    /**
     * Отримання моделі даних задачі
     */
    public IssueCardModel getIssueModel() {
        return issue;
    }
} 