package ua.oip.jiralite.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.SwingConstants;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.util.ThemeManager;
import ua.oip.jiralite.ui.util.UiConstants;
import ua.oip.jiralite.ui.util.SwingHelper;

/**
 * Панель пошуку та фільтрації задач
 */
public class SearchPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private final ResourceBundle messages;
    private final BoardService boardService;
    private final ThemeManager themeManager;
    
    private JTextField searchField;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> priorityComboBox;
    private JComboBox<String> assigneeComboBox;
    private JCheckBox includeDescription;
    private JButton searchButton;
    private JButton resetButton;
    
    private List<Issue> allIssues = new ArrayList<>();
    private Consumer<List<Issue>> searchResultHandler;
    
    /**
     * Конструктор панелі пошуку
     * 
     * @param messages ресурси локалізації
     * @param boardService сервіс дошок
     */
    public SearchPanel(ResourceBundle messages, BoardService boardService) {
        this.messages = messages;
        this.boardService = boardService;
        this.themeManager = ThemeManager.getInstance();
        
        initializeUI();
        addThemeChangeListener();
    }
    
    /**
     * Ініціалізація інтерфейсу панелі пошуку
     */
    private void initializeUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                messages.getString("search.title"),
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Панель з полем пошуку
        JPanel searchInputPanel = new JPanel();
        searchInputPanel.setLayout(new BoxLayout(searchInputPanel, BoxLayout.Y_AXIS));
        
        // Створюємо мітку і поле для пошуку
        JLabel searchLabel = new JLabel(messages.getString("search.label"));
        searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchField = new JTextField(20);
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchField.getPreferredSize().height));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
        
        includeDescription = new JCheckBox(messages.getString("search.include_description"), true);
        includeDescription.setSelected(true);
        includeDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        searchInputPanel.add(searchLabel);
        searchInputPanel.add(Box.createVerticalStrut(2));
        searchInputPanel.add(searchField);
        searchInputPanel.add(Box.createVerticalStrut(2));
        searchInputPanel.add(includeDescription);
        
        // Панель з фільтрами
        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
        
        // Фільтр за статусом
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        JLabel statusLabel = new JLabel(messages.getString("search.status"));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusComboBox = new JComboBox<String>(
                new String[]{messages.getString("search.all"), 
                             messages.getString("status.todo"), 
                             messages.getString("status.in_progress"), 
                             messages.getString("status.done")});
        statusPanel.add(statusLabel);
        statusPanel.add(statusComboBox);
        
        // Фільтр за пріоритетом
        JPanel priorityPanel = new JPanel();
        priorityPanel.setLayout(new BoxLayout(priorityPanel, BoxLayout.Y_AXIS));
        JLabel priorityLabel = new JLabel(messages.getString("search.priority"));
        priorityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        priorityComboBox = new JComboBox<String>(
                new String[]{messages.getString("search.all"), 
                             messages.getString("priority.lowest"), 
                             messages.getString("priority.low"), 
                             messages.getString("priority.medium"), 
                             messages.getString("priority.high"), 
                             messages.getString("priority.highest")});
        priorityPanel.add(priorityLabel);
        priorityPanel.add(priorityComboBox);
        
        // Фільтр за виконавцем
        JPanel assigneePanel = new JPanel();
        assigneePanel.setLayout(new BoxLayout(assigneePanel, BoxLayout.Y_AXIS));
        JLabel assigneeLabel = new JLabel(messages.getString("search.assignee"));
        assigneeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        assigneeComboBox = new JComboBox<String>(
                new String[]{messages.getString("search.all"), messages.getString("search.not_assigned")});
        assigneePanel.add(assigneeLabel);
        assigneePanel.add(assigneeComboBox);
        
        filtersPanel.add(statusPanel);
        filtersPanel.add(priorityPanel);
        filtersPanel.add(assigneePanel);
        
        // Панель з кнопками
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        
        resetButton = new JButton(messages.getString("search.reset"));
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFilters();
            }
        });
        
        searchButton = new JButton(messages.getString("search.find"));
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Застосовуємо стиль до кнопок
        SwingHelper.applyButtonStyle(resetButton);
        SwingHelper.applyButtonStyle(searchButton);
        
        buttonsPanel.add(resetButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(searchButton);
        
        // Додаємо компоненти напряму до SearchPanel (яка тепер BoxLayout.Y_AXIS)
        add(Box.createVerticalStrut(5)); // Невеликий відступ зверху всередині рамки
        add(searchInputPanel);
        add(Box.createVerticalStrut(10));
        add(filtersPanel);
        add(Box.createVerticalStrut(10));
        add(buttonsPanel);
        add(Box.createVerticalStrut(5)); // Невеликий відступ знизу всередині рамки
        
        // Налаштування розмірів SearchPanel
        setMinimumSize(new Dimension(130, 0)); // Дуже вузька мінімальна ширина, висота розрахується
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)); // Дозволяємо рости по висоті скільки потрібно
        
        // Застосовуємо поточну тему
        applyTheme();
    }
    
    /**
     * Додає слухача зміни теми
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
     * Застосовує поточну тему до компонентів
     */
    private void applyTheme() {
        // Оновлюємо кольори та шрифти
        setBackground(themeManager.getCurrentScheme().background);
        
        // Оновлюємо рамку панелі
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                messages.getString("search.title"),
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Оновлюємо компоненти
        searchField.setBackground(themeManager.getCurrentScheme().panelBackground);
        searchField.setForeground(themeManager.getCurrentScheme().textPrimary);
        statusComboBox.setBackground(themeManager.getCurrentScheme().panelBackground);
        statusComboBox.setForeground(themeManager.getCurrentScheme().textPrimary);
        priorityComboBox.setBackground(themeManager.getCurrentScheme().panelBackground);
        priorityComboBox.setForeground(themeManager.getCurrentScheme().textPrimary);
        assigneeComboBox.setBackground(themeManager.getCurrentScheme().panelBackground);
        assigneeComboBox.setForeground(themeManager.getCurrentScheme().textPrimary);
        includeDescription.setBackground(themeManager.getCurrentScheme().background);
        includeDescription.setForeground(themeManager.getCurrentScheme().textPrimary);
        
        // Оновлюємо масштаб
        float scale = themeManager.getCurrentScale().getFactor();
        
        Font regularFont = UiConstants.DEFAULT_FONT.deriveFont(UiConstants.DEFAULT_FONT.getSize() * scale);
        searchField.setFont(regularFont);
        statusComboBox.setFont(regularFont);
        priorityComboBox.setFont(regularFont);
        assigneeComboBox.setFont(regularFont);
        includeDescription.setFont(regularFont);
        searchButton.setFont(regularFont);
        resetButton.setFont(regularFont);
        
        // Оновлюємо відрисовку
        revalidate();
        repaint();
    }
    
    /**
     * Встановлює список всіх задач для пошуку та фільтрації
     * @param issues список задач
     */
    public void setIssues(List<Issue> issues) {
        this.allIssues = issues;
        updateAssigneeFilter();
    }
    
    /**
     * Оновлює список виконавців на основі поточних задач
     */
    private void updateAssigneeFilter() {
        // Отримуємо унікальних виконавців зі списку задач
        Set<String> assignees = new HashSet<>();
        assignees.add(messages.getString("search.all"));
        
        // Додаємо імена виконавців із задач
        allIssues.stream()
                .filter(issue -> issue.getAssignee() != null)
                .map(issue -> issue.getAssignee().getFullName())
                .distinct()
                .forEach(assignees::add);
        
        // Оновлюємо модель комбобокса
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String assignee : assignees) {
            model.addElement(assignee);
        }
        assigneeComboBox.setModel(model);
    }
    
    /**
     * Встановлює обробник результатів пошуку
     * @param handler функція-обробник результатів
     */
    public void setSearchResultHandler(Consumer<List<Issue>> handler) {
        this.searchResultHandler = handler;
    }
    
    /**
     * Виконує пошук та фільтрацію задач
     */
    private void performSearch() {
        if (allIssues == null || allIssues.isEmpty()) {
            return;
        }
        
        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilter = (String) statusComboBox.getSelectedItem();
        String priorityFilter = (String) priorityComboBox.getSelectedItem();
        String assigneeFilter = (String) assigneeComboBox.getSelectedItem();
        boolean searchInDescription = includeDescription.isSelected();
        
        // Фільтруємо задачі
        List<Issue> filteredIssues = allIssues.stream()
                .filter(issue -> {
                    // Фільтр за текстом
                    boolean textMatch = true;
                    if (!searchText.isEmpty()) {
                        boolean titleMatch = issue.getTitle() != null && 
                                          issue.getTitle().toLowerCase().contains(searchText);
                        boolean descMatch = searchInDescription && issue.getDescription() != null && 
                                          issue.getDescription().toLowerCase().contains(searchText);
                        textMatch = titleMatch || descMatch;
                    }
                    
                    // Фільтр за статусом
                    boolean statusMatch = messages.getString("search.all").equals(statusFilter) || 
                                       mapUiStatusToEnum(statusFilter) == issue.getStatus();
                    
                    // Фільтр за пріоритетом
                    boolean priorityMatch = messages.getString("search.all").equals(priorityFilter) || 
                                         mapUiPriorityToEnum(priorityFilter) == issue.getPriority();
                    
                    // Фільтр за виконавцем
                    boolean assigneeMatch = true;
                    if (messages.getString("search.all").equals(assigneeFilter)) {
                        assigneeMatch = true;
                    } else if (messages.getString("search.not_assigned").equals(assigneeFilter)) {
                        assigneeMatch = issue.getAssignee() == null;
                    } else {
                        assigneeMatch = issue.getAssignee() != null && 
                                      issue.getAssignee().getFullName().equals(assigneeFilter);
                    }
                    
                    return textMatch && statusMatch && priorityMatch && assigneeMatch;
                })
                .collect(Collectors.toList());
        
        // Викликаємо обробник результатів
        if (searchResultHandler != null) {
            searchResultHandler.accept(filteredIssues);
        }
    }
    
    /**
     * Скидає всі фільтри
     */
    private void resetFilters() {
        searchField.setText("");
        statusComboBox.setSelectedItem(messages.getString("search.all"));
        priorityComboBox.setSelectedItem(messages.getString("search.all"));
        assigneeComboBox.setSelectedItem(messages.getString("search.all"));
        includeDescription.setSelected(true);
        
        // Виконуємо пошук з порожніми фільтрами (покаже всі задачі)
        performSearch();
    }
    
    /**
     * Перетворює текстове представлення статусу в enum
     */
    private Status mapUiStatusToEnum(String uiStatus) {
        if (uiStatus.equals(messages.getString("status.todo"))) {
            return Status.TO_DO;
        } else if (uiStatus.equals(messages.getString("status.in_progress"))) {
            return Status.IN_PROGRESS;
        } else if (uiStatus.equals(messages.getString("status.done"))) {
            return Status.DONE;
        } else {
            return null;
        }
    }
    
    /**
     * Перетворює текстове представлення пріоритету в enum
     */
    private Priority mapUiPriorityToEnum(String uiPriority) {
        if (uiPriority.equals(messages.getString("priority.lowest"))) {
            return Priority.LOWEST;
        } else if (uiPriority.equals(messages.getString("priority.low"))) {
            return Priority.LOW;
        } else if (uiPriority.equals(messages.getString("priority.medium"))) {
            return Priority.MEDIUM;
        } else if (uiPriority.equals(messages.getString("priority.high"))) {
            return Priority.HIGH;
        } else if (uiPriority.equals(messages.getString("priority.highest"))) {
            return Priority.HIGHEST;
        } else {
            return null;
        }
    }
} 