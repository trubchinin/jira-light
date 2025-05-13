package ua.oip.jiralite.ui.util;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Допоміжний клас для створення компонентів Swing
 * та виконання типових UI-операцій
 */
public class SwingHelper {
    
    /**
     * Створює стандартну панель з BoxLayout
     */
    public static JPanel createPanel(int axis) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, axis));
        return panel;
    }
    
    /**
     * Створює кнопку з текстом і іконкою
     */
    public static JButton createButton(String text, String iconName) {
        JButton button = new JButton(text);
        
        if (iconName != null && !iconName.isEmpty()) {
            String iconPath = "/icons/" + iconName + ".png";
            ImageIcon icon = new ImageIcon(SwingHelper.class.getResource(iconPath));
            button.setIcon(icon);
        }
        
        return button;
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
    
    /**
     * Створює панель з рамкою і заголовком
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        Border border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
        
        panel.setBorder(border);
        return panel;
    }
    
    /**
     * Відображає діалог з помилкою
     */
    public static void showErrorDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Відображає інформаційний діалог
     */
    public static void showInfoDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Запитує підтвердження у користувача
     */
    public static boolean showConfirmDialog(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(
            parent, message, title, 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE
        );
        
        return result == JOptionPane.YES_OPTION;
    }
} 