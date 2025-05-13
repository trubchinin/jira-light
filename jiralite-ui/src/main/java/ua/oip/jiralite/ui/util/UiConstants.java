package ua.oip.jiralite.ui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * Константи для інтерфейсу користувача.
 * Централізоване зберігання налаштувань зовнішнього вигляду.
 */
public class UiConstants {
    
    // Кольори
    public static final Color PRIMARY_COLOR = new Color(0, 82, 204);     // Синій
    public static final Color SECONDARY_COLOR = new Color(101, 84, 192);  // Фіолетовий
    public static final Color SUCCESS_COLOR = new Color(54, 179, 126);    // Зелений
    public static final Color WARNING_COLOR = new Color(255, 171, 0);     // Помаранчевий
    public static final Color DANGER_COLOR = new Color(222, 53, 11);      // Червоний
    public static final Color INFO_COLOR = new Color(80, 95, 121);        // Темно-сірий
    
    public static final Color BACKGROUND_COLOR = new Color(244, 245, 247); // Світло-сірий фон
    public static final Color PANEL_BACKGROUND = new Color(255, 255, 255); // Білий фон панелей
    
    public static final Color TEXT_PRIMARY = new Color(23, 43, 77);       // Темний для тексту
    public static final Color TEXT_SECONDARY = new Color(107, 119, 140);  // Сірий для підзаголовків
    
    // Відступи
    public static final int PANEL_PADDING = 16;
    public static final int COMPONENT_SPACING = 10;
    public static final int SECTION_SPACING = 20;
    
    // Розміри
    public static final Dimension BUTTON_SIZE = new Dimension(100, 30);
    public static final Dimension FIELD_SIZE = new Dimension(220, 30);
    public static final Dimension ICON_SIZE = new Dimension(16, 16);
    
    public static final int LOGIN_FRAME_WIDTH = 350;
    public static final int LOGIN_FRAME_HEIGHT = 280;
    
    public static final int MAIN_FRAME_WIDTH = 1024;
    public static final int MAIN_FRAME_HEIGHT = 768;
    
    public static final int ISSUE_DIALOG_WIDTH = 600;
    public static final int ISSUE_DIALOG_HEIGHT = 500;
    
    // Шрифти
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font SUBHEADER_FONT = new Font("SansSerif", Font.BOLD, 14);
    public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 11);
    
    // Рамки
    public static final Border DEFAULT_BORDER = BorderFactory.createEmptyBorder(
            PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING);
    
    public static final Border CARD_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(223, 225, 230), 1, true),
            BorderFactory.createEmptyBorder(8, 8, 8, 8));
    
    // Іконки
    public static final String ICON_USER = "user";
    public static final String ICON_PROJECT = "project";
    public static final String ICON_ISSUE = "issue";
} 