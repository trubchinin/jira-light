# Jira Lite UI

UI-модуль системы управления задачами Jira Lite, реализующий графический интерфейс пользователя на основе Java Swing.

## Структура модуля

```
src/main/java/ua/oip/jiralite/ui
├─ Launcher.java                 # Точка входа (создает LoginFrame)
├─ frame/
│  ├─ LoginFrame.java            # Аутентификация
│  ├─ MainFrame.java             # Kanban-доска
│  └─ IssueDialog.java           # Детали задачи
├─ panel/
│  ├─ ProjectTreePanel.java      # Левая часть JSplitPane
│  ├─ BoardColumnPanel.java      # Одна колонка "To Do / In Progress / Done"
│  └─ IssueCardPanel.java        # Карточка задачи на доске
├─ model/
│  └─ IssueCardModel.java        # View-Model для карточки (паттерн MVC)
├─ util/
│  ├─ UiConstants.java           # Цвета, шрифты, иконки
│  └─ SwingHelper.java           # Фабрики компонентов, диалоги ошибок
└─ listener/
   ├─ LoginAction.java           # ActionListener кнопки "Sign in"
   ├─ IssueCardMouseAdapter.java # Drag-&-Drop задач
   └─ ColumnDropTarget.java      # Прием карточки в колонку
```

## Особенности архитектуры

1. **Модель-Представление-Контроллер (MVC)**:

    - Модель (Model): DTO-классы из пакета `model` и сервисы из модуля `jiralite-service`
    - Представление (View): классы в пакетах `frame` и `panel`
    - Контроллер (Controller): обработчики событий в пакете `listener`

2. **Инверсия управления**:

    - Окна не создают сервисы непосредственно — им инжектируются интерфейсы через конструкторы
    - Это облегчает тестирование UI

3. **Интернационализация**:
    - Реализована через `ResourceBundle` с возможностью переключения между украинским и английским языками

## Запуск

Запуск модуля возможен через Maven:

```
mvn clean package
java -jar target/jiralite-ui.jar
```

## Скриншоты

Окно аутентификации:
![Login Screen](docs/images/login.png)

Основной интерфейс с Kanban-доской:
![Main Screen](docs/images/main.png)

Редактирование задачи:
![Issue Dialog](docs/images/issue.png)

## Drag & Drop

Модуль поддерживает перетаскивание карточек задач между колонками статусов с подтверждением изменения статуса задачи.
