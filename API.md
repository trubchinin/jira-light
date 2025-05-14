# API та інтерфейси між модулями Jira Lite

## Загальна схема взаємодії модулів

```
+------------------+          +------------------+          +------------------+
|                  |          |                  |          |                  |
| jiralite-domain  | <------- | jiralite-service | <------- |   jiralite-ui    |
|                  |          |                  |          |                  |
+------------------+          +------------------+          +------------------+
```

## Модуль jiralite-domain

### Основні сутності

#### User

```java
public class User {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private Role role; // enum: ADMIN, USER, GUEST

    // Getters and setters
}
```

#### Project

```java
public class Project {
    private Long id;
    private String name;
    private String key;
    private String description;
    private User lead;
    private List<User> members;

    // Getters and setters
}
```

#### Board

```java
public class Board {
    private Long id;
    private String name;
    private Project project;

    // Getters and setters
}
```

#### Issue

```java
public class Issue {
    private Long id;
    private String key;
    private String title;
    private String description;
    private Status status; // enum: TO_DO, IN_PROGRESS, DONE
    private Priority priority; // enum: HIGHEST, HIGH, MEDIUM, LOW, LOWEST
    private User assignee;
    private User reporter;
    private Project project;
    private Board board;

    // Getters and setters
}
```

### Енумерації

#### Role

```java
public enum Role {
    ADMIN, USER, GUEST
}
```

#### Status

```java
public enum Status {
    TO_DO, IN_PROGRESS, DONE
}
```

#### Priority

```java
public enum Priority {
    HIGHEST, HIGH, MEDIUM, LOW, LOWEST
}
```

## Модуль jiralite-service

### AuthService

Сервіс для автентифікації користувачів та управління правами доступу.

```java
public class AuthService {
    // Singleton
    private static AuthService instance;

    public static AuthService getInstance();

    // Методи автентифікації
    public User signIn(String username, String password) throws AuthException;
    public void logout();

    // Методи отримання поточного користувача
    public User getCurrentUser();

    // Методи перевірки прав
    public boolean canCreateIssue();
    public boolean canEditIssue();
    public boolean canDeleteIssue();
}
```

#### Використання

```java
// Отримання екземпляру сервісу
AuthService authService = AuthService.getInstance();

// Автентифікація
try {
    User user = authService.signIn("username", "password");
    System.out.println("Автентифіковано як: " + user.getFullName());
} catch (AuthException e) {
    System.err.println("Помилка автентифікації: " + e.getMessage());
}

// Перевірка прав
if (authService.canCreateIssue()) {
    // Дозволити створення задачі
}

// Вихід з системи
authService.logout();
```

### BoardService

Сервіс для роботи з дошками та задачами.

```java
public class BoardService {
    // Singleton
    private static BoardService instance;

    public static BoardService getInstance();

    // Методи для роботи з дошками
    public Board getBoardByProject(Long projectId);
    public List<Board> getBoardsByProject(Project project);

    // Методи для роботи з проектами
    public List<Project> getProjectsByUser(User user);
    public List<User> getProjectMembers(Project project);

    // Методи для роботи з задачами
    public List<Issue> getBoardIssues(Board board);
    public Issue createIssue(Board board, User reporter, User assignee, Object issueModel);
    public void updateIssue(Issue issue);
    public void updateIssueStatus(Issue issue, Status status);

    // Методи для перевірки прав на редагування задачі
    public boolean canUserEditIssue(Issue issue);
    public boolean canUserEditIssue(Long issueId);

    // Методи для роботи з коментарями
    public Comment addComment(Issue issue, String content);
    public Comment addComment(Issue issue, User author, String content);
}
```

#### Використання

```java
// Отримання екземпляру сервісу
BoardService boardService = BoardService.getInstance();

// Отримання дошки за ID проекту
Board board = boardService.getBoardByProject(1L);

// Отримання задач дошки
List<Issue> issues = boardService.getBoardIssues(board);

// Створення нової задачі
User currentUser = authService.getCurrentUser();
Issue newIssue = new Issue();
newIssue.setTitle("Нова задача");
newIssue.setDescription("Опис нової задачі");
Issue createdIssue = boardService.createIssue(board, currentUser, currentUser, newIssue);

// Оновлення статусу задачі
boardService.updateIssueStatus(createdIssue, Status.IN_PROGRESS);

// Перевірка прав на редагування задачі
if (boardService.canUserEditIssue(createdIssue)) {
    // Дозволити редагування задачі
}
```

## Модуль jiralite-ui

### Основні класи інтерфейсу

#### Launcher

```java
public class Launcher {
    public static void main(String[] args);
}
```

#### LoginFrame

```java
public class LoginFrame extends JFrame {
    // Конструктор
    public LoginFrame();

    // Методи обробки подій
    private void handleLogin();
}
```

#### MainFrame

```java
public class MainFrame extends JFrame {
    // Конструктор
    public MainFrame(User user);

    // Методи для оновлення інтерфейсу
    public void refreshBoardView();
}
```

#### BoardColumnPanel

```java
public class BoardColumnPanel extends JPanel {
    // Конструктор
    public BoardColumnPanel(Status status, List<Issue> issues);

    // Методи для взаємодії з колонкою
    public void addIssue(Issue issue);
    public void removeIssue(Issue issue);
}
```

#### IssueCardPanel

```java
public class IssueCardPanel extends JPanel {
    // Конструктор
    public IssueCardPanel(Issue issue);

    // Методи для взаємодії з карткою
    public void updateView();
    public Issue getIssue();
}
```

### Drag-and-Drop функціональність

#### IssueCardMouseAdapter

```java
public class IssueCardMouseAdapter extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e);

    @Override
    public void mouseDragged(MouseEvent e);

    @Override
    public void mouseReleased(MouseEvent e);
}
```

#### ColumnDropTarget

```java
public class ColumnDropTarget extends DropTarget {
    @Override
    public void drop(DropTargetDropEvent dtde);
}
```

## Взаємодія між модулями

### jiralite-domain → jiralite-service

Модуль jiralite-domain надає базові сутності та енумерації, які використовуються в сервісному шарі. Модуль jiralite-service використовує ці сутності для реалізації бізнес-логіки.

### jiralite-service → jiralite-ui

Модуль jiralite-ui використовує сервіси з модуля jiralite-service для виконання операцій:

-   `AuthService` для автентифікації та перевірки прав
-   `BoardService` для роботи з дошками, задачами та проектами

## Приклад повного потоку взаємодії

### Сценарій: Користувач переміщує задачу з колонки "To Do" в колонку "In Progress"

1. Користувач перетягує картку задачі з колонки "To Do" в колонку "In Progress"
2. У класі `IssueCardMouseAdapter` викликається метод `mousePressed` для початку перетягування
3. У класі `ColumnDropTarget` викликається метод `drop` при завершенні перетягування
4. У методі `drop` виконується перевірка прав користувача на редагування задачі через `boardService.canUserEditIssue(issue)`
5. Якщо користувач має права, статус задачі оновлюється через `boardService.updateIssueStatus(issue, Status.IN_PROGRESS)`
6. Інтерфейс оновлюється для відображення змін

### Сценарій: Користувач створює нову задачу

1. Користувач натискає кнопку "Створити задачу" в інтерфейсі
2. Відкривається діалогове вікно `IssueDialog`
3. Користувач заповнює поля та натискає "Зберегти"
4. У обробнику події викликається метод `boardService.createIssue(board, currentUser, assignee, issueModel)`
5. Нова задача додається в колонку "To Do" через метод `boardColumnPanel.addIssue(newIssue)`
6. Інтерфейс оновлюється для відображення нової задачі

### Сценарій: Гість намагається редагувати задачу

1. Гість намагається перетягнути картку задачі
2. У класі `ColumnDropTarget` викликається метод `drop`
3. У методі `drop` виконується перевірка прав через `boardService.canUserEditIssue(issue)`
4. Оскільки гість не має прав на редагування, відображається повідомлення про відсутність прав
5. Задача повертається на початкову позицію
