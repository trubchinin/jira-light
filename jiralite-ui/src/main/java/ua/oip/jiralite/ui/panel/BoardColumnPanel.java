package ua.oip.jiralite.ui.panel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.ui.listener.ColumnDropTarget;
import ua.oip.jiralite.ui.listener.IssueCardMouseAdapter;
import ua.oip.jiralite.ui.model.IssueCardModel;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Панель для відображення однієї колонки Kanban-дошки.
 * Підтримує Drag-n-Drop картки задачі між колонками.
 */
public class BoardColumnPanel extends JPanel {
    
    private final String title;
    private final Status status;
    private final ResourceBundle messages;
    private final List<IssueCardPanel> issueCards = new ArrayList<>();
    
    private JPanel cardsPanel;
    
    /**
     * Конструктор колонки дошки
     * 
     * @param title заголовок колонки
     * @param status статус задач у колонці
     * @param messages ресурси локалізації
     */
    public BoardColumnPanel(String title, Status status, ResourceBundle messages) {
        this.title = title;
        this.status = status;
        this.messages = messages;
        
        initializeUI();
    }
    
    /**
     * Ініціалізація компонентів інтерфейсу
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UiConstants.TEXT_SECONDARY, 1, true),
                title,
                TitledBorder.CENTER, 
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                UiConstants.TEXT_PRIMARY));
        
        // Панель для карток з вертикальним BoxLayout
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(UiConstants.BACKGROUND_COLOR);
        
        // Додаємо скрол-панель, щоб прокручувати картки, якщо їх багато
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Встановлюємо розмір
        setPreferredSize(new Dimension(220, 500));
    }
    
    /**
     * Додає обробник Drag-n-Drop для колонки
     * 
     * @param dropHandler обробник перетягування
     */
    public void setDropTarget(ColumnDropTarget dropHandler) {
        new DropTarget(cardsPanel, dropHandler);
    }
    
    /**
     * Додає задачу до колонки
     * 
     * @param issue модель задачі
     * @param dragHandler обробник перетягування карток
     */
    public void addIssue(IssueCardModel issue, IssueCardMouseAdapter dragHandler) {
        // Створюємо картку задачі
        IssueCardPanel card = new IssueCardPanel(issue, messages);
        card.addCardMouseListener(dragHandler);
        
        // Додаємо картку до колонки з відступом між картками
        cardsPanel.add(Box.createVerticalStrut(UiConstants.COMPONENT_SPACING));
        cardsPanel.add(card);
        issueCards.add(card);
        
        // Оновлюємо інтерфейс
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
    
    /**
     * Видаляє задачу з колонки
     * 
     * @param issueId ідентифікатор задачі
     */
    public void removeIssue(Long issueId) {
        IssueCardPanel cardToRemove = null;
        
        // Шукаємо картку для видалення
        for (IssueCardPanel card : issueCards) {
            if (card.getIssueModel().getId().equals(issueId)) {
                cardToRemove = card;
                break;
            }
        }
        
        // Видаляємо картку, якщо знайдена
        if (cardToRemove != null) {
            int index = issueCards.indexOf(cardToRemove);
            cardsPanel.remove(cardToRemove);
            
            // Видаляємо також відступ перед карткою, якщо це не перша картка
            if (index > 0 && index < cardsPanel.getComponentCount()) {
                cardsPanel.remove(index - 1); // Видаляємо відступ
            }
            
            issueCards.remove(cardToRemove);
            
            // Оновлюємо інтерфейс
            cardsPanel.revalidate();
            cardsPanel.repaint();
        }
    }
    
    /**
     * Отримання статусу колонки
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Отримання списку карток задач
     */
    public List<IssueCardPanel> getIssueCards() {
        return issueCards;
    }
    
    /**
     * Очищення всіх карток у колонці
     */
    public void clearIssues() {
        cardsPanel.removeAll();
        issueCards.clear();
        
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
} 