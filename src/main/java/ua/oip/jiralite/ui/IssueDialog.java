package ua.oip.jiralite.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.domain.user.User;
import ua.oip.jiralite.service.BoardService;

public class IssueDialog extends JDialog {
    
    private final BoardService boardService;
    private final Project project;
    private Issue issue;
    private boolean isNewIssue;
    
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Status> statusComboBox;
    private JComboBox<Priority> priorityComboBox;
    private JComboBox<User> assigneeComboBox;
    
    private boolean confirmed = false;
    
    public IssueDialog(JFrame parent, String title, BoardService boardService, Project project) {
        this(parent, title, boardService, project, null);
    }
    
    public IssueDialog(JFrame parent, String title, BoardService boardService, Project project, Issue issue) {
        super(parent, title, true);
        this.boardService = boardService;
        this.project = project;
        this.issue = issue;
        this.isNewIssue = (issue == null);
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(true);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Панель с полями ввода
        JPanel fieldsPanel = createFieldsPanel();
        mainPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        // Панель с кнопками
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Заполняем поля данными, если редактируем существующую задачу
        if (!isNewIssue) {
            populateFields();
        }
    }
    
    private JPanel createFieldsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Заголовок
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        titleField = new JTextField(30);
        panel.add(titleField, gbc);
        
        // Описание
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        descriptionArea = new JTextArea(10, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        panel.add(scrollPane, gbc);
        
        // Статус
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        statusComboBox = new JComboBox<>(Status.values());
        panel.add(statusComboBox, gbc);
        
        // Приоритет
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Priority:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        priorityComboBox = new JComboBox<>(Priority.values());
        panel.add(priorityComboBox, gbc);
        
        // Исполнитель
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Assignee:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        assigneeComboBox = new JComboBox<>();
        loadAssignees();
        panel.add(assigneeComboBox, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton(isNewIssue ? "Create" : "Save");
        JButton cancelButton = new JButton("Cancel");
        
        panel.add(saveButton);
        panel.add(cancelButton);
        
        // Обработчики событий
        saveButton.addActionListener(e -> {
            if (validateForm()) {
                saveIssue();
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        return panel;
    }
    
    private void loadAssignees() {
        // Добавляем опцию "Не назначено"
        assigneeComboBox.addItem(null);
        
        // Добавляем всех участников проекта
        for (User user : project.getMembers()) {
            assigneeComboBox.addItem(user);
        }
    }
    
    private void populateFields() {
        titleField.setText(issue.getTitle());
        descriptionArea.setText(issue.getDescription());
        statusComboBox.setSelectedItem(issue.getStatus());
        priorityComboBox.setSelectedItem(issue.getPriority());
        
        if (issue.getAssignee() != null) {
            // Ищем исполнителя в списке и выбираем его
            for (int i = 0; i < assigneeComboBox.getItemCount(); i++) {
                User user = assigneeComboBox.getItemAt(i);
                if (user != null && user.getId().equals(issue.getAssignee().getId())) {
                    assigneeComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    private boolean validateForm() {
        String title = titleField.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Title must not be empty",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void saveIssue() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        Status status = (Status) statusComboBox.getSelectedItem();
        Priority priority = (Priority) priorityComboBox.getSelectedItem();
        User assignee = (User) assigneeComboBox.getSelectedItem();
        
        if (isNewIssue) {
            // Создаем новую задачу
            issue = boardService.createIssue(project, title, description, assignee);
            if (status != null) {
                boardService.updateIssueStatus(issue, status);
            }
        } else {
            // Обновляем существующую задачу
            issue.setTitle(title);
            issue.setDescription(description);
            issue.setPriority(priority);
            issue.setAssignee(assignee);
            
            if (status != issue.getStatus()) {
                boardService.updateIssueStatus(issue, status);
            }
        }
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public Issue getIssue() {
        return issue;
    }
} 