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
import java.util.List;
import java.util.ResourceBundle;
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

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.util.ThemeManager;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Панель поиска и фильтрации задач
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
     * Конструктор панели поиска
     * 
     * @param messages ресурсы локализации
     * @param boardService сервис досок
     */
    public SearchPanel(ResourceBundle messages, BoardService boardService) {
        this.messages = messages;
        this.boardService = boardService;
        this.themeManager = ThemeManager.getInstance();
        
        initializeUI();
        addThemeChangeListener();
    }
    
    /**
     * Инициализация интерфейса панели поиска
     */
    private void initializeUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                "Поиск и фильтрация",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Панель с полем поиска
        JPanel searchInputPanel = new JPanel();
        searchInputPanel.setLayout(new BoxLayout(searchInputPanel, BoxLayout.Y_AXIS));
        
        JLabel searchLabel = new JLabel("Поиск:");
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
        
        includeDescription = new JCheckBox("Искать в описании");
        includeDescription.setSelected(true);
        includeDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        searchInputPanel.add(searchLabel);
        searchInputPanel.add(Box.createVerticalStrut(2));
        searchInputPanel.add(searchField);
        searchInputPanel.add(Box.createVerticalStrut(2));
        searchInputPanel.add(includeDescription);
        
        // Панель с фильтрами
        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
        
        // Фильтр по статусу
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        JLabel statusLabel = new JLabel("Статус:");
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusComboBox = new JComboBox<>();
        statusComboBox.setModel(new DefaultComboBoxModel<>(
                new String[]{"Все", "To Do", "In Progress", "Done"}));
        statusPanel.add(statusLabel);
        statusPanel.add(statusComboBox);
        
        // Фильтр по приоритету
        JPanel priorityPanel = new JPanel();
        priorityPanel.setLayout(new BoxLayout(priorityPanel, BoxLayout.Y_AXIS));
        JLabel priorityLabel = new JLabel("Приоритет:");
        priorityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        priorityComboBox = new JComboBox<>();
        priorityComboBox.setModel(new DefaultComboBoxModel<>(
                new String[]{"Все", "Lowest", "Low", "Medium", "High", "Highest"}));
        priorityPanel.add(priorityLabel);
        priorityPanel.add(priorityComboBox);
        
        // Фильтр по исполнителю
        JPanel assigneePanel = new JPanel();
        assigneePanel.setLayout(new BoxLayout(assigneePanel, BoxLayout.Y_AXIS));
        JLabel assigneeLabel = new JLabel("Исполнитель:");
        assigneeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        assigneeComboBox = new JComboBox<>();
        assigneeComboBox.setModel(new DefaultComboBoxModel<>(
                new String[]{"Все", "Не назначено"}));
        assigneePanel.add(assigneeLabel);
        assigneePanel.add(assigneeComboBox);
        
        filtersPanel.add(statusPanel);
        filtersPanel.add(priorityPanel);
        filtersPanel.add(assigneePanel);
        
        // Панель с кнопками
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        
        resetButton = new JButton("Сбросить");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFilters();
            }
        });
        
        searchButton = new JButton("Найти");
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
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
        
        // Применить текущую тему
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
        
        // Обновляем рамку панели
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                "Поиск и фильтрация",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Обновляем компоненты
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
        
        // Обновляем масштаб
        float scale = themeManager.getCurrentScale().getFactor();
        
        Font regularFont = UiConstants.DEFAULT_FONT.deriveFont(UiConstants.DEFAULT_FONT.getSize() * scale);
        searchField.setFont(regularFont);
        statusComboBox.setFont(regularFont);
        priorityComboBox.setFont(regularFont);
        assigneeComboBox.setFont(regularFont);
        includeDescription.setFont(regularFont);
        searchButton.setFont(regularFont);
        resetButton.setFont(regularFont);
        
        // Обновляем отрисовку
        revalidate();
        repaint();
    }
    
    /**
     * Устанавливает список всех задач для поиска и фильтрации
     * @param issues список задач
     */
    public void setIssues(List<Issue> issues) {
        this.allIssues = issues;
        updateAssigneeFilter();
    }
    
    /**
     * Обновляет список исполнителей на основе текущих задач
     */
    private void updateAssigneeFilter() {
        // Получаем уникальных исполнителей из списка задач
        List<String> assignees = new ArrayList<>();
        assignees.add("Все");
        assignees.add("Не назначено");
        
        // Добавляем имена исполнителей из задач
        allIssues.stream()
                .filter(issue -> issue.getAssignee() != null)
                .map(issue -> issue.getAssignee().getFullName())
                .distinct()
                .forEach(assignees::add);
        
        // Обновляем модель комбобокса
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String assignee : assignees) {
            model.addElement(assignee);
        }
        assigneeComboBox.setModel(model);
    }
    
    /**
     * Устанавливает обработчик результатов поиска
     * @param handler функция-обработчик результатов
     */
    public void setSearchResultHandler(Consumer<List<Issue>> handler) {
        this.searchResultHandler = handler;
    }
    
    /**
     * Выполняет поиск и фильтрацию задач
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
        
        // Фильтруем задачи
        List<Issue> filteredIssues = allIssues.stream()
                .filter(issue -> {
                    // Фильтр по тексту
                    boolean textMatch = true;
                    if (!searchText.isEmpty()) {
                        boolean titleMatch = issue.getTitle() != null && 
                                          issue.getTitle().toLowerCase().contains(searchText);
                        boolean descMatch = searchInDescription && issue.getDescription() != null && 
                                          issue.getDescription().toLowerCase().contains(searchText);
                        textMatch = titleMatch || descMatch;
                    }
                    
                    // Фильтр по статусу
                    boolean statusMatch = "Все".equals(statusFilter) || 
                                       mapUiStatusToEnum(statusFilter) == issue.getStatus();
                    
                    // Фильтр по приоритету
                    boolean priorityMatch = "Все".equals(priorityFilter) || 
                                         mapUiPriorityToEnum(priorityFilter) == issue.getPriority();
                    
                    // Фильтр по исполнителю
                    boolean assigneeMatch = true;
                    if ("Все".equals(assigneeFilter)) {
                        assigneeMatch = true;
                    } else if ("Не назначено".equals(assigneeFilter)) {
                        assigneeMatch = issue.getAssignee() == null;
                    } else {
                        assigneeMatch = issue.getAssignee() != null && 
                                      issue.getAssignee().getFullName().equals(assigneeFilter);
                    }
                    
                    return textMatch && statusMatch && priorityMatch && assigneeMatch;
                })
                .collect(Collectors.toList());
        
        // Вызываем обработчик результатов
        if (searchResultHandler != null) {
            searchResultHandler.accept(filteredIssues);
        }
    }
    
    /**
     * Сбрасывает все фильтры
     */
    private void resetFilters() {
        searchField.setText("");
        statusComboBox.setSelectedItem("Все");
        priorityComboBox.setSelectedItem("Все");
        assigneeComboBox.setSelectedItem("Все");
        includeDescription.setSelected(true);
        
        // Выполняем поиск с пустыми фильтрами (покажет все задачи)
        performSearch();
    }
    
    /**
     * Преобразует текстовое представление статуса в enum
     */
    private Status mapUiStatusToEnum(String uiStatus) {
        switch (uiStatus) {
            case "To Do": return Status.TO_DO;
            case "In Progress": return Status.IN_PROGRESS;
            case "Done": return Status.DONE;
            default: return null;
        }
    }
    
    /**
     * Преобразует текстовое представление приоритета в enum
     */
    private Priority mapUiPriorityToEnum(String uiPriority) {
        switch (uiPriority) {
            case "Lowest": return Priority.LOWEST;
            case "Low": return Priority.LOW;
            case "Medium": return Priority.MEDIUM;
            case "High": return Priority.HIGH;
            case "Highest": return Priority.HIGHEST;
            default: return null;
        }
    }
} 