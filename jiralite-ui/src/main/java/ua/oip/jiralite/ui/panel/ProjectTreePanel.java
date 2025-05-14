package ua.oip.jiralite.ui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.util.UiConstants;

/**
 * Панель відображення деревовидної структури проєктів та дошок.
 * Ліва частина JSplitPane в головному вікні.
 */
public final class ProjectTreePanel extends JPanel {
    
    /**
     * Интерфейс для обработки событий выбора проекта и доски
     */
    public interface ProjectSelectionListener {
        void onProjectSelected(Project project);
        void onBoardSelected(Board board);
    }
    
    private final User currentUser;
    private final BoardService boardService;
    private final ResourceBundle messages;
    
    private ProjectSelectionListener selectionListener;
    private final JTree tree;
    
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
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Проекти"));
        
        // Ініціалізуємо дерево
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Проекти");
        tree = new JTree(new DefaultTreeModel(root));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        
        // Додаємо загальний обробник вибору вузла в дереві
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
                    tree.getLastSelectedPathComponent();
            
            if (node != null && selectionListener != null) {
                Object userObject = node.getUserObject();
                
                if (userObject instanceof Project) {
                    System.out.println("ProjectTreePanel: проект вибрано: " + ((Project) userObject).getName());
                    selectionListener.onProjectSelected((Project) userObject);
                } else if (userObject instanceof Board) {
                    Board board = (Board) userObject;
                    System.out.println("ProjectTreePanel: дошку вибрано: " + board + ", Name: " + board.getName());
                    selectionListener.onBoardSelected(board);
                }
            }
        });
        
        add(new JScrollPane(tree), BorderLayout.CENTER);
        
        // Завантажуємо проекти та дошки
        loadProjects();
    }
    
    /**
     * Установка обработчика выбора проекта/доски
     */
    public void setSelectionListener(ProjectSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    /**
     * Завантаження проєктів користувача
     */
    public void loadProjects() {
        // Очистка дерева
        tree.setModel(null);
        
        // Отримання проєктів користувача та додавання їх у дерево
        List<Project> projects = boardService.getProjectsByUser(currentUser);
        
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Projects");
        for (Project project : projects) {
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
            rootNode.add(projectNode);
            
            System.out.println("Додаємо проект: " + project.getName() + " (ID: " + project.getId() + ")");
            
            // Додавання дошок проєкту
            List<Board> boards = boardService.getBoardsByProject(project);
            System.out.println("Отримали " + boards.size() + " дошок для проекту " + project.getName());
            
            for (Board board : boards) {
                System.out.println("ProjectTreePanel: додаємо дошку: " + board + " (ID: " + board.getId() + ")");
                DefaultMutableTreeNode boardNode = new DefaultMutableTreeNode(board);
                projectNode.add(boardNode);
            }
        }
        
        tree.setModel(new DefaultTreeModel(rootNode));
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
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