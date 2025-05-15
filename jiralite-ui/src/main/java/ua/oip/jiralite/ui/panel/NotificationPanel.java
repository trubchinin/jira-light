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
import ua.oip.jiralite.ui.util.SwingHelper;

/**
 * Панель системи сповіщень
 */
public class NotificationPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    // Константи для типів сповіщень
    public enum NotificationType {
        ISSUE_CREATED,
        ISSUE_UPDATED,
        ISSUE_ASSIGNED,
        STATUS_CHANGED,
        COMMENT_ADDED,
        GENERAL
    }
    
    // Клас для представлення сповіщення
    public static class Notification {
        private String title;
        private String message;
        private NotificationType type;
        private LocalDateTime timestamp;
        private boolean isRead;
        private Long referenceId; // ID задачі або іншого об'єкту
        
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
     * Конструктор панелі сповіщень
     * 
     * @param messages ресурси локалізації
     */
    public NotificationPanel(ResourceBundle messages) {
        this.messages = messages;
        this.themeManager = ThemeManager.getInstance();
        this.prefs = Preferences.userNodeForPackage(NotificationPanel.class);
        
        initializeUI();
        addThemeChangeListener();
        loadNotificationSettings();
        
        // Додаємо демонстраційні сповіщення
        addDemoNotifications();
    }
    
    /**
     * Ініціалізація інтерфейсу панелі сповіщень
     */
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                "Сповіщення",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Панель з заголовком і лічильником
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel titleLabel = new JLabel("Системні сповіщення");
        titleLabel.setFont(UiConstants.SUBHEADER_FONT);
        
        countLabel = new JLabel("0 нових");
        countLabel.setForeground(themeManager.getCurrentScheme().info);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(countLabel, BorderLayout.EAST);
        
        // Створюємо табличні вкладки
        tabbedPane = new JTabbedPane();
        
        // Вкладка "Всі сповіщення"
        JPanel allNotificationsPanel = createNotificationsListPanel();
        tabbedPane.addTab("Всі", allNotificationsPanel);
        
        // Вкладка "Налаштування"
        JPanel settingsPanel = createSettingsPanel();
        tabbedPane.addTab("Налаштування", settingsPanel);
        
        // Основна панель
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // Застосовуємо поточну тему
        applyTheme();
    }
    
    /**
     * Створює панель зі списком сповіщень
     */
    private JPanel createNotificationsListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Створюємо модель і список сповіщень
        notificationModel = new DefaultListModel<>();
        notificationList = new JList<>(notificationModel);
        notificationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Налаштовуємо відображення елементів списку
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
                    
                    // Заголовок і час
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
                    
                    // Текст повідомлення
                    JLabel messageLabel = new JLabel("<html>" + notification.getMessage() + "</html>");
                    messageLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
                    
                    // Тип сповіщення (іконка або текст)
                    String typeText = getNotificationTypeText(notification.getType());
                    JLabel typeLabel = new JLabel(typeText);
                    typeLabel.setForeground(getNotificationTypeColor(notification.getType()));
                    typeLabel.setFont(typeLabel.getFont().deriveFont(10f));
                    typeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                    
                    // Додавання компонентів на панель
                    panel.add(headerPanel, BorderLayout.NORTH);
                    panel.add(messageLabel, BorderLayout.CENTER);
                    panel.add(typeLabel, BorderLayout.SOUTH);
                    
                    // При виділенні змінюємо колір
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
        
        // Додаємо обробник кліку по сповіщенню
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
        
        // Поміщаємо список у панель з прокруткою
        JScrollPane scrollPane = new JScrollPane(notificationList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Панель з кнопками
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton markAllReadButton = new JButton("Позначити всі як прочитані");
        markAllReadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAllNotificationsAsRead();
            }
        });
        
        JButton clearAllButton = new JButton("Очистити всі");
        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllNotifications();
            }
        });
        
        // Застосовуємо стиль до кнопок
        SwingHelper.applyButtonStyle(markAllReadButton);
        SwingHelper.applyButtonStyle(clearAllButton);
        
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(markAllReadButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(clearAllButton);
        
        // Додаємо компоненти на панель
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Створює панель налаштувань сповіщень
     */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Заголовок
        JLabel titleLabel = new JLabel("Налаштування сповіщень");
        titleLabel.setFont(UiConstants.SUBHEADER_FONT);
        
        // Прапорці для типів сповіщень
        JPanel checkboxPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        settingsCheckboxes = new ArrayList<>();
        
        // Створюємо прапорці для кожного типу сповіщень
        JCheckBox issueCreatedCheckbox = new JCheckBox("Сповіщати про створення нових задач", 
                prefs.getBoolean("notify.ISSUE_CREATED", true));
        JCheckBox issueUpdatedCheckbox = new JCheckBox("Сповіщати про оновлення задач", 
                prefs.getBoolean("notify.ISSUE_UPDATED", true));
        JCheckBox issueAssignedCheckbox = new JCheckBox("Сповіщати про призначення задач", 
                prefs.getBoolean("notify.ISSUE_ASSIGNED", true));
        JCheckBox statusChangedCheckbox = new JCheckBox("Сповіщати про зміну статусу задач", 
                prefs.getBoolean("notify.STATUS_CHANGED", true));
        JCheckBox commentAddedCheckbox = new JCheckBox("Сповіщати про нові коментарі", 
                prefs.getBoolean("notify.COMMENT_ADDED", true));
        JCheckBox generalCheckbox = new JCheckBox("Системні сповіщення", 
                prefs.getBoolean("notify.GENERAL", true));
        
        // Додаємо прапорці в список і на панель
        settingsCheckboxes.add(issueCreatedCheckbox);
        settingsCheckboxes.add(issueUpdatedCheckbox);
        settingsCheckboxes.add(issueAssignedCheckbox);
        settingsCheckboxes.add(statusChangedCheckbox);
        settingsCheckboxes.add(commentAddedCheckbox);
        settingsCheckboxes.add(generalCheckbox);
        
        for (JCheckBox checkbox : settingsCheckboxes) {
            checkboxPanel.add(checkbox);
            
            // Додаємо обробник для збереження налаштувань
            checkbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveNotificationSettings();
                }
            });
        }
        
        // Кнопка "Зберегти налаштування"
        JButton saveButton = new JButton("Зберегти налаштування");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveNotificationSettings();
            }
        });
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(saveButton, BorderLayout.EAST);
        
        // Додаємо компоненти на панель
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(checkboxPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
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
        
        // Оновлюємо рамку панелі
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentScheme().border, 1),
                "Сповіщення",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UiConstants.SUBHEADER_FONT,
                themeManager.getCurrentScheme().textPrimary));
        
        // Оновлюємо масштаб
        float scale = themeManager.getCurrentScale().getFactor();
        
        // Оновлюємо шрифти
        countLabel.setFont(UiConstants.DEFAULT_FONT.deriveFont(UiConstants.DEFAULT_FONT.getSize() * scale));
        
        // Оновлюємо відрисовку
        revalidate();
        repaint();
    }
    
    /**
     * Додає нове сповіщення
     * 
     * @param notification об'єкт сповіщення
     */
    public void addNotification(Notification notification) {
        // Перевіряємо налаштування для цього типу сповіщень
        if (isNotificationTypeEnabled(notification.getType())) {
            notificationModel.add(0, notification);
            updateUnreadCount();
        }
    }
    
    /**
     * Додає демонстраційні сповіщення
     */
    private void addDemoNotifications() {
        // Демо-сповіщення для різних типів
        Notification notification1 = new Notification(
                "Нову задачу створено", 
                "Задачу DEMO-7 'Тестування системи сповіщень' було створено користувачем 'Administrator'", 
                NotificationType.ISSUE_CREATED, 7L);
        
        Notification notification2 = new Notification(
                "Змінено статус задачі", 
                "Задачу DEMO-2 'Розробка UI' переміщено у статус 'Done'", 
                NotificationType.STATUS_CHANGED, 2L);
        
        Notification notification3 = new Notification(
                "Вас призначено виконавцем", 
                "Вас було призначено виконавцем задачі DEMO-4 'Покращення безпеки'", 
                NotificationType.ISSUE_ASSIGNED, 4L);
        
        // Додаємо сповіщення у список
        notificationModel.add(0, notification1);
        notificationModel.add(0, notification2);
        notificationModel.add(0, notification3);
        
        // Оновлюємо лічильник непрочитаних
        updateUnreadCount();
    }
    
    /**
     * Оновлює лічильник непрочитаних сповіщень
     */
    private void updateUnreadCount() {
        int unreadCount = 0;
        
        for (int i = 0; i < notificationModel.getSize(); i++) {
            if (!notificationModel.getElementAt(i).isRead()) {
                unreadCount++;
            }
        }
        
        countLabel.setText(unreadCount + " нових");
        
        // Оновлюємо зовнішній вигляд лічильника
        if (unreadCount > 0) {
            countLabel.setForeground(themeManager.getCurrentScheme().danger);
            countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD));
        } else {
            countLabel.setForeground(themeManager.getCurrentScheme().textSecondary);
            countLabel.setFont(countLabel.getFont().deriveFont(Font.PLAIN));
        }
    }
    
    /**
     * Позначає всі сповіщення як прочитані
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
     * Очищає всі сповіщення
     */
    private void clearAllNotifications() {
        notificationModel.clear();
        updateUnreadCount();
    }
    
    /**
     * Зберігає налаштування сповіщень
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
     * Завантажує налаштування сповіщень
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
     * Перевіряє, чи увімкнені сповіщення вказаного типу
     * 
     * @param type тип сповіщення
     * @return true, якщо сповіщення цього типу увімкнені
     */
    private boolean isNotificationTypeEnabled(NotificationType type) {
        return prefs.getBoolean("notify." + type.name(), true);
    }
    
    /**
     * Повертає текстове представлення типу сповіщення
     * 
     * @param type тип сповіщення
     * @return текстове представлення
     */
    private String getNotificationTypeText(NotificationType type) {
        switch (type) {
            case ISSUE_CREATED: return "Створення задачі";
            case ISSUE_UPDATED: return "Оновлення задачі";
            case ISSUE_ASSIGNED: return "Призначення виконавця";
            case STATUS_CHANGED: return "Зміна статусу";
            case COMMENT_ADDED: return "Новий коментар";
            case GENERAL: return "Системне сповіщення";
            default: return "";
        }
    }
    
    /**
     * Повертає колір для типу сповіщення
     * 
     * @param type тип сповіщення
     * @return колір
     */
    private Color getNotificationTypeColor(NotificationType type) {
        switch (type) {
            case ISSUE_CREATED: return new Color(46, 204, 113); // Зелений
            case ISSUE_UPDATED: return new Color(52, 152, 219); // Синій
            case ISSUE_ASSIGNED: return new Color(155, 89, 182); // Фіолетовий
            case STATUS_CHANGED: return new Color(241, 196, 15); // Жовтий
            case COMMENT_ADDED: return new Color(230, 126, 34); // Оранжевий
            case GENERAL: return new Color(149, 165, 166); // Сірий
            default: return Color.GRAY;
        }
    }
} 