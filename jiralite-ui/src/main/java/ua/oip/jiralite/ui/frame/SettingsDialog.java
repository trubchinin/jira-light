package ua.oip.jiralite.ui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.oip.jiralite.ui.util.ThemeManager;
import ua.oip.jiralite.ui.util.ThemeManager.Theme;
import ua.oip.jiralite.ui.util.ThemeManager.UiScale;
import ua.oip.jiralite.ui.util.SwingHelper;

/**
 * Діалог налаштувань інтерфейсу
 */
public class SettingsDialog extends JDialog {
    
    private static final Logger log = LoggerFactory.getLogger(SettingsDialog.class);
    
    private final ResourceBundle messages;
    private final ThemeManager themeManager;
    
    private JRadioButton lightThemeRadio;
    private JRadioButton darkThemeRadio;
    private JRadioButton systemThemeRadio;
    private JComboBox<UiScale> scaleComboBox;
    private JSlider scaleSlider;
    private JPanel lightThemePreview;
    private JPanel darkThemePreview;
    
    /**
     * Конструктор діалогу налаштувань
     * 
     * @param owner батьківське вікно
     * @param messages ресурси локалізації
     */
    public SettingsDialog(Frame owner, ResourceBundle messages) {
        super(owner, "Налаштування", true);
        this.messages = messages;
        this.themeManager = ThemeManager.getInstance();
        
        initializeUI();
        loadCurrentSettings();
    }
    
    /**
     * Ініціалізація інтерфейсу
     */
    private void initializeUI() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Створюємо панель з вкладками
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 1. Вкладка "Зовнішній вигляд"
        JPanel appearancePanel = createAppearancePanel();
        tabbedPane.addTab("Зовнішній вигляд", appearancePanel);
        
        // 2. Вкладка "Інтерфейс"
        JPanel interfacePanel = createInterfacePanel();
        tabbedPane.addTab("Інтерфейс", interfacePanel);
        
        // Додаємо панель з вкладками до контентної панелі
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Кнопки дій
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("Скасувати");
        cancelButton.addActionListener(e -> dispose());
        
        JButton applyButton = new JButton("Застосувати");
        applyButton.addActionListener(e -> {
            applySettings();
        });
        
        JButton okButton = new JButton("Зберегти");
        okButton.addActionListener(e -> {
            applySettings();
            dispose();
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(okButton);
        
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(contentPanel);
        setSize(550, 450);
        setMinimumSize(new Dimension(450, 350));
        setLocationRelativeTo(getOwner());
    }
    
    /**
     * Створення панелі налаштувань зовнішнього вигляду
     * 
     * @return панель з налаштуваннями зовнішнього вигляду
     */
    private JPanel createAppearancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 1. Секція вибору теми з превью
        JPanel themePanel = new JPanel(new GridBagLayout());
        themePanel.setBorder(BorderFactory.createTitledBorder("Тема"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 5, 5, 15);
        
        // Радіо-кнопки для вибору теми
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        
        lightThemeRadio = new JRadioButton("Світла");
        darkThemeRadio = new JRadioButton("Темна");
        systemThemeRadio = new JRadioButton("Системна");
        
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightThemeRadio);
        themeGroup.add(darkThemeRadio);
        themeGroup.add(systemThemeRadio);
        
        // Додаємо слухачів для оновлення превью
        lightThemeRadio.addActionListener(e -> updateThemePreviewSelection(true));
        darkThemeRadio.addActionListener(e -> updateThemePreviewSelection(false));
        systemThemeRadio.addActionListener(e -> updateThemePreviewSelection(true));
        
        // Вирівнювання радіо-кнопок
        lightThemeRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        darkThemeRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        systemThemeRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        radioPanel.add(lightThemeRadio);
        radioPanel.add(Box.createVerticalStrut(8));
        radioPanel.add(darkThemeRadio);
        radioPanel.add(Box.createVerticalStrut(8));
        radioPanel.add(systemThemeRadio);
        
        themePanel.add(radioPanel, gbc);
        
        // Створюємо превью тем
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.X_AXIS));
        
        // Світла тема превью
        lightThemePreview = createThemePreview(ThemeManager.LIGHT_SCHEME);
        lightThemePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        
        // Темна тема превью
        darkThemePreview = createThemePreview(ThemeManager.DARK_SCHEME);
        darkThemePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        previewPanel.add(lightThemePreview);
        previewPanel.add(Box.createHorizontalStrut(10));
        previewPanel.add(darkThemePreview);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        themePanel.add(previewPanel, gbc);
        
        panel.add(themePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Створює панель превью теми
     * 
     * @param scheme кольорова схема
     * @return панель превью
     */
    private JPanel createThemePreview(ThemeManager.ColorScheme scheme) {
        JPanel preview = new JPanel();
        preview.setLayout(new BorderLayout());
        preview.setBackground(scheme.background);
        preview.setPreferredSize(new Dimension(180, 200));
        
        // Заголовок
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(scheme.panelBackground);
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JLabel headerLabel = new JLabel("Превью теми");
        headerLabel.setForeground(scheme.textPrimary);
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD));
        headerPanel.add(headerLabel);
        
        // Контент
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(scheme.panelBackground);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Елементи інтерфейсу
        JLabel titleLabel = new JLabel("Назва задачі");
        titleLabel.setForeground(scheme.textPrimary);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        
        JLabel descLabel = new JLabel("Опис задачі");
        descLabel.setForeground(scheme.textSecondary);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(scheme.panelBackground);
        
        JLabel statusLabel = new JLabel("В роботі");
        statusLabel.setForeground(scheme.info);
        statusPanel.add(statusLabel);
        
        JButton button = new JButton("Кнопка");
        button.setBackground(scheme.accentColor);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(80, 25));
        
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(descLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(statusPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(button);
        
        // Додавання компонентів
        preview.add(headerPanel, BorderLayout.NORTH);
        preview.add(contentPanel, BorderLayout.CENTER);
        
        return preview;
    }
    
    /**
     * Створення панелі налаштувань інтерфейсу
     * 
     * @return панель з налаштуваннями інтерфейсу
     */
    private JPanel createInterfacePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Секція масштабу інтерфейсу
        JPanel scalePanel = new JPanel();
        scalePanel.setLayout(new BoxLayout(scalePanel, BoxLayout.Y_AXIS));
        scalePanel.setBorder(BorderFactory.createTitledBorder("Масштаб інтерфейсу"));
        scalePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Випадаючий список
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel scaleLabel = new JLabel("Розмір елементів:");
        scaleComboBox = new JComboBox<>(UiScale.values());
        
        scaleComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scaleComboBox.getSelectedItem() != null) {
                    UiScale selectedScale = (UiScale) scaleComboBox.getSelectedItem();
                    updateScaleSlider(selectedScale);
                }
            }
        });
        
        comboPanel.add(scaleLabel);
        comboPanel.add(scaleComboBox);
        
        // Слайдер масштабу
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        sliderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        scaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 3, 1);
        scaleSlider.setMajorTickSpacing(1);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.setSnapToTicks(true);

        // Налаштування підписів слайдера
        java.util.Hashtable<Integer, JLabel> labels = new java.util.Hashtable<>();
        labels.put(0, new JLabel("80%"));
        labels.put(1, new JLabel("100%"));
        labels.put(2, new JLabel("120%"));
        labels.put(3, new JLabel("150%"));
        scaleSlider.setLabelTable(labels);
        
        scaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!scaleSlider.getValueIsAdjusting()) {
                    updateScaleCombobox(scaleSlider.getValue());
                }
            }
        });
        
        JLabel sliderHeader = new JLabel("Перетягніть слайдер для налаштування масштабу:");
        sliderPanel.add(sliderHeader, BorderLayout.NORTH);
        sliderPanel.add(scaleSlider, BorderLayout.CENTER);
        
        // Додаємо компоненти до панелі масштабу
        scalePanel.add(comboPanel);
        scalePanel.add(sliderPanel);
        
        // Додаємо компоненти до головної панелі
        panel.add(scalePanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Оновлює виділення превью теми
     */
    private void updateThemePreviewSelection(boolean isLightSelected) {
        if (isLightSelected) {
            lightThemePreview.setBorder(BorderFactory.createLineBorder(new Color(65, 105, 225), 2));
            darkThemePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        } else {
            lightThemePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            darkThemePreview.setBorder(BorderFactory.createLineBorder(new Color(65, 105, 225), 2));
        }
    }
    
    /**
     * Оновлює положення слайдера масштабу
     */
    private void updateScaleSlider(UiScale scale) {
        switch (scale) {
            case SMALL:
                scaleSlider.setValue(0);
                break;
            case MEDIUM:
                scaleSlider.setValue(1);
                break;
            case LARGE:
                scaleSlider.setValue(2);
                break;
            case EXTRA_LARGE:
                scaleSlider.setValue(3);
                break;
        }
    }
    
    /**
     * Оновлює вибраний пункт у випадаючому списку масштабу
     */
    private void updateScaleCombobox(int sliderValue) {
        UiScale scale;
        switch (sliderValue) {
            case 0:
                scale = UiScale.SMALL;
                break;
            case 2:
                scale = UiScale.LARGE;
                break;
            case 3:
                scale = UiScale.EXTRA_LARGE;
                break;
            default:
                scale = UiScale.MEDIUM;
        }
        scaleComboBox.setSelectedItem(scale);
    }
    
    /**
     * Завантаження поточних налаштувань
     */
    private void loadCurrentSettings() {
        // Встановлюємо поточну тему
        Theme currentTheme = themeManager.getCurrentTheme();
        switch (currentTheme) {
            case LIGHT:
                lightThemeRadio.setSelected(true);
                updateThemePreviewSelection(true);
                break;
            case DARK:
                darkThemeRadio.setSelected(true);
                updateThemePreviewSelection(false);
                break;
            case SYSTEM:
                systemThemeRadio.setSelected(true);
                updateThemePreviewSelection(true);
                break;
        }
        
        // Встановлюємо поточний масштаб
        UiScale currentScale = themeManager.getCurrentScale();
        scaleComboBox.setSelectedItem(currentScale);
        updateScaleSlider(currentScale);
    }
    
    /**
     * Застосування налаштувань
     */
    private void applySettings() {
        log.info("Застосування налаштувань інтерфейсу");
        
        // Визначаємо вибрану тему
        Theme selectedTheme;
        if (lightThemeRadio.isSelected()) {
            selectedTheme = Theme.LIGHT;
        } else if (darkThemeRadio.isSelected()) {
            selectedTheme = Theme.DARK;
        } else {
            selectedTheme = Theme.SYSTEM;
        }
        
        // Визначаємо вибраний масштаб
        UiScale selectedScale = (UiScale) scaleComboBox.getSelectedItem();
        
        // Зберігаємо поточні налаштування
        Theme previousTheme = themeManager.getCurrentTheme();
        UiScale previousScale = themeManager.getCurrentScale();
        
        // Перевіряємо, чи змінилися налаштування
        boolean themeChanged = previousTheme != selectedTheme;
        boolean scaleChanged = previousScale != selectedScale;
        
        if (!themeChanged && !scaleChanged) {
            // Якщо налаштування не змінилися, просто закриваємо діалог
            log.info("Налаштування не змінилися, закриваємо діалог");
            dispose();
            return;
        }
        
        // Встановлюємо нові налаштування
        themeManager.setTheme(selectedTheme);
        themeManager.setScale(selectedScale);
        
        // Оновлюємо інтерфейс
        themeManager.applyCurrentTheme();
        
        // Примусово оновлюємо компоненти
        JFrame mainFrame = (JFrame) getOwner();
        SwingUtilities.updateComponentTreeUI(mainFrame);
        SwingUtilities.updateComponentTreeUI(this);
        
        // Перерисовуємо компоненти
        if (mainFrame != null) {
            mainFrame.invalidate();
            mainFrame.validate();
            mainFrame.repaint();
        }
        
        // Оновлюємо діалог
        invalidate();
        validate();
        repaint();
        
        // Інформуємо користувача про успішне застосування налаштувань
        SwingHelper.showInfoDialog(
            this,
            messages.getString("app.info"),
            messages.getString("settings.changes_applied")
        );
    }
} 