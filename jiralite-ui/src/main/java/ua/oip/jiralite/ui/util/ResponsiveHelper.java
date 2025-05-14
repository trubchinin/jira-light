package ua.oip.jiralite.ui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилітарний клас для забезпечення адаптивності інтерфейсу
 */
public class ResponsiveHelper {
    
    private static final Logger log = LoggerFactory.getLogger(ResponsiveHelper.class);
    
    // Розмежування для різних розмірів екрану
    public static final int SMALL_SCREEN_WIDTH = 1024;
    public static final int MEDIUM_SCREEN_WIDTH = 1366;
    public static final int LARGE_SCREEN_WIDTH = 1920;
    
    // Розмежування для різних густин пікселів (DPI)
    public static final double LOW_DPI = 1.0;
    public static final double MEDIUM_DPI = 1.25;
    public static final double HIGH_DPI = 1.5;
    public static final double VERY_HIGH_DPI = 2.0;
    
    // Конструктор
    private ResponsiveHelper() {
        // Приватний конструктор для запобігання створення екземплярів
    }
    
    /**
     * Визначає оптимальний розмір вікна на основі розміру екрану
     * 
     * @param frame вікно для налаштування
     */
    public static void setupFrameSize(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        
        // Вираховуємо доступну площу екрана з урахуванням системних панелей
        int availableWidth = screenWidth - screenInsets.left - screenInsets.right;
        int availableHeight = screenHeight - screenInsets.top - screenInsets.bottom;
        
        // Вибір розміру вікна в залежності від розміру екрану
        Dimension frameSize;
        
        if (screenWidth >= LARGE_SCREEN_WIDTH) {
            // Великий екран (Full HD та вище)
            // Використовуємо 80% доступної ширини та 85% доступної висоти
            frameSize = new Dimension(
                    (int)(availableWidth * 0.8), 
                    (int)(availableHeight * 0.85));
        } else if (screenWidth >= MEDIUM_SCREEN_WIDTH) {
            // Середній екран (HD)
            // Використовуємо 85% доступної ширини та 90% доступної висоти
            frameSize = new Dimension(
                    (int)(availableWidth * 0.85), 
                    (int)(availableHeight * 0.9));
        } else {
            // Малий екран
            // Використовуємо 90% доступної ширини та 90% доступної висоти
            frameSize = new Dimension(
                    (int)(availableWidth * 0.9), 
                    (int)(availableHeight * 0.9));
        }
        
        // Центруємо вікно на екрані
        int x = screenInsets.left + (availableWidth - frameSize.width) / 2;
        int y = screenInsets.top + (availableHeight - frameSize.height) / 2;
        
        // Встановлюємо розмір та позицію вікна
        frame.setBounds(x, y, frameSize.width, frameSize.height);
        
        // Встановлюємо мінімальний розмір вікна
        frame.setMinimumSize(new Dimension(800, 600));
        
        log.debug("Адаптивний розмір: {}x{} для екрану {}x{}", 
                frame.getWidth(), frame.getHeight(), availableWidth, availableHeight);
    }
    
    /**
     * Розраховує оптимальну ширину колонки на основі розміру екрану
     * 
     * @param containerWidth ширина контейнера
     * @param columnCount кількість колонок
     * @return оптимальна ширина колонки
     */
    public static int calculateColumnWidth(int containerWidth, int columnCount) {
        ThemeManager themeManager = ThemeManager.getInstance();
        float scale = themeManager.getCurrentScale().getFactor();
        
        // Базова ширина колонки на основі категорії розміру екрану
        int baseWidth;
        String screenCategory = getScreenSizeCategory();
        
        switch (screenCategory) {
            case "Small":
                baseWidth = 220;
                break;
            case "Medium":
                baseWidth = 250;
                break;
            case "Large":
                baseWidth = 280;
                break;
            default:
                baseWidth = 250;
        }
        
        // Масштабуємо ширину відповідно до налаштувань користувача
        int scaledWidth = (int)(baseWidth * scale);
        
        // Розраховуємо доступний простір для колонок з урахуванням відступів
        int spacing = (int)(UiConstants.COMPONENT_SPACING * scale);
        int availableWidth = containerWidth - (spacing * (columnCount + 1));
        
        // Розраховуємо ширину колонки
        int columnWidth = Math.max(scaledWidth, availableWidth / columnCount);
        
        // Обмежуємо максимальну ширину
        int maxColumnWidth = (int)(400 * scale);
        columnWidth = Math.min(columnWidth, maxColumnWidth);
        
        log.debug("Розрахована ширина колонки: {} для контейнера шириною {} з {} колонками", 
                columnWidth, containerWidth, columnCount);
        
        return columnWidth;
    }
    
    /**
     * Налаштовує адаптивні розміри компонента
     * 
     * @param component компонент для налаштування
     * @param preferredWidth бажана ширина
     * @param preferredHeight бажана висота
     */
    public static void setupResponsiveSize(JComponent component, int preferredWidth, int preferredHeight) {
        ThemeManager themeManager = ThemeManager.getInstance();
        float scale = themeManager.getCurrentScale().getFactor();
        
        // Враховуємо масштаб користувача
        int scaledWidth = (int)(preferredWidth * scale);
        int scaledHeight = (int)(preferredHeight * scale);
        
        // Встановлюємо розміри компонента
        Dimension size = new Dimension(scaledWidth, scaledHeight);
        component.setPreferredSize(size);
        component.setMinimumSize(size);
    }
    
    /**
     * Створює адаптивний відступ у панелі
     * 
     * @param panel панель, для якої створюється відступ
     * @param top верхній відступ
     * @param left лівий відступ
     * @param bottom нижній відступ
     * @param right правий відступ
     */
    public static void setupResponsivePadding(JPanel panel, int top, int left, int bottom, int right) {
        ThemeManager themeManager = ThemeManager.getInstance();
        float scale = themeManager.getCurrentScale().getFactor();
        
        // Масштабуємо відступи
        int scaledTop = (int)(top * scale);
        int scaledLeft = (int)(left * scale);
        int scaledBottom = (int)(bottom * scale);
        int scaledRight = (int)(right * scale);
        
        // Встановлюємо відступи панелі
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                scaledTop, scaledLeft, scaledBottom, scaledRight));
    }
    
    /**
     * Повертає категорію розміру екрану
     * 
     * @return строка з категорією розміру екрану
     */
    public static String getScreenSizeCategory() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        
        if (screenWidth >= LARGE_SCREEN_WIDTH) {
            return "Large";
        } else if (screenWidth >= MEDIUM_SCREEN_WIDTH) {
            return "Medium";
        } else {
            return "Small";
        }
    }
    
    /**
     * Отримує коефіцієнт масштабування для поточного екрану
     * @return коефіцієнт масштабування (1.0 для звичайних екранів)
     */
    public static double getScreenScaleFactor() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        
        // Отримуємо трансформацію екрану для визначення DPI
        java.awt.geom.AffineTransform tx = gd.getDefaultConfiguration().getDefaultTransform();
        double scaleX = tx.getScaleX();
        double scaleY = tx.getScaleY();
        
        // Використовуємо середнє значення масштабування для осей X та Y
        return (scaleX + scaleY) / 2.0;
    }
    
    /**
     * Адаптивно встановлює шрифт компонента в залежності від розміру екрану та налаштувань
     * @param component компонент для налаштування шрифту
     * @param baseFontSize базовий розмір шрифту
     * @param isBold чи потрібно використовувати жирний шрифт
     */
    public static void setupResponsiveFont(Component component, float baseFontSize, boolean isBold) {
        ThemeManager themeManager = ThemeManager.getInstance();
        float userScale = themeManager.getCurrentScale().getFactor();
        
        // Отримуємо поточний шрифт компонента
        java.awt.Font currentFont = component.getFont();
        
        // Визначаємо новий розмір шрифту з урахуванням налаштувань користувача
        float newSize = baseFontSize * userScale;
        
        // Встановлюємо новий шрифт
        int fontStyle = isBold ? java.awt.Font.BOLD : java.awt.Font.PLAIN;
        java.awt.Font newFont = currentFont.deriveFont(fontStyle, newSize);
        component.setFont(newFont);
    }
    
    /**
     * Адаптивно оновлює компонентне дерево при зміні масштабу
     * @param rootComponent кореневий компонент для оновлення
     */
    public static void updateComponentTreeForScale(Component rootComponent) {
        // Оновлюємо UI-дерево
        SwingUtilities.updateComponentTreeUI(rootComponent);
        
        // Примушуємо компонент перемалюватися
        rootComponent.invalidate();
        rootComponent.validate();
        rootComponent.repaint();
        
        // Якщо це вікно, оновлюємо його розміри
        if (rootComponent instanceof JFrame) {
            JFrame frame = (JFrame) rootComponent;
            
            // Збережемо поточне розташування вікна
            int x = frame.getX();
            int y = frame.getY();
            
            // Налаштовуємо адаптивний розмір вікна
            setupFrameSize(frame);
            
            // Якщо вікно було повноекранним, зберігаємо цей стан
            if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                // Інакше відновлюємо його положення
                frame.setLocation(x, y);
            }
        }
    }
} 