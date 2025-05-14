package ua.oip.jiralite.ui.listener;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.AuthService;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.model.IssueCardModel;
import ua.oip.jiralite.ui.panel.BoardColumnPanel;
import ua.oip.jiralite.ui.panel.IssueCardPanel;
import ua.oip.jiralite.ui.util.SwingHelper;

/**
 * Обработчик событий перетаскивания карточек в колонку Kanban-доски
 */
public class ColumnDropTarget extends DropTargetAdapter {
    
    private final JPanel column;
    private final Status status;
    private final BoardService boardService;
    private final ResourceBundle messages;
    private final AuthService authService;
    
    /**
     * Конструктор обработчика событий
     * 
     * @param column панель колонки
     * @param status статус колонки
     * @param boardService сервис работы с доской
     * @param messages ресурсы локализации
     */
    public ColumnDropTarget(JPanel column, Status status, BoardService boardService, ResourceBundle messages) {
        this.column = column;
        this.status = status;
        this.boardService = boardService;
        this.messages = messages;
        this.authService = AuthService.getInstance();
        
        // Регистрируем DropTarget для колонки
        new DropTarget(column, DnDConstants.ACTION_MOVE, this, true);
        
        // Если колонка содержит JScrollPane, регистрируем и его как цель
        for (Component comp : column.getComponents()) {
            if (comp instanceof JScrollPane) {
                new DropTarget(comp, DnDConstants.ACTION_MOVE, this, true);
                
                // Регистрируем и контент внутри скроллпейна
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JPanel) {
                    new DropTarget(view, DnDConstants.ACTION_MOVE, this, true);
                    
                    // Регистрируем каждую карточку внутри панели как цель
                    JPanel cardsPanel = (JPanel) view;
                    for (Component cardComp : cardsPanel.getComponents()) {
                        if (cardComp instanceof IssueCardPanel) {
                            new DropTarget(cardComp, DnDConstants.ACTION_MOVE, this, true);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            // Перевіряємо, чи підтримується формат перетягування
            if (!dtde.isDataFlavorSupported(IssueCardMouseAdapter.CardTransferable.CARD_FLAVOR)) {
                dtde.rejectDrop();
                return;
            }
            
            // Проверка прав пользователя на редактирование задач
            User currentUser = authService.getCurrentUser();
            if (currentUser == null || !authService.canEditIssue()) {
                System.err.println("ColumnDropTarget: користувач не має прав на зміну статусу задачі");
                SwingHelper.showErrorDialog(
                    SwingUtilities.getWindowAncestor(column),
                    messages.getString("app.error"),
                    messages.getString("permission.not_allowed")
                );
                dtde.rejectDrop();
                return;
            }
            
            // Приймаємо перетягування
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            
            // Отримуємо об'єкт, що перетягується
            Transferable transferable = dtde.getTransferable();
            IssueCardModel issueModel = (IssueCardModel) transferable.getTransferData(
                    IssueCardMouseAdapter.CardTransferable.CARD_FLAVOR);
            
            // Для користувача з роллю USER перевіряємо, чи є він виконавцем задачі
            if (currentUser.getRole() == ua.oip.jiralite.domain.enums.Role.USER) {
                // Перевіряємо, чи є поточний користувач виконавцем цієї задачі
                // Шукаємо задачу в глобальному списку задач
                boolean isAssignedToCurrentUser = false;
                
                try {
                    // Отримуємо глобальний список задач через рефлексію
                    java.lang.reflect.Field globalIssuesField = boardService.getClass().getDeclaredField("globalIssues");
                    globalIssuesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.List<Issue> globalIssues = (java.util.List<Issue>) globalIssuesField.get(null);
                    
                    // Виведення відлагоджувальної інформації
                    System.out.println("ColumnDropTarget.drop: Перевірка прав USER на редагування задачі " + issueModel.getId());
                    System.out.println("ColumnDropTarget.drop: Поточний користувач: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
                    
                    // Шукаємо задачу в списку та перевіряємо виконавця
                    for (Issue issue : globalIssues) {
                        if (issue.getId().equals(issueModel.getId())) {
                            System.out.println("ColumnDropTarget.drop: Знайдено задачу з ID " + issue.getId());
                            
                            if (issue.getAssignee() != null) {
                                System.out.println("ColumnDropTarget.drop: Виконавець задачі: " + 
                                    issue.getAssignee().getUsername() + " (ID: " + issue.getAssignee().getId() + ")");
                            } else {
                                System.out.println("ColumnDropTarget.drop: У задачі не призначено виконавця");
                                // Якщо задача без виконавця, дозволяємо користувачу USER редагувати її
                                isAssignedToCurrentUser = true;
                                break;
                            }
                            
                            if (issue.getAssignee() != null && 
                                issue.getAssignee().getId() != null && 
                                currentUser.getId() != null &&
                                issue.getAssignee().getId().equals(currentUser.getId())) {
                                System.out.println("ColumnDropTarget.drop: Виконавець співпадає з поточним користувачем");
                                isAssignedToCurrentUser = true;
                            } else {
                                System.out.println("ColumnDropTarget.drop: Виконавець не співпадає з поточним користувачем");
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("ColumnDropTarget: помилка при отриманні інформації про задачу: " + e.getMessage());
                    e.printStackTrace();
                }
                
                if (!isAssignedToCurrentUser) {
                    System.err.println("ColumnDropTarget: користувач з роллю USER не може змінювати задачі інших виконавців");
                    SwingHelper.showErrorDialog(
                        SwingUtilities.getWindowAncestor(column),
                        messages.getString("app.error"),
                        messages.getString("permission.cannot_edit_others_issues")
                    );
                    dtde.dropComplete(false);
                    return;
                }
            }
            
            // Якщо статус задачі не змінився, завершуємо обробку
            if (status.equals(issueModel.getStatus())) {
                dtde.dropComplete(false);
                return;
            }
            
            // Отримуємо початкову колонку, з якої перетягуємо картку
            BoardColumnPanel sourceColumn = null;
            for (Component comp : column.getParent().getComponents()) {
                if (comp instanceof BoardColumnPanel) {
                    BoardColumnPanel column = (BoardColumnPanel) comp;
                    if (column.getStatus().equals(issueModel.getStatus())) {
                        sourceColumn = column;
                        break;
                    }
                }
            }
            
            // Видаляємо картку з початкової колонки
            if (sourceColumn != null) {
                sourceColumn.removeIssue(issueModel.getId());
            }
            
            // Оновлюємо статус в моделі
            Status oldStatus = issueModel.getStatus();
            issueModel.setStatus(status);
            
            System.out.println("ColumnDropTarget.drop: змінено статус з " + oldStatus + " на " + status);
            
            // Примусово оновлюємо статус у глобальному списку задач 
            // для синхронізації з UI
            try {
                // Отримуємо ID задачі
                Long issueId = issueModel.getId();
                if (issueId != null) {
                    System.out.println("ColumnDropTarget.drop: оновлюємо статус задачі з ID " + issueId + " у глобальному списку");
                    boardService.updateIssueStatus(issueId, status);
                }
            } catch (Exception ex) {
                System.err.println("ColumnDropTarget.drop: помилка при оновленні статусу: " + ex.getMessage());
            }
            
            // Створюємо нову картку для задачі в цільовій колонці
            if (column instanceof BoardColumnPanel) {
                BoardColumnPanel columnPanel = (BoardColumnPanel) column;
                columnPanel.addIssueToTop(issueModel);
            }
            
            // Оновлюємо статус задачі через сервіс
            if (boardService != null) {
                // Якщо сервіс доступний, викликаємо метод оновлення статусу
                boardService.updateIssueStatus(issueModel.getId(), status);
                
                // Прибираємо відкладене оновлення дошки, щоб уникнути "стрибків" карток
                System.out.println("ColumnDropTarget: статус задачі з ID " + issueModel.getId() + 
                    " змінено з " + oldStatus + " на " + status);
            } else {
                System.out.println("ColumnDropTarget: boardService == null, не можемо оновити статус задачі");
            }
            
            // Повідомляємо про успішне завершення
            dtde.dropComplete(true);
        } catch (Exception e) {
            System.err.println("ColumnDropTarget: помилка при перетягуванні: " + e.getMessage());
            e.printStackTrace();
            dtde.dropComplete(false);
        }
    }
    
    /**
     * Знаходить панель карток у колонці
     */
    private JPanel findCardsPanel(JPanel columnPanel) {
        // Шукаємо першу JPanel у колонці, припускаємо що це панель карток
        for (Component comp : columnPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JPanel) {
                    return (JPanel) view;
                }
            }
        }
        return null;
    }
    
    /**
     * Запитує підтвердження зміни статусу
     * 
     * @param issueModel модель задачі
     * @return true, якщо зміна підтверджена, false - якщо скасована
     */
    private boolean confirmStatusChange(IssueCardModel issueModel) {
        // Отримуємо назву колонки для повідомлення
        String columnName = getColumnName(status);
        
        String message = messages.getString("issue.change_status_confirm")
                .replace("{0}", columnName);
        
        return SwingHelper.showConfirmDialog(null, 
                messages.getString("app.confirm"), message);
    }
    
    /**
     * Повертає локалізовану назву колонки за статусом
     * 
     * @param status статус
     * @return назва колонки
     */
    private String getColumnName(Status status) {
        switch (status) {
            case TO_DO:
                return messages.getString("column.todo");
            case IN_PROGRESS:
                return messages.getString("column.in_progress");
            case DONE:
                return messages.getString("column.done");
            default:
                return status.name();
        }
    }
} 