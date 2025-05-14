package ua.oip.jiralite.ui.util;

import java.awt.Color;
import java.awt.Font;
import java.util.prefs.Preferences;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Менеджер тем для додатку Jira Lite
 * Відповідає за налаштування зовнішнього вигляду UI
 */
public class ThemeManager {
    
    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);
    
    // Синглтон екземпляр
    private static ThemeManager instance;
    
    // Интерфейс для слушателей изменения темы
    public interface ThemeChangeListener {
        void onThemeChanged();
    }
    
    // Список слушателей изменения темы
    private final java.util.List<ThemeChangeListener> themeChangeListeners = new java.util.ArrayList<>();
    
    // Константи теми
    public enum Theme {
        LIGHT, DARK, SYSTEM
    }
    
    // Константи для масштабування
    public enum UiScale {
        SMALL(0.8f), MEDIUM(1.0f), LARGE(1.2f), EXTRA_LARGE(1.5f);
        
        private final float factor;
        
        UiScale(float factor) {
            this.factor = factor;
        }
        
        public float getFactor() {
            return factor;
        }
        
        @Override
        public String toString() {
            switch (this) {
                case SMALL:
                    return "Малий (80%)";
                case MEDIUM:
                    return "Середній (100%)";
                case LARGE:
                    return "Великий (120%)";
                case EXTRA_LARGE:
                    return "Дуже великий (150%)";
                default:
                    return "Середній (100%)";
            }
        }
    }
    
    // Поточні налаштування
    private Theme currentTheme = Theme.LIGHT;
    private UiScale currentScale = UiScale.MEDIUM;
    
    // Кольорові схеми
    public static final ColorScheme LIGHT_SCHEME = new ColorScheme(
            new Color(245, 247, 250),         // background
            new Color(255, 255, 255),         // panelBackground
            new Color(50, 50, 60),            // textPrimary
            new Color(120, 120, 130),         // textSecondary
            new Color(39, 174, 96),           // success
            new Color(41, 128, 185),          // info 
            new Color(243, 156, 18),          // warning
            new Color(231, 76, 60),           // danger
            new Color(245, 245, 245),         // todoColumn
            new Color(240, 248, 255),         // inProgressColumn
            new Color(240, 255, 240),         // doneColumn
            new Color(220, 220, 225),         // border
            new Color(240, 240, 245),         // cardBackground
            new Color(250, 250, 255),         // cardHeaderBackground
            new Color(245, 245, 250),         // cardFooterBackground
            new Color(65, 105, 225),          // accentColor
            new Color(0, 0, 0, 10)            // shadowColor
    );
    
    public static final ColorScheme DARK_SCHEME = new ColorScheme(
            new Color(32, 33, 36),            // background
            new Color(43, 44, 49),            // panelBackground
            new Color(220, 220, 225),         // textPrimary
            new Color(180, 180, 185),         // textSecondary
            new Color(46, 204, 113),          // success
            new Color(52, 152, 219),          // info
            new Color(241, 196, 15),          // warning
            new Color(231, 76, 60),           // danger
            new Color(35, 36, 40),            // todoColumn
            new Color(35, 40, 48),            // inProgressColumn
            new Color(35, 50, 35),            // doneColumn
            new Color(60, 63, 68),            // border
            new Color(45, 46, 51),            // cardBackground
            new Color(50, 52, 57),            // cardHeaderBackground
            new Color(48, 49, 54),            // cardFooterBackground
            new Color(100, 149, 237),         // accentColor
            new Color(0, 0, 0, 30)            // shadowColor
    );
    
    private ColorScheme currentScheme = LIGHT_SCHEME;
    
    // Конструктор
    private ThemeManager() {
        loadPreferences();
    }
    
    /**
     * Отримання екземпляру менеджера тем
     * @return екземпляр ThemeManager
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Застосування поточної теми до додатку
     */
    public void applyCurrentTheme() {
        log.info("Застосування теми: {}", currentTheme);
        
        // Вибір кольорової схеми в залежності від теми
        currentScheme = (currentTheme == Theme.DARK) ? DARK_SCHEME : LIGHT_SCHEME;
        
        // Масштабування шрифтів
        float scaleFactor = currentScale.getFactor();
        Font baseFont = UiConstants.DEFAULT_FONT.deriveFont(
                UiConstants.DEFAULT_FONT.getSize() * scaleFactor);
        Font headerFont = UiConstants.HEADER_FONT.deriveFont(
                UiConstants.HEADER_FONT.getSize() * scaleFactor);
        Font subheaderFont = UiConstants.SUBHEADER_FONT.deriveFont(
                UiConstants.SUBHEADER_FONT.getSize() * scaleFactor);
        Font smallFont = UiConstants.SMALL_FONT.deriveFont(
                UiConstants.SMALL_FONT.getSize() * scaleFactor);
        
        // Налаштовуємо UI Manager
        try {
            // Встановлюємо Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Встановлюємо кольори компонентів
            UIManager.put("Panel.background", new ColorUIResource(currentScheme.panelBackground));
            UIManager.put("Button.background", new ColorUIResource(currentScheme.panelBackground));
            UIManager.put("TextField.background", new ColorUIResource(currentScheme.background));
            UIManager.put("TextArea.background", new ColorUIResource(currentScheme.background));
            UIManager.put("ComboBox.background", new ColorUIResource(currentScheme.panelBackground));
            UIManager.put("Label.foreground", new ColorUIResource(currentScheme.textPrimary));
            UIManager.put("Button.foreground", new ColorUIResource(currentScheme.textPrimary));
            UIManager.put("TextField.foreground", new ColorUIResource(currentScheme.textPrimary));
            UIManager.put("TextArea.foreground", new ColorUIResource(currentScheme.textPrimary));
            UIManager.put("ComboBox.foreground", new ColorUIResource(currentScheme.textPrimary));
            UIManager.put("TabbedPane.background", new ColorUIResource(currentScheme.background));
            UIManager.put("TabbedPane.foreground", new ColorUIResource(currentScheme.textPrimary));
            UIManager.put("TabbedPane.selected", new ColorUIResource(currentScheme.panelBackground));
            UIManager.put("TabbedPane.contentAreaColor", new ColorUIResource(currentScheme.panelBackground));
            UIManager.put("ScrollPane.background", new ColorUIResource(currentScheme.background));
            UIManager.put("MenuBar.background", new ColorUIResource(currentScheme.panelBackground));
            UIManager.put("Menu.background", new ColorUIResource(currentScheme.panelBackground));
            UIManager.put("MenuItem.background", new ColorUIResource(currentScheme.panelBackground));
            UIManager.put("Menu.foreground", new ColorUIResource(currentScheme.textPrimary));
            UIManager.put("MenuItem.foreground", new ColorUIResource(currentScheme.textPrimary));
            
            // Встановлюємо шрифти
            UIManager.put("Label.font", new FontUIResource(baseFont));
            UIManager.put("Button.font", new FontUIResource(baseFont));
            UIManager.put("TextField.font", new FontUIResource(baseFont));
            UIManager.put("TextArea.font", new FontUIResource(baseFont));
            UIManager.put("ComboBox.font", new FontUIResource(baseFont));
            UIManager.put("MenuBar.font", new FontUIResource(baseFont));
            UIManager.put("MenuItem.font", new FontUIResource(baseFont));
            UIManager.put("Menu.font", new FontUIResource(baseFont));
            UIManager.put("TabbedPane.font", new FontUIResource(baseFont));
            
            // Уведомляем всех слушателей об изменении темы
            notifyThemeChangeListeners();
            
            // Обновляем все активные окна приложения
            for (java.awt.Frame frame : java.awt.Frame.getFrames()) {
                if (frame.isVisible() && frame instanceof javax.swing.JFrame) {
                    // Обновляем UI для каждого окна
                    javax.swing.SwingUtilities.updateComponentTreeUI(frame);
                    
                    // Обновляем все вложенные диалоги
                    for (java.awt.Window window : frame.getOwnedWindows()) {
                        if (window.isVisible() && window instanceof javax.swing.JDialog) {
                            javax.swing.SwingUtilities.updateComponentTreeUI(window);
                        }
                    }
                    
                    // Принудительно вызываем перекомпоновку и перерисовку окна
                    frame.invalidate();
                    frame.validate();
                    frame.repaint();
                }
            }
            
            log.info("Тему успішно застосовано");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            log.error("Помилка при застосуванні теми", e);
        }
        
        // Зберігаємо налаштування
        savePreferences();
    }
    
    /**
     * Добавляет слушателя изменения темы
     * @param listener слушатель изменения темы
     */
    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (listener != null && !themeChangeListeners.contains(listener)) {
            themeChangeListeners.add(listener);
        }
    }
    
    /**
     * Удаляет слушателя изменения темы
     * @param listener слушатель изменения темы
     */
    public void removeThemeChangeListener(ThemeChangeListener listener) {
        themeChangeListeners.remove(listener);
    }
    
    /**
     * Уведомляет всех слушателей об изменении темы
     */
    private void notifyThemeChangeListeners() {
        for (ThemeChangeListener listener : themeChangeListeners) {
            listener.onThemeChanged();
        }
    }
    
    /**
     * Зміна поточної теми
     * @param theme нова тема
     */
    public void setTheme(Theme theme) {
        if (theme != currentTheme) {
            log.info("Змінюємо тему з {} на {}", currentTheme, theme);
            this.currentTheme = theme;
            applyCurrentTheme();
        }
    }
    
    /**
     * Перемикання між світлою та темною темою
     */
    public void toggleTheme() {
        Theme newTheme = (currentTheme == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        setTheme(newTheme);
    }
    
    /**
     * Отримання назви поточної теми для відображення
     * @return назва поточної теми
     */
    public String getCurrentThemeName() {
        return currentTheme == Theme.DARK ? "Темна" : "Світла";
    }
    
    /**
     * Встановлення масштабу інтерфейсу
     * @param scale новий масштаб
     */
    public void setScale(UiScale scale) {
        if (scale != currentScale) {
            log.info("Змінюємо масштаб з {} на {}", currentScale, scale);
            this.currentScale = scale;
            applyCurrentTheme();
        }
    }
    
    /**
     * Отримання поточної кольорової схеми
     * @return поточна кольорова схема
     */
    public ColorScheme getCurrentScheme() {
        return currentScheme;
    }
    
    /**
     * Отримання поточної теми
     * @return поточна тема
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Отримання поточного масштабу
     * @return поточний масштаб
     */
    public UiScale getCurrentScale() {
        return currentScale;
    }
    
    /**
     * Завантаження налаштувань користувача
     */
    private void loadPreferences() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
            String themeStr = prefs.get("theme", Theme.LIGHT.name());
            String scaleStr = prefs.get("scale", UiScale.MEDIUM.name());
            
            try {
                currentTheme = Theme.valueOf(themeStr);
            } catch (IllegalArgumentException e) {
                currentTheme = Theme.LIGHT;
            }
            
            try {
                currentScale = UiScale.valueOf(scaleStr);
            } catch (IllegalArgumentException e) {
                currentScale = UiScale.MEDIUM;
            }
            
            log.info("Завантажено налаштування: тема={}, масштаб={}", currentTheme, currentScale);
        } catch (Exception e) {
            log.error("Помилка при завантаженні налаштувань", e);
        }
    }
    
    /**
     * Збереження налаштувань користувача
     */
    private void savePreferences() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
            prefs.put("theme", currentTheme.name());
            prefs.put("scale", currentScale.name());
            prefs.flush();
            log.info("Збережено налаштування: тема={}, масштаб={}", currentTheme, currentScale);
        } catch (Exception e) {
            log.error("Помилка при збереженні налаштувань", e);
        }
    }
    
    /**
     * Клас, що містить кольорову схему
     */
    public static class ColorScheme {
        public final Color background;
        public final Color panelBackground;
        public final Color textPrimary;
        public final Color textSecondary;
        public final Color success;
        public final Color info;
        public final Color warning;
        public final Color danger;
        public final Color todoColumn;
        public final Color inProgressColumn;
        public final Color doneColumn;
        public final Color border;
        public final Color cardBackground;
        public final Color cardHeaderBackground;
        public final Color cardFooterBackground;
        public final Color accentColor;
        public final Color shadowColor;
        
        public ColorScheme(
                Color background, 
                Color panelBackground, 
                Color textPrimary, 
                Color textSecondary, 
                Color success, 
                Color info, 
                Color warning, 
                Color danger, 
                Color todoColumn, 
                Color inProgressColumn, 
                Color doneColumn,
                Color border,
                Color cardBackground,
                Color cardHeaderBackground,
                Color cardFooterBackground,
                Color accentColor,
                Color shadowColor) {
            this.background = background;
            this.panelBackground = panelBackground;
            this.textPrimary = textPrimary;
            this.textSecondary = textSecondary;
            this.success = success;
            this.info = info;
            this.warning = warning;
            this.danger = danger;
            this.todoColumn = todoColumn;
            this.inProgressColumn = inProgressColumn;
            this.doneColumn = doneColumn;
            this.border = border;
            this.cardBackground = cardBackground;
            this.cardHeaderBackground = cardHeaderBackground;
            this.cardFooterBackground = cardFooterBackground;
            this.accentColor = accentColor;
            this.shadowColor = shadowColor;
        }
    }
} 