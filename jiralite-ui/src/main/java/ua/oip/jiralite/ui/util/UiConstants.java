package ua.oip.jiralite.ui.util;

import java.awt.Color;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import ua.oip.jiralite.domain.enums.Status;

/**
 * Константы для пользовательского интерфейса
 */
public final class UiConstants {
    
    // Предотвращаем создание экземпляров
    private UiConstants() {}
    
    // Цвета
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 250);
    public static final Color PANEL_BACKGROUND = new Color(255, 255, 255);
    public static final Color TEXT_PRIMARY = new Color(50, 50, 50);
    public static final Color TEXT_SECONDARY = new Color(120, 120, 120);
    
    // Цвета для статусов и уведомлений
    public static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    public static final Color INFO_COLOR = new Color(41, 128, 185);
    public static final Color WARNING_COLOR = new Color(243, 156, 18);
    public static final Color DANGER_COLOR = new Color(231, 76, 60);
    
    // Цвета колонок Kanban доски
    public static final Color TODO_COLUMN_COLOR = new Color(245, 245, 245);
    public static final Color IN_PROGRESS_COLUMN_COLOR = new Color(240, 248, 255);
    public static final Color DONE_COLUMN_COLOR = new Color(240, 255, 240);
    
    // Шрифты
    public static final Font HEADER_FONT = new Font("Dialog", Font.BOLD, 18);
    public static final Font SUBHEADER_FONT = new Font("Dialog", Font.BOLD, 16);
    public static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);
    public static final Font SMALL_FONT = new Font("Dialog", Font.PLAIN, 11);
    
    // Отступы и размеры
    public static final int PANEL_PADDING = 10;
    public static final int COMPONENT_SPACING = 5;
    
    // Размеры компонентов
    public static final int ISSUE_DIALOG_WIDTH = 600;
    public static final int ISSUE_DIALOG_HEIGHT = 500;
    public static final int MAIN_FRAME_WIDTH = 1200;
    public static final int MAIN_FRAME_HEIGHT = 700;
    
    // Иконки
    public static final String ICON_PROJECT = "project";
    public static final String ICON_BOARD = "board";
    public static final String ICON_ISSUE = "issue";
    public static final String ICON_USER = "user";
    
    // Границы
    public static final Border DEFAULT_BORDER = 
            BorderFactory.createEmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING);
    
    /**
     * Возвращает локализованное название статуса для колонки
     */
    public static String statusCaption(Status status) {
        if (status == null) {
            return "Невідомий";
        }
        
        ResourceBundle messages = ResourceBundle.getBundle("i18n.labels");
        switch (status) {
            case TO_DO:
                return messages.getString("status.todo");
            case IN_PROGRESS:
                return messages.getString("status.in_progress");
            case DONE:
                return messages.getString("status.done");
            default:
                return "Невідомий";
        }
    }
    
    /**
     * Возвращает цвет фона для колонки со статусом
     */
    public static Color columnColor(Status status) {
        if (status == null) {
            return BACKGROUND_COLOR;
        }
        
        switch (status) {
            case TO_DO:
                return TODO_COLUMN_COLOR;
            case IN_PROGRESS:
                return IN_PROGRESS_COLUMN_COLOR;
            case DONE:
                return DONE_COLUMN_COLOR;
            default:
                return BACKGROUND_COLOR;
        }
    }
} 