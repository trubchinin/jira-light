package ua.oip.jiralite.ui.panel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.util.SwingHelper;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Панель відображення деревовидної структури проєктів та дошок.
 * Ліва частина JSplitPane в головному вікні.
 */
public class ProjectTreePanel extends JPanel {
    
    private final User currentUser;
    private final BoardService boardService;
    private final ResourceBundle messages;
    
    private JTree projectTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    
    private ProjectSelectionListener selectionListener;
    
    /**
     * Інтерфейс для слухача вибору проекту або дошки
     */
    public interface ProjectSelectionListener {
        void onProjectSelected(Project project);
        void onBoardSelected(Board board);
    }
    
    /**
     * Конструктор панелі дерева проєктів
     * 
     * @param currentUser поточний користувач
     * @param boardService сервіс дошок
     * @param messages ресурси локалізації
     */
    public ProjectTreePanel(User currentUser, BoardService boardService, ResourceBundle messages) {
        this.currentUser = currentUser;
        this.boardService = boardService;
        this.messages = messages;
        
        initializeUI();
    }
    
    /**
     * Ініціалізація компонентів інтерфейсу
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(UiConstants.PANEL_PADDING, 
                UiConstants.PANEL_PADDING, UiConstants.PANEL_PADDING, UiConstants.PANEL_PADDING));
        
        // Заголовок панелі
        JLabel titleLabel = new JLabel(messages.getString("project.title"));
        titleLabel.setFont(UiConstants.HEADER_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, UiConstants.COMPONENT_SPACING, 0));
        
        // Створення кореневого вузла та моделі дерева
        rootNode = new DefaultMutableTreeNode("Projects");
        treeModel = new DefaultTreeModel(rootNode);
        
        // Створення дерева проєктів
        projectTree = new JTree(treeModel);
        projectTree.setRootVisible(false);
        projectTree.setShowsRootHandles(true);
        projectTree.setCellRenderer(new ProjectTreeCellRenderer());
        
        // Додавання прослуховувача подій вибору у дереві
        projectTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                handleTreeSelection();
            }
        });
        
        // Створення скрол-панелі для дерева
        JScrollPane scrollPane = new JScrollPane(projectTree);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Панель з кнопками для створення проєктів та дошок
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        JButton newProjectButton = SwingHelper.createButton(
                messages.getString("project.create"), 
                UiConstants.ICON_PROJECT);
        
        newProjectButton.addActionListener(e -> handleCreateProject());
        
        buttonPanel.add(newProjectButton);
        buttonPanel.add(Box.createHorizontalGlue());
        
        // Додавання всіх компонентів на панель
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Завантаження проєктів
        loadProjects();
    }
    
    /**
     * Завантаження проєктів користувача
     */
    public void loadProjects() {
        // Очистка дерева
        rootNode.removeAllChildren();
        
        // Отримання проєктів користувача та додавання їх у дерево
        List<Project> projects = boardService.getProjectsByUser(currentUser);
        
        for (Project project : projects) {
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
            rootNode.add(projectNode);
            
            // Додавання дошок проєкту
            List<Board> boards = boardService.getBoardsByProject(project);
            for (Board board : boards) {
                DefaultMutableTreeNode boardNode = new DefaultMutableTreeNode(board);
                projectNode.add(boardNode);
            }
        }
        
        // Оновлення моделі та розгортання всіх вузлів
        treeModel.reload();
        for (int i = 0; i < projectTree.getRowCount(); i++) {
            projectTree.expandRow(i);
        }
    }
    
    /**
     * Обробка вибору у дереві
     */
    private void handleTreeSelection() {
        if (selectionListener == null) {
            return;
        }
        
        TreePath path = projectTree.getSelectionPath();
        if (path == null) {
            return;
        }
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        
        if (userObject instanceof Project) {
            selectionListener.onProjectSelected((Project) userObject);
        } else if (userObject instanceof Board) {
            selectionListener.onBoardSelected((Board) userObject);
        }
    }
    
    /**
     * Обробка створення нового проєкту
     */
    private void handleCreateProject() {
        // Тут буде логіка створення нового проєкту
        // В реальній реалізації тут буде відкриття діалогу для введення даних проєкту
    }
    
    /**
     * Встановлення слухача вибору проєкту/дошки
     */
    public void setSelectionListener(ProjectSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    /**
     * Кастомний рендерер для дерева проєктів
     * Відображає іконки та форматування для різних типів вузлів
     */
    private class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {
        
        private final ImageIcon projectIcon;
        private final ImageIcon boardIcon;
        
        public ProjectTreeCellRenderer() {
            projectIcon = new ImageIcon(getClass().getResource("/icons/" + UiConstants.ICON_PROJECT + ".png"));
            boardIcon = new ImageIcon(getClass().getResource("/icons/" + UiConstants.ICON_ISSUE + ".png"));
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, 
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof Project) {
                Project project = (Project) userObject;
                setText(project.getName());
                setIcon(projectIcon);
                setToolTipText(project.getDescription());
            } else if (userObject instanceof Board) {
                Board board = (Board) userObject;
                setText(board.getName());
                setIcon(boardIcon);
            }
            
            return this;
        }
    }
} 