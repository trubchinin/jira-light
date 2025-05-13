package ua.oip.jiralite.ui.listener;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.util.ResourceBundle;

import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.service.BoardService;
import ua.oip.jiralite.ui.model.IssueCardModel;
import ua.oip.jiralite.ui.panel.IssueCardPanel;
import ua.oip.jiralite.ui.util.SwingHelper;

/**
 * Обробник перетягування карток в колонки.
 * Дозволяє переміщати задачі між статусами на дошці Kanban.
 */
public class ColumnDropTarget extends DropTargetAdapter {
    
    private final Status targetStatus;
    private final BoardService boardService;
    private final ResourceBundle messages;
    
    /**
     * Конструктор цільової області для перетягування
     * 
     * @param targetStatus статус цільової колонки
     * @param boardService сервіс дошок
     * @param messages ресурси локалізації
     */
    public ColumnDropTarget(Status targetStatus, BoardService boardService, ResourceBundle messages) {
        this.targetStatus = targetStatus;
        this.boardService = boardService;
        this.messages = messages;
    }
    
    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            // Перевіряємо, чи підтримується формат перетягування
            if (!dtde.isDataFlavorSupported(IssueCardMouseAdapter.CardTransferable.CARD_FLAVOR)) {
                dtde.rejectDrop();
                return;
            }
            
            // Приймаємо дію перетягування
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            
            // Отримуємо об'єкт, що перетягується
            Transferable transferable = dtde.getTransferable();
            IssueCardPanel card = (IssueCardPanel) transferable.getTransferData(
                    IssueCardMouseAdapter.CardTransferable.CARD_FLAVOR);
            
            // Отримуємо модель задачі
            IssueCardModel issueModel = card.getIssueModel();
            
            // Якщо статус задачі не змінився, завершуємо обробку
            if (targetStatus.equals(issueModel.getStatus())) {
                dtde.dropComplete(false);
                return;
            }
            
            // Підтверджуємо переміщення задачі
            boolean confirmed = confirmStatusChange(issueModel);
            
            if (confirmed) {
                // Змінюємо статус задачі
                updateIssueStatus(issueModel);
                dtde.dropComplete(true);
            } else {
                dtde.dropComplete(false);
            }
            
        } catch (Exception e) {
            // Виникла помилка при обробці перетягування
            dtde.rejectDrop();
            SwingHelper.showErrorDialog(null, 
                    messages.getString("app.error"), 
                    messages.getString("issue.drag_error") + ": " + e.getMessage());
        }
    }
    
    /**
     * Підтверджує зміну статусу задачі
     * 
     * @param issueModel модель задачі
     * @return true, якщо зміна підтверджена, false - якщо скасована
     */
    private boolean confirmStatusChange(IssueCardModel issueModel) {
        // Отримуємо назву колонки для повідомлення
        String columnName = getColumnName(targetStatus);
        
        // Запитуємо підтвердження зміни статусу
        String message = messages.getString("column.move_confirm")
                .replace("{0}", columnName);
        
        return SwingHelper.showConfirmDialog(null, 
                messages.getString("app.confirm"), message);
    }
    
    /**
     * Оновлює статус задачі через сервіс
     * 
     * @param issueModel модель задачі
     */
    private void updateIssueStatus(IssueCardModel issueModel) {
        // Змінюємо статус в моделі
        issueModel.setStatus(targetStatus);
        
        // Оновлюємо задачу через сервіс
        boardService.updateIssueStatus(issueModel.getId(), targetStatus);
    }
    
    /**
     * Повертає локалізовану назву колонки для статусу
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