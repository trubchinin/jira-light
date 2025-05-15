package ua.oip.jiralite.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.ui.util.ThemeManager;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Панель отображения результатов поиска
 */
public class SearchResultPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private final ResourceBundle messages;
    private final ThemeManager themeManager;
    
    private JPanel resultsContainer;
    private JLabel statusLabel;
    private Consumer<Issue> onIssueSelected;
    
    /**
     * Конструктор панели результатов поиска
     * 
     * @param messages ресурсы локализации
     */
    public SearchResultPanel(ResourceBundle messages) {
        this.messages = messages;
        this.themeManager = ThemeManager.getInstance();
        
        initializeUI();
        addThemeChangeListener();
    }
    
    /**
     * Инициализация интерфейса панели результатов
     */
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                messages.getString("search.results"),
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Панель с информацией о результатах
        JPanel infoPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(messages.getString("search.no_results"));
        infoPanel.add(statusLabel, BorderLayout.WEST);
        
        // Панель с результатами
        resultsContainer = new JPanel();
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        
        // Добавление скроллинга
        JScrollPane scrollPane = new JScrollPane(resultsContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Добавление компонентов на панель
        add(infoPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Применяем тему
        applyTheme();
    }
    
    /**
     * Добавляет слушателя изменения темы
     */
    private void addThemeChangeListener() {
        themeManager.addThemeChangeListener(new ThemeManager.ThemeChangeListener() {
            @Override
            public void onThemeChanged() {
                applyTheme();
            }
        });
    }
    
    /**
     * Применяет текущую тему к компонентам
     */
    private void applyTheme() {
        // Обновляем цвета и шрифты
        setBackground(themeManager.getCurrentScheme().background);
        resultsContainer.setBackground(themeManager.getCurrentScheme().background);
        statusLabel.setForeground(themeManager.getCurrentScheme().textPrimary);
        
        // Обновляем рамку панели
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                messages.getString("search.results"),
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Обновляем масштаб
        float scale = themeManager.getCurrentScale().getFactor();
        statusLabel.setFont(UiConstants.DEFAULT_FONT.deriveFont(UiConstants.DEFAULT_FONT.getSize() * scale));
        
        // Обновляем каждую карточку результата
        for (Component comp : resultsContainer.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel resultCard = (JPanel) comp;
                resultCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
                resultCard.setBackground(themeManager.getCurrentScheme().panelBackground);
                
                // Обновляем текстовые метки внутри карточки
                for (Component innerComp : resultCard.getComponents()) {
                    if (innerComp instanceof JLabel) {
                        JLabel label = (JLabel) innerComp;
                        label.setForeground(themeManager.getCurrentScheme().textPrimary);
                        
                        // Устанавливаем масштабированный шрифт
                        if (label.getFont().isBold()) {
                            label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize() * scale));
                        } else {
                            label.setFont(label.getFont().deriveFont(label.getFont().getSize() * scale));
                        }
                    }
                }
            }
        }
        
        // Обновляем отрисовку
        revalidate();
        repaint();
    }
    
    /**
     * Отображает результаты поиска
     * 
     * @param issues список найденных задач
     */
    public void displayResults(List<Issue> issues) {
        // Очищаем контейнер результатов
        resultsContainer.removeAll();
        
        if (issues == null || issues.isEmpty()) {
            statusLabel.setText(messages.getString("search.no_results"));
            resultsContainer.revalidate();
            resultsContainer.repaint();
            return;
        }
        
        // Обновляем статусную метку
        statusLabel.setText(java.text.MessageFormat.format(messages.getString("search.found_issues"), issues.size()));
        
        // Масштаб UI
        float scale = themeManager.getCurrentScale().getFactor();
        
        // Добавляем карточки для каждой задачи
        for (Issue issue : issues) {
            JPanel resultCard = createResultCard(issue, scale);
            resultsContainer.add(resultCard);
            resultsContainer.add(Box.createVerticalStrut(5));
        }
        
        // Обновляем отрисовку
        resultsContainer.revalidate();
        resultsContainer.repaint();
    }
    
    /**
     * Создает карточку для отображения задачи в результатах поиска
     * 
     * @param issue задача
     * @param scale масштаб UI
     * @return панель с информацией о задаче
     */
    private JPanel createResultCard(Issue issue, float scale) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        card.setBackground(themeManager.getCurrentScheme().panelBackground);
        
        // Исходный размер панели
        int cardWidth = (int)(450 * scale);
        int cardHeight = (int)(80 * scale);
        
        card.setPreferredSize(new Dimension(cardWidth, cardHeight));
        card.setMaximumSize(new Dimension(cardWidth, cardHeight));
        card.setMinimumSize(new Dimension(cardWidth, cardHeight));
        
        // Добавление слушателя для выбора задачи
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(card.getBackground().brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(themeManager.getCurrentScheme().panelBackground);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onIssueSelected != null) {
                    onIssueSelected.accept(issue);
                }
            }
        });
        
        // Создаем компоненты карточки
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        
        // Ключ и статус
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        
        JPanel keyStatusPanel = new JPanel(new BorderLayout(5, 0));
        keyStatusPanel.setOpaque(false);
        
        JLabel keyLabel = new JLabel(issue.getKey());
        keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD, 12 * scale));
        keyLabel.setForeground(themeManager.getCurrentScheme().accentColor);
        
        JLabel statusLabel = createStatusBadge(issue.getStatus(), scale);
        
        keyStatusPanel.add(keyLabel, BorderLayout.WEST);
        keyStatusPanel.add(statusLabel, BorderLayout.EAST);
        
        card.add(keyStatusPanel, gbc);
        
        // Приоритет
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        
        JLabel priorityLabel = createPriorityBadge(issue.getPriority(), scale);
        card.add(priorityLabel, gbc);
        
        // Заголовок задачи
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        
        JLabel titleLabel = new JLabel(issue.getTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13 * scale));
        card.add(titleLabel, gbc);
        
        // Исполнитель
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        
        String assigneeText = messages.getString("issue.not_assigned");
        if (issue.getAssignee() != null) {
            assigneeText = messages.getString("issue.assignee") + ": " + issue.getAssignee().getFullName();
        }
        
        JLabel assigneeLabel = new JLabel(assigneeText);
        assigneeLabel.setFont(assigneeLabel.getFont().deriveFont(Font.ITALIC, 12 * scale));
        assigneeLabel.setForeground(themeManager.getCurrentScheme().textSecondary);
        card.add(assigneeLabel, gbc);
        
        return card;
    }
    
    /**
     * Создает визуальный индикатор статуса
     * 
     * @param status статус задачи
     * @param scale масштаб UI
     * @return метка с визуальным индикатором статуса
     */
    private JLabel createStatusBadge(Status status, float scale) {
        JLabel badge = new JLabel();
        
        String text;
        Color background;
        Color foreground = Color.WHITE;
        
        switch (status) {
            case TO_DO:
                text = "TO DO";
                background = new Color(120, 120, 120);
                break;
            case IN_PROGRESS:
                text = "IN PROGRESS";
                background = new Color(41, 128, 185);
                break;
            case DONE:
                text = "DONE";
                background = new Color(39, 174, 96);
                break;
            default:
                text = "UNKNOWN";
                background = Color.GRAY;
        }
        
        badge.setText(text);
        badge.setForeground(foreground);
        badge.setOpaque(true);
        badge.setBackground(background);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        badge.setFont(badge.getFont().deriveFont(10f * scale));
        
        return badge;
    }
    
    /**
     * Создает визуальный индикатор приоритета
     * 
     * @param priority приоритет задачи
     * @param scale масштаб UI
     * @return метка с визуальным индикатором приоритета
     */
    private JLabel createPriorityBadge(Priority priority, float scale) {
        JLabel badge = new JLabel();
        
        String text;
        Color background;
        Color foreground = Color.WHITE;
        
        switch (priority) {
            case LOWEST:
                text = "LOWEST";
                background = new Color(180, 180, 180);
                break;
            case LOW:
                text = "LOW";
                background = new Color(76, 175, 80);
                break;
            case MEDIUM:
                text = "MEDIUM";
                background = new Color(33, 150, 243);
                break;
            case HIGH:
                text = "HIGH";
                background = new Color(255, 152, 0);
                break;
            case HIGHEST:
                text = "HIGHEST";
                background = new Color(244, 67, 54);
                break;
            default:
                text = "MEDIUM";
                background = new Color(33, 150, 243);
        }
        
        badge.setText(text);
        badge.setForeground(foreground);
        badge.setOpaque(true);
        badge.setBackground(background);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        badge.setFont(badge.getFont().deriveFont(10f * scale));
        
        return badge;
    }
    
    /**
     * Устанавливает обработчик события выбора задачи
     * 
     * @param handler обработчик события
     */
    public void setOnIssueSelectedHandler(Consumer<Issue> handler) {
        this.onIssueSelected = handler;
    }
} 