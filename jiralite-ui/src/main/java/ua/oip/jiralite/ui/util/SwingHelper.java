package ua.oip.jiralite.ui.util;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Вспомогательный класс для работы со Swing компонентами
 */
public final class SwingHelper {
    
    private SwingHelper() {
        // Utility class, не позволяем создавать экземпляры
    }
    
    /**
     * Показывает сообщение об ошибке
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Помилка",
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Показывает диалог ошибки
     */
    public static void showErrorDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Показывает информационное сообщение
     */
    public static void showInfoDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Показывает диалог подтверждения
     */
    public static boolean showConfirmDialog(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Создает кнопку с текстом и обработчиком
     */
    public static JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        if (listener != null) {
            button.addActionListener(listener);
        }
        return button;
    }
    
    /**
     * Создает панель с заголовком
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }
    
    /**
     * Створює стандартну панель з BoxLayout
     */
    public static JPanel createPanel(int axis) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, axis));
        return panel;
    }
    
    /**
     * Створює заголовок форми
     */
    public static JLabel createHeader(String text) {
        JLabel header = new JLabel(text);
        Font font = header.getFont().deriveFont(Font.BOLD, header.getFont().getSize() + 4);
        header.setFont(font);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return header;
    }
    
    /**
     * Додає панель з полями відступу до компонента
     */
    public static JComponent withPadding(JComponent component, int top, int left, int bottom, int right) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(component);
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return panel;
    }
    
    /**
     * Додає вертикальний відступ
     */
    public static Component createVerticalSpacer(int height) {
        return Box.createVerticalStrut(height);
    }
    
    /**
     * Додає горизонтальний відступ
     */
    public static Component createHorizontalSpacer(int width) {
        return Box.createHorizontalStrut(width);
    }
} 