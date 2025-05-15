package ua.oip.jiralite.ui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

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
public final class ProjectTreePanel extends JPanel {
    
    /**
     * Інтерфейс для обробки подій вибору проєкту та дошки
     */
    public interface ProjectSelectionListener {
        void onProjectSelected(Project project);
        void onBoardSelected(Board board);
    }
    
    private User currentUser;
    private BoardService boardService;
    private ResourceBundle messages;
    
    private ProjectSelectionListener selectionListener;
    private JTree tree;
    
    /**
     * Конструктор панелі дерева проєктів
     * 
     * @param currentUser поточний користувач
     * @param boardService сервіс дошок
     * @param messages ресурси локалізації
     */
    public ProjectTreePanel(User currentUser, BoardService boardService, ResourceBundle messages) {
        try {
            System.out.println("=== Створення ProjectTreePanel ===");
            
            // Ініціалізуємо поля класу
            this.currentUser = currentUser;
            this.boardService = boardService;
            this.messages = messages;
            
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Проекти"));
            
            // Додаємо тестовий лейбл для перевірки
            JLabel testLabel = new JLabel("Ініціалізація...");
            testLabel.setHorizontalAlignment(JLabel.CENTER);
            add(testLabel, BorderLayout.NORTH);
            
            // Ініціалізуємо дерево з найпростішою моделлю
            System.out.println("Ініціалізація порожнього дерева");
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Проекти");
            root.add(new DefaultMutableTreeNode("Завантаження..."));
            
            tree = new JTree(root);
            tree.setRootVisible(true);
            tree.setShowsRootHandles(true);
            
            // Встановлюємо власний рендерер для дерева (якщо потрібно)
            // tree.setCellRenderer(new ProjectTreeCellRenderer());
            
            // Додаємо загальний обробник вибору вузла в дереві
            tree.addTreeSelectionListener(e -> {
                try {
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
                } catch (Exception ex) {
                    System.err.println("Помилка при обробці вибору вузла: " + ex.getMessage());
                }
            });
            
            System.out.println("Створення ScrollPane для дерева");
            JScrollPane treeScrollPane = new JScrollPane(tree);
            add(treeScrollPane, BorderLayout.CENTER);
            
            // Додаємо кнопку оновлення для тестування
            JPanel buttonPanel = new JPanel();
            JButton refreshButton = new JButton(messages.getString("app.refresh"));
            refreshButton.addActionListener(e -> loadProjects());
            
            // Застосовуємо стиль до кнопки
            SwingHelper.applyButtonStyle(refreshButton);
            
            buttonPanel.add(refreshButton);
            add(buttonPanel, BorderLayout.SOUTH);
            
            System.out.println("=== ProjectTreePanel створено ===");
            
            // Завантажуємо проекти у фоновому потоці
            SwingUtilities.invokeLater(this::loadProjects);
            
        } catch (Exception e) {
            System.err.println("ПОМИЛКА при створенні ProjectTreePanel: " + e.getMessage());
            e.printStackTrace();
            setLayout(new BorderLayout());
            add(new JLabel("Помилка завантаження: " + e.getMessage()), BorderLayout.CENTER);
        }
    }
    
    /**
     * Встановлення обробника вибору проєкту/дошки
     */
    public void setSelectionListener(ProjectSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    /**
     * Завантаження проєктів користувача
     */
    public void loadProjects() {
        try {
            System.out.println("ProjectTreePanel.loadProjects: початок завантаження проектів");
            
            // Змінюємо курсор на "очікування"
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
            
            // Найпростіша тестова модель
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Проекти");
            
            try {
                // Отримання проєктів користувача та додавання їх у дерево
                List<Project> projects = boardService.getProjectsByUser(currentUser);
                System.out.println("ProjectTreePanel.loadProjects: отримано " + projects.size() + " проектів");
                
                if (projects.isEmpty()) {
                    // Якщо проектів немає, додаємо заглушку
                    rootNode.add(new DefaultMutableTreeNode("Немає доступних проектів"));
                } else {
                    // Додаємо всі проекти
                    for (Project project : projects) {
                        DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
                        rootNode.add(projectNode);
                        
                        System.out.println("ProjectTreePanel.loadProjects: додаємо проект: " + project.getName() + " (ID: " + project.getId() + ")");
                        
                        try {
                            // Додавання дошок проєкту
                            List<Board> boards = boardService.getBoardsByProject(project);
                            System.out.println("ProjectTreePanel.loadProjects: отримали " + boards.size() + " дошок для проекту " + project.getName());
                            
                            if (boards.isEmpty()) {
                                // Якщо дошок немає, додаємо заглушку
                                projectNode.add(new DefaultMutableTreeNode("Немає дошок"));
                            } else {
                                // Додаємо всі дошки
                                for (Board board : boards) {
                                    System.out.println("ProjectTreePanel.loadProjects: додаємо дошку: " + board.getName() + " (ID: " + board.getId() + ")");
                                    DefaultMutableTreeNode boardNode = new DefaultMutableTreeNode(board);
                                    projectNode.add(boardNode);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Помилка при завантаженні дошок для проекту: " + e.getMessage());
                            projectNode.add(new DefaultMutableTreeNode("Помилка завантаження дошок"));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Помилка при завантаженні проектів: " + e.getMessage());
                e.printStackTrace();
                rootNode.add(new DefaultMutableTreeNode("Помилка завантаження проектів"));
            }
            
            // Встановлюємо модель дерева
            System.out.println("ProjectTreePanel.loadProjects: встановлюємо модель дерева");
            tree.setModel(new DefaultTreeModel(rootNode));
            
            // Розгортаємо всі вузли
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
            
            // Повертаємо звичайний курсор
            setCursor(java.awt.Cursor.getDefaultCursor());
            
            // Оновлюємо UI
            revalidate();
            repaint();
            
            System.out.println("ProjectTreePanel.loadProjects: завершено");
        } catch (Exception e) {
            System.err.println("КРИТИЧНА ПОМИЛКА при завантаженні проектів: " + e.getMessage());
            e.printStackTrace();
            setCursor(java.awt.Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Метод для перезавантаження проектів (використовується з MainFrame)
     */
    public void refreshProjects() {
        loadProjects();
    }
    
    /**
     * Перевіряє, чи було ініціалізоване дерево проектів
     * @return true якщо дерево ініціалізоване і містить проекти
     */
    public boolean isInitialized() {
        if (tree == null || tree.getModel() == null) {
            return false;
        }
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        return root.getChildCount() > 0 && 
               !(root.getChildCount() == 1 && 
                 "Завантаження...".equals(((DefaultMutableTreeNode)root.getChildAt(0)).getUserObject()));
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