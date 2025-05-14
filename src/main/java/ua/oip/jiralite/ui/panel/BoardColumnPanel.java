/**
 * Восстанавливает карточку в колонке, если она была удалена при перетаскивании
 * 
 * @param card карточка задачи для восстановления
 */
public void restoreCard(IssueCardPanel card) {
    if (card != null && card.getIssueModel() != null) {
        Long issueId = card.getIssueModel().getId();
        
        // Если карточка отсутствует в коллекции, восстанавливаем ее
        if (!cards.containsKey(issueId)) {
            cards.put(issueId, card);
            
            // Добавляем физически карточку в панель с отступом
            if (!Arrays.asList(cardsPanel.getComponents()).contains(card)) {
                cardsPanel.add(Box.createVerticalStrut(UiConstants.COMPONENT_SPACING));
                cardsPanel.add(card);
            }
        }
        
        // В любом случае делаем карточку видимой
        card.setVisible(true);
        
        // Обновляем UI
        revalidate();
        repaint();
    }
}

/**
 * Добавляет задачу в колонку с созданием новой карточки из модели
 * Это предотвращает проблемы с одновременным нахождением компонента в разных контейнерах
 * 
 * @param issueModel модель задачи
 */
public void addIssueWithNewCard(IssueCardModel issueModel) {
    if (issueModel == null) return;
    
    // Удаляем существующую карточку с таким же ID, если есть
    Long issueId = issueModel.getId();
    if (issueId != null && cards.containsKey(issueId)) {
        removeIssue(issueId);
    }
    
    // Создаем новую карточку
    IssueCardPanel newCard = new IssueCardPanel(issueModel, messages);
    
    // Регистрируем DropTarget для карточки, чтобы можно было перетаскивать на нее
    new DropTarget(newCard, DnDConstants.ACTION_MOVE, 
            new ColumnDropTarget(this, status, boardService, messages));
    
    // Добавляем карточку в колонку через стандартный метод
    addIssue(issueModel, card -> {
        // Добавляем обработчик перетаскивания
        IssueCardMouseAdapter dragHandler = new IssueCardMouseAdapter();
        card.addMouseListener(dragHandler);
    });
} 