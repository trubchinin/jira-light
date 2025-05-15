package ua.oip.jiralite.ui.frame;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import ua.oip.jiralite.ui.util.SwingHelper;

/**
 * Діалог "Про програму"
 */
public class AboutDialog extends JDialog {
    
    private final ResourceBundle messages;
    
    /**
     * Конструктор діалогу
     * 
     * @param parent батьківське вікно
     * @param messages ресурси локалізації
     */
    public AboutDialog(JFrame parent, ResourceBundle messages) {
        super(parent, true);
        this.messages = messages;
        
        initializeUI();
    }
    
    /**
     * Ініціалізація інтерфейсу
     */
    private void initializeUI() {
        setTitle(messages.getString("about.title"));
        setSize(500, 300);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Заголовок
        JLabel titleLabel = new JLabel("Jira Lite", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Опис
        JTextArea descriptionArea = new JTextArea(messages.getString("about.description"));
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setOpaque(false);
        descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(descriptionArea, BorderLayout.CENTER);
        
        // Кнопка закриття
        JButton closeButton = SwingHelper.createButton(
                messages.getString("app.close"),
                e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
} 