package ua.oip.jiralite.ui.listener;

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Box;

import ua.oip.jiralite.domain.enums.Status;
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
            
            // Приймаємо перетягування
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            
            // Отримуємо об'єкт, що перетягується
            Transferable transferable = dtde.getTransferable();
            IssueCardPanel card = (IssueCardPanel) transferable.getTransferData(
                    IssueCardMouseAdapter.CardTransferable.CARD_FLAVOR);
            
            // Сделаем карточку снова видимой
            card.setVisible(true);
            
            // Отримуємо модель задачі
            IssueCardModel issueModel = card.getIssueModel();
            
            // Якщо статус задачі не змінився, завершуємо обробку
            if (status.equals(issueModel.getStatus())) {
                // Просто возвращаем карточку в исходную колонку без изменений
                dtde.dropComplete(true);
                return;
            }
            
            // Запитуємо підтвердження зміни статусу
            final boolean confirmed = confirmStatusChange(issueModel);
            
            if (confirmed) {
                // Оновлюємо статус задачі
                updateIssueStatus(issueModel);
                
                // Добавляем карточку в текущую колонку
                if (column instanceof BoardColumnPanel) {
                    BoardColumnPanel columnPanel = (BoardColumnPanel) column;
                    // Находим родительскую колонку карточки, чтобы удалить ее оттуда
                    Container cardParent = card.getParent();
                    Container boardColumnPanel = null;
                    while (cardParent != null) {
                        if (cardParent.getParent() instanceof BoardColumnPanel) {
                            boardColumnPanel = cardParent.getParent();
                            break;
                        }
                        cardParent = cardParent.getParent();
                    }
                    
                    // Если нашли родительскую колонку и она не текущая, удаляем карточку оттуда
                    if (boardColumnPanel != null && boardColumnPanel != columnPanel) {
                        BoardColumnPanel sourceColumn = (BoardColumnPanel) boardColumnPanel;
                        // Получаем ID задачи
                        Long issueId = issueModel.getId();
                        if (issueId != null) {
                            // Удаляем карточку из исходной колонки
                            sourceColumn.removeIssue(issueId);
                        }
                    }
                    
                    // Создаем новую карточку в текущей колонке
                    // Это предотвращает проблемы с одновременным нахождением компонента в разных контейнерах
                    columnPanel.addIssueWithNewCard(issueModel);
                }
                
                dtde.dropComplete(true);
            } else {
                // Если пользователь отказался от перемещения, сообщаем, что drop не удался,
                // чтобы карточка вернулась в исходную колонку
                dtde.dropComplete(false);
            }
            
        } catch (Exception e) {
            // Виникла помилка при обробці перетягування
            e.printStackTrace();
            dtde.rejectDrop();
            SwingHelper.showErrorDialog(null, 
                    messages.getString("app.error"), 
                    messages.getString("issue.drag_error") + ": " + e.getMessage());
        }
    }
    
    /**
     * Находит панель карточек в колонке
     */
    private JPanel findCardsPanel(JPanel columnPanel) {
        // Ищем первую JPanel в колонке, предполагаем что это панель карточек
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
     * Оновлює статус задачі
     * 
     * @param issueModel модель задачі
     */
    private void updateIssueStatus(IssueCardModel issueModel) {
        // Змінюємо статус в моделі
        issueModel.setStatus(status);
        
        // Оновлюємо UI асинхронно
        SwingUtilities.invokeLater(() -> {
            boardService.updateIssueStatus(issueModel.getId(), status);
        });
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