package ua.oip.jiralite.ui.listener;

import java.awt.Component;
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

import ua.oip.jiralite.ui.panel.IssueCardPanel;

/**
 * Обробник перетягування карток задач.
 * Реалізує функціональність Drag-and-Drop для карток задач на дошці.
 */
public class IssueCardMouseAdapter extends MouseAdapter implements DragSourceListener, DragGestureListener {
    
    private final DragSource dragSource;
    private IssueCardPanel draggedCard;
    private boolean isDragging = false;
    
    /**
     * Конструктор адаптера для перетягування карток
     */
    public IssueCardMouseAdapter() {
        this.dragSource = new DragSource();
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
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        // Скидаємо перетягування, якщо воно не завершилося успішно
        if (!isDragging && draggedCard != null) {
            draggedCard = null;
        }
    }
    
    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        if (draggedCard == null) {
            return;
        }
        
        // Створюємо transferable об'єкт для передачі даних картки
        CardTransferable transferable = new CardTransferable(draggedCard);
        
        // Починаємо перетягування
        dragSource.startDrag(dge, DragSource.DefaultMoveDrop, 
                transferable, this);
        
        isDragging = true;
    }
    
    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        // Обробка завершення перетягування
        isDragging = false;
        draggedCard = null;
    }
    
    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
        // Нічого не робимо
    }
    
    @Override
    public void dragExit(DragSourceEvent dse) {
        // Нічого не робимо
    }
    
    @Override
    public void dragOver(DragSourceDragEvent dsde) {
        // Нічого не робимо
    }
    
    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
        // Нічого не робимо
    }
    
    /**
     * Клас для передачі даних картки під час перетягування
     */
    private static class CardTransferable implements Transferable {
        
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
            if (isDataFlavorSupported(flavor)) {
                return card;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
} 