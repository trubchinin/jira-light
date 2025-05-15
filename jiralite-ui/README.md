# Jira Lite UI

UI-модуль системи управління задачами Jira Lite, що реалізує графічний інтерфейс користувача на основі Java Swing.

## Структура модуля

```
src/main/java/ua/oip/jiralite/ui
├─ Launcher.java                 # Точка входу (створює LoginFrame)
├─ frame/
│  ├─ LoginFrame.java            # Автентифікація
│  ├─ MainFrame.java             # Kanban-дошка
│  └─ IssueDialog.java           # Деталі задачі
├─ panel/
│  ├─ ProjectTreePanel.java      # Ліва частина JSplitPane
│  ├─ BoardColumnPanel.java      # Одна колонка "Зробити / В роботі / Завершено"
│  ├─ SearchPanel.java           # Панель пошуку та фільтрації
│  ├─ SearchResultPanel.java     # Відображення результатів пошуку
│  └─ IssueCardPanel.java        # Картка задачі на дошці
├─ model/
│  └─ IssueCardModel.java        # View-Model для картки (патерн MVC)
├─ util/
│  ├─ UiConstants.java           # Кольори, шрифти, іконки
│  ├─ ThemeManager.java          # Керування темами оформлення
│  └─ SwingHelper.java           # Фабрики компонентів, діалоги помилок
└─ listener/
   ├─ LoginAction.java           # ActionListener кнопки "Увійти"
   ├─ IssueCardMouseAdapter.java # Drag-&-Drop задач
   └─ ColumnDropTarget.java      # Прийом картки в колонку
```

## Особливості архітектури

1. **Модель-Представлення-Контролер (MVC)**:

    - Модель (Model): DTO-класи з пакету `model` та сервіси з модуля `jiralite-service`
    - Представлення (View): класи в пакетах `frame` та `panel`
    - Контролер (Controller): обробники подій у пакеті `listener`

2. **Інверсія управління**:

    - Вікна не створюють сервіси безпосередньо — їм інжектуються інтерфейси через конструктори
    - Це полегшує тестування UI

3. **Інтернаціоналізація**:
    - Реалізована через `ResourceBundle` з підтримкою української мови
    - Всі елементи інтерфейсу використовують локалізовані рядки
    - Файли локалізації знаходяться в `src/main/resources/i18n/`

## Локалізація

У проекті реалізовано повну локалізацію інтерфейсу на українську мову:

-   Усі елементи інтерфейсу користувача перекладено українською
-   Використано механізм ResourceBundle для збереження та завантаження текстів
-   Всі статичні рядки винесено з коду у файли локалізації
-   Додана підтримка для коректного відображення всіх компонентів українською мовою

Локалізаційні файли:

-   `labels_uk.properties` - основний файл локалізації українською мовою

## Запуск

Запуск модуля можливий через Maven:

```
mvn clean package
java -jar target/jiralite-ui.jar
```

## Скріншоти

Вікно автентифікації:
![Login Screen](docs/images/login.png)

Основний інтерфейс з Kanban-дошкою:
![Main Screen](docs/images/main.png)

Редагування задачі:
![Issue Dialog](docs/images/issue.png)

## Drag & Drop

Модуль підтримує перетягування карток задач між колонками статусів з підтвердженням зміни статусу задачі.
