package ua.oip.jiralite.ui.util;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Допоміжний клас для роботи зі Swing компонентами
 */
public final class SwingHelper {
    
    private SwingHelper() {
        // Utility class, не дозволяємо створювати екземпляри
    }
    
    /**
     * Показує повідомлення про помилку
     */
    public static void showError(Component parent, String message) {
        // Отримання ресурсів локалізації
        ResourceBundle messages = ResourceBundle.getBundle("i18n.labels", new Locale("uk", "UA"));
        
        JOptionPane.showMessageDialog(
                parent,
                message,
                messages.getString("app.error"),
                JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Показує діалог помилки
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
     * Показує інформаційне повідомлення
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
     * Показує діалог підтвердження
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
     * Створює кнопку з текстом та обробником
     */
    public static JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        if (listener != null) {
            button.addActionListener(listener);
        }
        // Застосовуємо стиль до кнопки
        applyButtonStyle(button);
        return button;
    }
    
    /**
     * Застосовує єдиний стиль до кнопки (синій фон з білим текстом)
     * 
     * @param button кнопка для стилізації
     */
    public static void applyButtonStyle(JButton button) {
        if (button == null) return;
        
        // Отримуємо менеджер тем для використання акцентного кольору
        ThemeManager themeManager = ThemeManager.getInstance();
        
        // Встановлюємо стиль кнопки
        button.setBackground(themeManager.getCurrentScheme().accentColor);
        button.setForeground(java.awt.Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        // Додаємо слухач зміни теми для оновлення кольору
        themeManager.addThemeChangeListener(() -> {
            button.setBackground(themeManager.getCurrentScheme().accentColor);
            button.setForeground(java.awt.Color.WHITE);
        });
    }
    
    /**
     * Створює панель з заголовком
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