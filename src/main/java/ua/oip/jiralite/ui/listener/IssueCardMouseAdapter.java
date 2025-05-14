package ua.oip.jiralite.ui.listener;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ua.oip.jiralite.ui.panel.IssueCardPanel;
import ua.oip.jiralite.ui.panel.BoardColumnPanel;

/**
 * Обработчик событий мыши для карточек задач
 */
public class IssueCardMouseAdapter extends MouseAdapter implements DragGestureListener, DragSourceListener {
    
    private final DragSource dragSource;
    private IssueCardPanel draggedCard;
    private boolean isDragging = false;
    
    // Сохраняем информацию о исходной колонке и позиции для возможности отката
    private Container originalParent;
    private int originalIndex;
    private Point originalLocation;
    
    /**
     * Конструктор обработчика
     */
    public IssueCardMouseAdapter() {
        this.dragSource = new DragSource();
        this.dragSource.createDefaultDragGestureRecognizer(
                null, DnDConstants.ACTION_MOVE, this);
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        Component component = e.getComponent();
        
        // Знаходимо батьківську панель картки, якщо натиснуто на її дочірній компонент
        while (component != null && !(component instanceof IssueCardPanel)) {
            component = component.getParent();
        }
        
        if (component instanceof IssueCardPanel) {
            draggedCard = (IssueCardPanel) component;
            
            // Сохраняем исходную позицию для возможного отката
            originalParent = draggedCard.getParent();
            originalLocation = draggedCard.getLocation();
            
            // Запоминаем индекс карточки в оригинальном контейнере
            if (originalParent != null) {
                Component[] components = originalParent.getComponents();
                for (int i = 0; i < components.length; i++) {
                    if (components[i] == draggedCard) {
                        originalIndex = i;
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (isDragging) {
            isDragging = false;
        }
    }
    
    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        if (draggedCard != null) {
            // Визначаємо точку захоплення картки
            Point dragOrigin = dge.getDragOrigin();
            JComponent c = (JComponent) dge.getComponent();
            Point relativePoint = SwingUtilities.convertPoint(c, dragOrigin, draggedCard);
            
            // Важно: НЕ удаляем карточку до успешного завершения перетаскивания
            // Просто делаем ее невидимой на время перетаскивания, чтобы пользователь
            // не видел двух копий одной карточки
            if (draggedCard.getParent() != null) {
                draggedCard.setVisible(false);
            }
            
            // Починаємо операцію перетягування
            isDragging = true;
            dragSource.startDrag(
                    dge,
                    DragSource.DefaultMoveDrop,
                    new CardTransferable(draggedCard),
                    this
            );
        }
    }
    
    // Реализация методов интерфейса DragSourceListener
    @Override
    public void dragEnter(DragSourceDragEvent dsde) {}
    
    @Override
    public void dragOver(DragSourceDragEvent dsde) {}
    
    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {}
    
    @Override
    public void dragExit(DragSourceEvent dse) {}
    
    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        // Если перетаскивание не завершилось успехом или карточка
        // по какой-то причине осталась невидимой
        if (draggedCard != null) {
            // Восстанавливаем видимость карточки
            draggedCard.setVisible(true);
            
            // Если перетаскивание было не успешным, возвращаем карточку на место
            if (!dsde.getDropSuccess() && originalParent != null) {
                // Проверяем, что карточка все еще существует
                if (draggedCard.getParent() != originalParent) {
                    // Если карточка была удалена из родителя, возвращаем ее обратно
                    try {
                        originalParent.add(draggedCard, originalIndex);
                        draggedCard.setLocation(originalLocation);
                        originalParent.revalidate();
                        originalParent.repaint();
                        
                        // Если родительский контейнер - колонка Kanban, обновляем структуру данных
                        if (originalParent.getParent() instanceof BoardColumnPanel) {
                            BoardColumnPanel columnPanel = (BoardColumnPanel) originalParent.getParent();
                            columnPanel.restoreCard(draggedCard);
                        }
                    } catch (Exception e) {
                        // Восстановление не удалось, логируем ошибку
                        System.err.println("Не удалось восстановить карточку: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        
        // Очищаем состояние
        isDragging = false;
        draggedCard = null;
        originalParent = null;
    }
    
    /**
     * Класс для передачи карточки в операции Drag and Drop
     */
    public static class CardTransferable implements Transferable {
        
        public static final DataFlavor CARD_FLAVOR = 
                new DataFlavor(IssueCardPanel.class, "IssueCardPanel");
        
        private final IssueCardPanel card;
        
        public CardTransferable(IssueCardPanel card) {
            this.card = card;
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { CARD_FLAVOR };
        }
        
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return CARD_FLAVOR.equals(flavor);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) 
                throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return card;
        }
    }
    
    /**
     * Вспомогательный класс для работы с координатами в Swing
     */
    private static class SwingUtilities {
        public static Point convertPoint(Component source, Point pt, Component destination) {
            Point p = new Point(pt);
            for (Component c = source; c != destination && c != null; c = c.getParent()) {
                p.translate(c.getX(), c.getY());
            }
            return p;
        }
    }
} 