package ua.oip.jiralite.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.ui.util.ThemeManager;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Панель системы уведомлений
 */
public class NotificationPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // Константы для типов уведомлений
    public enum NotificationType {
        ISSUE_CREATED,
        ISSUE_UPDATED,
        ISSUE_ASSIGNED,
        STATUS_CHANGED,
        COMMENT_ADDED,
        GENERAL
    }
    
    // Класс для представления уведомления
    public static class Notification {
        private String title;
        private String message;
        private NotificationType type;
        private LocalDateTime timestamp;
        private boolean isRead;
        private Long referenceId; // ID задачи или другого объекта
        
        public Notification(String title, String message, NotificationType type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = LocalDateTime.now();
            this.isRead = false;
        }
        
        public Notification(String title, String message, NotificationType type, Long referenceId) {
            this(title, message, type);
            this.referenceId = referenceId;
        }
        
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public NotificationType getType() { return type; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isRead() { return isRead; }
        public Long getReferenceId() { return referenceId; }
        
        public void setRead(boolean isRead) { this.isRead = isRead; }
        
        @Override
        public String toString() {
            return title;
        }
    }
    
    private final ResourceBundle messages;
    private final ThemeManager themeManager;
    private final Preferences prefs;
    
    private JPanel notificationsPanel;
    private JLabel countLabel;
    private JTabbedPane tabbedPane;
    
    private JList<Notification> notificationList;
    private DefaultListModel<Notification> notificationModel;
    
    private List<JCheckBox> settingsCheckboxes;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
    
    /**
     * Конструктор панели уведомлений
     * 
     * @param messages ресурсы локализации
     */
    public NotificationPanel(ResourceBundle messages) {
        this.messages = messages;
        this.themeManager = ThemeManager.getInstance();
        this.prefs = Preferences.userNodeForPackage(NotificationPanel.class);
        
        initializeUI();
        addThemeChangeListener();
        loadNotificationSettings();
        
        // Добавляем демонстрационные уведомления
        addDemoNotifications();
    }
    
    /**
     * Инициализация интерфейса панели уведомлений
     */
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                "Уведомления",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Панель с заголовком и счетчиком
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel titleLabel = new JLabel("Системные уведомления");
        titleLabel.setFont(UiConstants.SUBHEADER_FONT);
        
        countLabel = new JLabel("0 новых");
        countLabel.setForeground(themeManager.getCurrentScheme().info);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(countLabel, BorderLayout.EAST);
        
        // Создаем табличные вкладки
        tabbedPane = new JTabbedPane();
        
        // Вкладка "Все уведомления"
        JPanel allNotificationsPanel = createNotificationsListPanel();
        tabbedPane.addTab("Все", allNotificationsPanel);
        
        // Вкладка "Настройки"
        JPanel settingsPanel = createSettingsPanel();
        tabbedPane.addTab("Настройки", settingsPanel);
        
        // Основная панель
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // Применяем текущую тему
        applyTheme();
    }
    
    /**
     * Создает панель со списком уведомлений
     */
    private JPanel createNotificationsListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Создаем модель и список уведомлений
        notificationModel = new DefaultListModel<>();
        notificationList = new JList<>(notificationModel);
        notificationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Настраиваем отображение элементов списка
        notificationList.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                
                JPanel panel = new JPanel(new BorderLayout(5, 3));
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                
                if (value instanceof Notification) {
                    Notification notification = (Notification) value;
                    
                    // Заголовок и время
                    JPanel headerPanel = new JPanel(new BorderLayout());
                    headerPanel.setOpaque(false);
                    
                    JLabel titleLabel = new JLabel(notification.getTitle());
                    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
                    
                    if (!notification.isRead()) {
                        titleLabel.setForeground(themeManager.getCurrentScheme().accentColor);
                    }
                    
                    JLabel timeLabel = new JLabel(notification.getTimestamp().format(TIME_FORMATTER));
                    timeLabel.setForeground(Color.GRAY);
                    timeLabel.setFont(timeLabel.getFont().deriveFont(Font.ITALIC, 10f));
                    
                    headerPanel.add(titleLabel, BorderLayout.WEST);
                    headerPanel.add(timeLabel, BorderLayout.EAST);
                    
                    // Текст сообщения
                    JLabel messageLabel = new JLabel("<html>" + notification.getMessage() + "</html>");
                    messageLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
                    
                    // Тип уведомления (иконка или текст)
                    String typeText = getNotificationTypeText(notification.getType());
                    JLabel typeLabel = new JLabel(typeText);
                    typeLabel.setForeground(getNotificationTypeColor(notification.getType()));
                    typeLabel.setFont(typeLabel.getFont().deriveFont(10f));
                    typeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                    
                    // Добавление компонентов на панель
                    panel.add(headerPanel, BorderLayout.NORTH);
                    panel.add(messageLabel, BorderLayout.CENTER);
                    panel.add(typeLabel, BorderLayout.SOUTH);
                    
                    // При выделении меняем цвет
                    if (isSelected) {
                        panel.setBackground(list.getSelectionBackground());
                        messageLabel.setForeground(list.getSelectionForeground());
                    } else {
                        panel.setBackground(list.getBackground());
                        messageLabel.setForeground(list.getForeground());
                    }
                }
                
                return panel;
            }
        });
        
        // Добавляем обработчик клика по уведомлению
        notificationList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = notificationList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Notification notification = notificationModel.getElementAt(index);
                        notification.setRead(true);
                        updateUnreadCount();
                        notificationList.repaint();
                    }
                }
            }
        });
        
        // Помещаем список в скроллируемую панель
        JScrollPane scrollPane = new JScrollPane(notificationList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Панель с кнопками
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton markAllReadButton = new JButton("Отметить все как прочитанные");
        markAllReadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAllNotificationsAsRead();
            }
        });
        
        JButton clearAllButton = new JButton("Очистить все");
        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllNotifications();
            }
        });
        
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(markAllReadButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(clearAllButton);
        
        // Добавляем компоненты на панель
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Создает панель настроек уведомлений
     */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Заголовок
        JLabel titleLabel = new JLabel("Настройки уведомлений");
        titleLabel.setFont(UiConstants.SUBHEADER_FONT);
        
        // Флажки для типов уведомлений
        JPanel checkboxPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        settingsCheckboxes = new ArrayList<>();
        
        // Создаем флажки для каждого типа уведомлений
        JCheckBox issueCreatedCheckbox = new JCheckBox("Уведомлять о создании новых задач", 
                prefs.getBoolean("notify.ISSUE_CREATED", true));
        JCheckBox issueUpdatedCheckbox = new JCheckBox("Уведомлять об обновлении задач", 
                prefs.getBoolean("notify.ISSUE_UPDATED", true));
        JCheckBox issueAssignedCheckbox = new JCheckBox("Уведомлять о назначении задач", 
                prefs.getBoolean("notify.ISSUE_ASSIGNED", true));
        JCheckBox statusChangedCheckbox = new JCheckBox("Уведомлять об изменении статуса задач", 
                prefs.getBoolean("notify.STATUS_CHANGED", true));
        JCheckBox commentAddedCheckbox = new JCheckBox("Уведомлять о новых комментариях", 
                prefs.getBoolean("notify.COMMENT_ADDED", true));
        JCheckBox generalCheckbox = new JCheckBox("Системные уведомления", 
                prefs.getBoolean("notify.GENERAL", true));
        
        // Добавляем флажки в список и на панель
        settingsCheckboxes.add(issueCreatedCheckbox);
        settingsCheckboxes.add(issueUpdatedCheckbox);
        settingsCheckboxes.add(issueAssignedCheckbox);
        settingsCheckboxes.add(statusChangedCheckbox);
        settingsCheckboxes.add(commentAddedCheckbox);
        settingsCheckboxes.add(generalCheckbox);
        
        for (JCheckBox checkbox : settingsCheckboxes) {
            checkboxPanel.add(checkbox);
            
            // Добавляем обработчик для сохранения настроек
            checkbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveNotificationSettings();
                }
            });
        }
        
        // Кнопка "Сохранить настройки"
        JButton saveButton = new JButton("Сохранить настройки");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveNotificationSettings();
            }
        });
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(saveButton, BorderLayout.EAST);
        
        // Добавляем компоненты на панель
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(checkboxPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
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
        
        if (tabbedPane != null) {
            tabbedPane.setBackground(themeManager.getCurrentScheme().background);
            tabbedPane.setForeground(themeManager.getCurrentScheme().textPrimary);
        }
        
        if (notificationList != null) {
            notificationList.setBackground(themeManager.getCurrentScheme().panelBackground);
            notificationList.setForeground(themeManager.getCurrentScheme().textPrimary);
        }
        
        if (settingsCheckboxes != null) {
            for (JCheckBox checkbox : settingsCheckboxes) {
                checkbox.setBackground(themeManager.getCurrentScheme().background);
                checkbox.setForeground(themeManager.getCurrentScheme().textPrimary);
            }
        }
        
        // Обновляем рамку панели
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                "Уведомления",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Обновляем масштаб
        float scale = themeManager.getCurrentScale().getFactor();
        
        // Обновляем шрифты
        countLabel.setFont(UiConstants.DEFAULT_FONT.deriveFont(UiConstants.DEFAULT_FONT.getSize() * scale));
        
        // Обновляем отрисовку
        revalidate();
        repaint();
    }
    
    /**
     * Добавляет новое уведомление
     * 
     * @param notification объект уведомления
     */
    public void addNotification(Notification notification) {
        // Проверяем настройки для этого типа уведомлений
        if (isNotificationTypeEnabled(notification.getType())) {
            notificationModel.add(0, notification);
            updateUnreadCount();
        }
    }
    
    /**
     * Добавляет демонстрационные уведомления
     */
    private void addDemoNotifications() {
        // Демо-уведомления для разных типов
        Notification notification1 = new Notification(
                "Новая задача создана", 
                "Задача DEMO-7 'Тестирование системы уведомлений' была создана пользователем 'Administrator'", 
                NotificationType.ISSUE_CREATED, 7L);
        
        Notification notification2 = new Notification(
                "Изменен статус задачи", 
                "Задача DEMO-2 'Разработка UI' перемещена в статус 'Done'", 
                NotificationType.STATUS_CHANGED, 2L);
        
        Notification notification3 = new Notification(
                "Вы назначены исполнителем", 
                "Вы были назначены исполнителем задачи DEMO-4 'Улучшение безопасности'", 
                NotificationType.ISSUE_ASSIGNED, 4L);
        
        // Добавляем уведомления в список
        notificationModel.add(0, notification1);
        notificationModel.add(0, notification2);
        notificationModel.add(0, notification3);
        
        // Обновляем счетчик непрочитанных
        updateUnreadCount();
    }
    
    /**
     * Обновляет счетчик непрочитанных уведомлений
     */
    private void updateUnreadCount() {
        int unreadCount = 0;
        
        for (int i = 0; i < notificationModel.getSize(); i++) {
            if (!notificationModel.getElementAt(i).isRead()) {
                unreadCount++;
            }
        }
        
        countLabel.setText(unreadCount + " новых");
        
        // Обновляем внешний вид счетчика
        if (unreadCount > 0) {
            countLabel.setForeground(themeManager.getCurrentScheme().danger);
            countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD));
        } else {
            countLabel.setForeground(themeManager.getCurrentScheme().textSecondary);
            countLabel.setFont(countLabel.getFont().deriveFont(Font.PLAIN));
        }
    }
    
    /**
     * Отмечает все уведомления как прочитанные
     */
    private void markAllNotificationsAsRead() {
        for (int i = 0; i < notificationModel.getSize(); i++) {
            Notification notification = notificationModel.getElementAt(i);
            notification.setRead(true);
        }
        
        updateUnreadCount();
        notificationList.repaint();
    }
    
    /**
     * Очищает все уведомления
     */
    private void clearAllNotifications() {
        notificationModel.clear();
        updateUnreadCount();
    }
    
    /**
     * Сохраняет настройки уведомлений
     */
    private void saveNotificationSettings() {
        if (settingsCheckboxes != null) {
            NotificationType[] types = NotificationType.values();
            
            for (int i = 0; i < settingsCheckboxes.size() && i < types.length; i++) {
                JCheckBox checkbox = settingsCheckboxes.get(i);
                NotificationType type = types[i];
                
                prefs.putBoolean("notify." + type.name(), checkbox.isSelected());
            }
        }
    }
    
    /**
     * Загружает настройки уведомлений
     */
    private void loadNotificationSettings() {
        if (settingsCheckboxes != null) {
            NotificationType[] types = NotificationType.values();
            
            for (int i = 0; i < settingsCheckboxes.size() && i < types.length; i++) {
                JCheckBox checkbox = settingsCheckboxes.get(i);
                NotificationType type = types[i];
                
                checkbox.setSelected(prefs.getBoolean("notify." + type.name(), true));
            }
        }
    }
    
    /**
     * Проверяет, включены ли уведомления указанного типа
     * 
     * @param type тип уведомления
     * @return true, если уведомления этого типа включены
     */
    private boolean isNotificationTypeEnabled(NotificationType type) {
        return prefs.getBoolean("notify." + type.name(), true);
    }
    
    /**
     * Возвращает текстовое представление типа уведомления
     * 
     * @param type тип уведомления
     * @return текстовое представление
     */
    private String getNotificationTypeText(NotificationType type) {
        switch (type) {
            case ISSUE_CREATED: return "Создание задачи";
            case ISSUE_UPDATED: return "Обновление задачи";
            case ISSUE_ASSIGNED: return "Назначение исполнителя";
            case STATUS_CHANGED: return "Изменение статуса";
            case COMMENT_ADDED: return "Новый комментарий";
            case GENERAL: return "Системное уведомление";
            default: return "";
        }
    }
    
    /**
     * Возвращает цвет для типа уведомления
     * 
     * @param type тип уведомления
     * @return цвет
     */
    private Color getNotificationTypeColor(NotificationType type) {
        switch (type) {
            case ISSUE_CREATED: return new Color(46, 204, 113); // Зеленый
            case ISSUE_UPDATED: return new Color(52, 152, 219); // Синий
            case ISSUE_ASSIGNED: return new Color(155, 89, 182); // Фиолетовый
            case STATUS_CHANGED: return new Color(241, 196, 15); // Желтый
            case COMMENT_ADDED: return new Color(230, 126, 34); // Оранжевый
            case GENERAL: return new Color(149, 165, 166); // Серый
            default: return Color.GRAY;
        }
    }
} 