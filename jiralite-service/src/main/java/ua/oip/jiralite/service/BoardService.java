package ua.oip.jiralite.service;

import java.util.ArrayList;
import java.util.List;

import ua.oip.jiralite.domain.Board;
import ua.oip.jiralite.domain.Comment;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;
import ua.oip.jiralite.domain.User;
import ua.oip.jiralite.domain.enums.Priority;
import ua.oip.jiralite.domain.enums.Status;
import ua.oip.jiralite.repository.IssueRepository;
import ua.oip.jiralite.repository.ProjectRepository;

/**
 * Сервіс для роботи з дошками та задачами
 */
public class BoardService {
    
    private static BoardService instance;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final AuthService authService;
    
    // Список для зберігання глобально створених задач (імітація БД)
    private static final List<Issue> globalIssues = new ArrayList<>();
    
    /**
     * Повертає Singleton екземпляр сервісу
     */
    public static BoardService getInstance() {
        if (instance == null) {
            instance = new BoardService(null, null, null);
        }
        return instance;
    }
    
    /**
     * Конструктор для впровадження залежностей
     */
    public BoardService(ProjectRepository projectRepository, IssueRepository issueRepository, AuthService authService) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.authService = authService;
    }
    
    /**
     * Отримує дошку за ID проекту
     */
    public Board getBoardByProject(Long projectId) {
        // У реальному додатку тут був би запит до БД
        // Для прототипу повертаємо фіктивну дошку
        
        Board board = new Board();
        board.setId(1L);
        
        // Створюємо демо-користувача для проекту
        User admin = new User("admin", "password", "Administrator", "admin@example.com");
        admin.setId(1L);
        
        // Створюємо проект за допомогою сеттерів
        Project project = new Project();
        project.setId(projectId);
        project.setName("Demo Project");
        project.setKey("DEMO");
        project.setLead(admin); // Встановлюємо керівника проекту
        
        board.setProject(project);
        
        // Створюємо демо-задачі
        createDemoIssues(project);
        
        return board;
    }
    
    /**
     * Отримує список досок проекту
     */
    public List<Board> getBoardsByProject(Project project) {
        // У реальному додатку тут був би запит до БД
        List<Board> boards = new ArrayList<>();
        
        System.out.println("BoardService.getBoardsByProject: створюємо нову дошку");
        Board board = new Board();
        board.setId(1L);
        board.setName("Kanban Board");  // Встановлюємо назву дошки
        board.setProject(project);      // Зв'язуємо дошку з проектом
        
        System.out.println("BoardService: створюємо дошку з назвою '" + board.getName() + "' для проекту " + project.getName());
        System.out.println("BoardService: перевіряємо toString(): " + board.toString());
        
        boards.add(board);
        
        return boards;
    }
    
    /**
     * Отримує список проектів користувача
     */
    public List<Project> getProjectsByUser(User user) {
        // У реальному додатку тут був би запит до БД
        List<Project> projects = new ArrayList<>();
        
        // Створюємо проект за допомогою сеттерів
        Project project = new Project();
        project.setId(1L);
        project.setName("Demo Project");
        project.setKey("DEMO");
        project.setLead(user); // Встановлюємо текучого користувача як керівника
        
        projects.add(project);
        
        return projects;
    }
    
    /**
     * Отримує учасників проекту
     */
    public List<User> getProjectMembers(Project project) {
        // У реальному додатку тут був би запит до БД
        List<User> members = new ArrayList<>();
        
        User user1 = new User("admin", "password", "Administrator", "admin@example.com");
        user1.setId(1L);
        
        User user2 = new User("user", "password", "User", "user@example.com");
        user2.setId(2L);
        
        members.add(user1);
        members.add(user2);
        
        return members;
    }
    
    /**
     * Отримує всі задачі дошки
     */
    public List<Issue> getBoardIssues(Board board) {
        // У реальному додатку тут був би запит до БД
        // Для прототипу повертаємо задачі з глобального списку
        System.out.println("BoardService.getBoardIssues: отримуємо задачі для дошки " + board.getName());
        
        if (board != null && board.getProject() != null) {
            // Спочатку створюємо базові демо-задачі, якщо їх немає
            createDemoIssues(board.getProject());
            
            // Створюємо список задач, відносячихся до поточного проекту
            List<Issue> result = new ArrayList<>();
            
            // Отримуємо задачі для поточного проекту з глобального списку
            System.out.println("BoardService.getBoardIssues: фільтруємо задачі для проекту " + board.getProject().getName());
            for (Issue issue : globalIssues) {
                boolean includeIssue = false;
                
                // Перевіряємо, відноситься чи задача до поточного проекту
                if (issue.getProject() == null) {
                    System.out.println("BoardService.getBoardIssues: задача з ID " + issue.getId() + 
                        " не має проекту, встановлюю поточний проект");
                    issue.setProject(board.getProject());
                    includeIssue = true;
                } 
                else if (issue.getProject() != null && 
                         issue.getProject().getId() != null && 
                         board.getProject() != null &&
                         board.getProject().getId() != null &&
                         issue.getProject().getId().equals(board.getProject().getId())) {
                    includeIssue = true;
                }
                
                if (includeIssue) {
                    result.add(issue);
                    System.out.println("BoardService.getBoardIssues: додано задачу до результату: ID=" + 
                        issue.getId() + ", назва='" + issue.getTitle() + "', статус=" + issue.getStatus());
                }
            }
            
            // Виводимо інформацію про фінальний список задач
            System.out.println("BoardService.getBoardIssues: фінальний список задач для відображення (розмір: " + result.size() + "):");
            for (Issue issue : result) {
                System.out.println("  - Задача для відображення: ID=" + issue.getId() + 
                    ", назва='" + issue.getTitle() + "', статус=" + issue.getStatus() +
                    ", опис=" + (issue.getDescription() != null ? 
                       issue.getDescription().substring(0, Math.min(30, issue.getDescription().length())) + "..." : "null"));
            }
            
            return result;
        }
        
        System.out.println("BoardService.getBoardIssues: дошка або проект є null, повертаємо порожній список");
        return new ArrayList<>();
    }
    
    /**
     * Створює демо-задачі для прототипу
     */
    private void createDemoIssues(Project project) {
        // Перевіряємо, чи є вже базові демо-задачі в глобальному списку
        boolean hasBasicIssues = false;
        for (Issue issue : globalIssues) {
            if (issue.getId() != null && issue.getId() <= 3) {
                hasBasicIssues = true;
                break;
            }
        }
        
        // Створюємо базові демо-задачі тільки якщо їх ще немає
        if (!hasBasicIssues) {
            System.out.println("BoardService.createDemoIssues: створюємо базові демо-задачі");
            
            // Створюємо демо-користувачів
            User admin = new User("admin", "password", "Administrator", "admin@example.com");
            admin.setId(1L);
            
            User john = new User("john", "password", "John Developer", "john@example.com");
            john.setId(2L);
            
            User guest = new User("guest", "password", "Guest User", "guest@example.com");
            guest.setId(3L);
            
            // Створюємо базові демонстраційні задачі
            Issue issue1 = new Issue();
            issue1.setId(1L);
            issue1.setTitle("Налаштувати аутентифікацію");
            issue1.setDescription("Інтеграція з OAuth 2.0");
            issue1.setStatus(Status.TO_DO);
            issue1.setPriority(Priority.HIGH);
            issue1.setProject(project);
            issue1.setAssignee(john);
            issue1.setReporter(admin);
            issue1.setKey(project.getKey() + "-1");
            
            Issue issue2 = new Issue();
            issue2.setId(2L);
            issue2.setTitle("Розробити UI для дошки");
            issue2.setDescription("Створити інтерфейс канбан-дошки");
            issue2.setStatus(Status.IN_PROGRESS);
            issue2.setPriority(Priority.MEDIUM);
            issue2.setProject(project);
            issue2.setAssignee(admin);
            issue2.setReporter(admin);
            issue2.setKey(project.getKey() + "-2");
            
            Issue issue3 = new Issue();
            issue3.setId(3L);
            issue3.setTitle("Написати документацію");
            issue3.setDescription("Створити документацію для API");
            issue3.setStatus(Status.DONE);
            issue3.setPriority(Priority.LOW);
            issue3.setProject(project);
            issue3.setAssignee(guest);
            issue3.setReporter(john);
            issue3.setKey(project.getKey() + "-3");
            
            // Додаємо задачу, створену админом, але призначену на john для демонстрації обмежень
            Issue issue4 = new Issue();
            issue4.setId(4L);
            issue4.setTitle("Покращити безпеку системи");
            issue4.setDescription("Впровадити додаткові перевірки прав доступу");
            issue4.setStatus(Status.TO_DO);
            issue4.setPriority(Priority.HIGHEST);
            issue4.setProject(project);
            issue4.setAssignee(admin);  // Виконавець - адмін
            issue4.setReporter(admin);  // Створювач - адмін
            issue4.setKey(project.getKey() + "-4");
            
            // Додаємо задачі напряму в globalIssues
            addToGlobalIssuesList(issue1);
            addToGlobalIssuesList(issue2);
            addToGlobalIssuesList(issue3);
            addToGlobalIssuesList(issue4);
            
            System.out.println("BoardService.createDemoIssues: створено 4 базових демо-задач");
        } else {
            System.out.println("BoardService.createDemoIssues: базові демо-задачі вже існують");
        }
        
        // Проходимось по всіх задачах у глобальному списку та оновлюємо їх проекти, якщо потрібно
        for (Issue issue : globalIssues) {
            if (issue.getProject() == null || !issue.getProject().getId().equals(project.getId())) {
                System.out.println("BoardService.createDemoIssues: оновлюємо проект для задачі " + issue.getTitle());
                issue.setProject(project);
                
                // Оновлюємо ключ задачі, якщо змінився проект
                if (issue.getKey() == null || !issue.getKey().startsWith(project.getKey())) {
                    issue.setKey(project.getKey() + "-" + issue.getId());
                    System.out.println("BoardService.createDemoIssues: згенеровано новий ключ " + issue.getKey() + " для задачі " + issue.getTitle());
                }
            }
        }
        
        // Виводимо список задач
        System.out.println("BoardService.createDemoIssues: список задач у глобальному списку (розмір: " + globalIssues.size() + "):");
        for (Issue i : globalIssues) {
            System.out.println("  - Задача: ID=" + i.getId() + ", назва='" + i.getTitle() + 
                "', статус=" + i.getStatus() + ", ключ=" + i.getKey());
        }
    }
    
    /**
     * Додає комментарий до задачі
     */
    public Comment addComment(Issue issue, String content) {
        User currentUser = authService.getCurrentUser();
        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setAuthor(currentUser);
        
        return comment;
    }
    
    /**
     * Перегруженная версія методу для підтримки UI
     */
    public Comment addComment(Issue issue, User author, String content) {
        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setAuthor(author);
        
        return comment;
    }
    
    /**
     * Оновлює існуючу задачу
     */
    public void updateIssue(Issue issue) {
        if (issue == null) {
            System.err.println("BoardService.updateIssue: задача null, оновлення неможливе");
            return;
        }
        
        System.out.println("BoardService.updateIssue: оновлюємо задачу з ID " + issue.getId() + 
            ", назвою '" + issue.getTitle() + "', статусом " + issue.getStatus());
        
        // Оновлюємо задачу в глобальному списку
        boolean found = false;
        for (int i = 0; i < globalIssues.size(); i++) {
            Issue existingIssue = globalIssues.get(i);
            if (existingIssue.getId() != null && existingIssue.getId().equals(issue.getId())) {
                // Оновлюємо всі поля існуючої задачі
                existingIssue.setTitle(issue.getTitle());
                existingIssue.setDescription(issue.getDescription());
                existingIssue.setStatus(issue.getStatus());
                existingIssue.setPriority(issue.getPriority());
                existingIssue.setAssignee(issue.getAssignee());
                existingIssue.setReporter(issue.getReporter());
                existingIssue.setKey(issue.getKey());
                
                System.out.println("BoardService.updateIssue: оновлено задачу в глобальному списку ID=" + issue.getId() + 
                    ", назва='" + issue.getTitle() + "', статус=" + issue.getStatus() +
                    ", опис=" + (issue.getDescription() != null ? 
                       issue.getDescription().substring(0, Math.min(30, issue.getDescription().length())) + "..." : "null"));
                
                found = true;
                break;
            }
        }
        
        // Якщо задачі немає в глобальному списку, додаємо її
        if (!found) {
            System.out.println("BoardService.updateIssue: задача з ID " + issue.getId() + 
                " не знайдена в глобальному списку, додаємо її");
            addToGlobalIssuesList(issue);
        }
    }
    
    /**
     * Оновлює задачу на основі UI моделі
     */
    public void updateIssue(Issue issue, Object issueModel, User currentUser) {
        // Обновление задачи по модели UI
        updateIssue(issue);
    }
    
    /**
     * Створює нову задачу на дошці
     * 
     * @param board    дошка, на якій створюється задача
     * @param reporter користувач, створюючий задачу
     * @param assignee виконавець задачі
     * @param issueModel модель задачі для створення
     * @return створена задача
     */
    public Issue createIssue(Board board, User reporter, User assignee, Object issueModel) {
        // Генеруємо унікальний ID для нової задачі
        long id = generateUniqueId();
        
        // Перевіряємо, чи нам приходить об'єкт Issue напряму
        if (issueModel instanceof Issue) {
            Issue issue = (Issue) issueModel;
            System.out.println("BoardService.createIssue: тип моделі задачі: " + issueModel.getClass().getName());
            
            // Устанавливаем ID, якщо його немає
            if (issue.getId() == null) {
                issue.setId(id);
            }
            
            // Устанавливаем зв'язки
            issue.setBoard(board);
            issue.setProject(board.getProject());
            
            // Устанавливаем автора і виконавника, якщо вони не установлені
            if (issue.getReporter() == null) {
                issue.setReporter(reporter);
            }
            
            if (issue.getAssignee() == null && assignee != null) {
                System.out.println("BoardService.createIssue: установлен assignee " + assignee.getFullName());
                issue.setAssignee(assignee);
            }
            
            // Устанавливаем статус за замовчуванням, якщо він не установлений
            if (issue.getStatus() == null) {
                issue.setStatus(Status.TO_DO);
            }
            
            // Генеруємо ключ задачі в форматі PROJECT_KEY-ISSUE_ID
            if (issue.getKey() == null) {
                String projectKey = board.getProject().getKey();
                issue.setKey(projectKey + "-" + id);
            }
            
            System.out.println("BoardService.createIssue: додаю задачу до глобального списку з ID " + 
                issue.getId() + ", назвою '" + issue.getTitle() + "' та статусом " + issue.getStatus());
            
            // Додаємо задачу в глобальний список
            addToGlobalIssuesList(issue);
            
            return issue;
        }
        // Якщо тип моделі не Issue, спробуємо використовувати рефлексію для отримання даних
        else if (issueModel != null) {
            // Створюємо нову задачу
            Issue issue = new Issue();
            issue.setId(id);
            
            try {
                // Отримуємо клас моделі
                Class<?> modelClass = issueModel.getClass();
                
                // Спробуємо отримати заголовок
                try {
                    java.lang.reflect.Method getTitleMethod = modelClass.getMethod("getTitle");
                    String title = (String) getTitleMethod.invoke(issueModel);
                    issue.setTitle(title);
                } catch (Exception e) {
                    // Якщо не вдалося отримати заголовок, устанавливаємо за замовчуванням
                    issue.setTitle("Нова задача " + id);
                }
                
                // Спробуємо отримати опис
                try {
                    java.lang.reflect.Method getDescriptionMethod = modelClass.getMethod("getDescription");
                    String description = (String) getDescriptionMethod.invoke(issueModel);
                    issue.setDescription(description);
                } catch (Exception e) {
                    // Ігноруємо помилку
                }
                
                // Спробуємо отримати статус
                try {
                    java.lang.reflect.Method getStatusMethod = modelClass.getMethod("getStatus");
                    Status status = (Status) getStatusMethod.invoke(issueModel);
                    if (status != null) {
                        issue.setStatus(status);
                    } else {
                        issue.setStatus(Status.TO_DO);
                    }
                } catch (Exception e) {
                    issue.setStatus(Status.TO_DO);
                }
                
                // Спробуємо отримати приоритет
                try {
                    java.lang.reflect.Method getPriorityMethod = modelClass.getMethod("getPriority");
                    Priority priority = (Priority) getPriorityMethod.invoke(issueModel);
                    if (priority != null) {
                        issue.setPriority(priority);
                    } else {
                        issue.setPriority(Priority.MEDIUM);
                    }
                } catch (Exception e) {
                    issue.setPriority(Priority.MEDIUM);
                }
            } catch (Exception e) {
                System.err.println("BoardService.createIssue: помилка при роботі з рефлексией: " + e.getMessage());
                // Устанавливаем значення за замовчуванням
                issue.setTitle("Нова задача " + id);
                issue.setStatus(Status.TO_DO);
                issue.setPriority(Priority.MEDIUM);
            }
            
            // Устанавливаем загальні поля не залежно від успіху рефлексии
            issue.setBoard(board);
            issue.setProject(board.getProject());
            issue.setReporter(reporter);
            
            if (assignee != null) {
                issue.setAssignee(assignee);
            }
            
            // Генеруємо ключ задачі
            String projectKey = board.getProject().getKey();
            issue.setKey(projectKey + "-" + id);
            
            // Додаємо задачу в глобальний список
            addToGlobalIssuesList(issue);
            
            return issue;
        }
        
        // Якщо модель не визначена, створюємо пусту задачу
        System.out.println("BoardService.createIssue: модель не визначена, створюю задачу за замовчуванням");
        
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Нова задача " + id);
        issue.setDescription("Опис нової задачі");
        issue.setStatus(Status.TO_DO);
        issue.setPriority(Priority.MEDIUM);
        
        // Устанавливаем зв'язки
        issue.setBoard(board);
        issue.setProject(board.getProject());
        issue.setReporter(reporter);
        
        if (assignee != null) {
            issue.setAssignee(assignee);
        }
        
        // Генеруємо ключ задачі
        String projectKey = board.getProject().getKey();
        issue.setKey(projectKey + "-" + id);
        
        // Додаємо задачу в глобальний список
        addToGlobalIssuesList(issue);
        
        return issue;
    }
    
    /**
     * Генерує унікальний ID для нової задачі
     */
    private long generateUniqueId() {
        // Находим максимальний ID серед існуючих задач
        long maxId = 100;
        for (Issue issue : globalIssues) {
            if (issue.getId() != null && issue.getId() > maxId) {
                maxId = issue.getId();
            }
        }
        // Повертаємо ID на 1 більше максимального
        return maxId + 1;
    }
    
    /**
     * Оновлює статус задачі
     */
    public void updateIssueStatus(Long issueId, Status status) {
        // Перевіряємо глобальний список задач
        for (Issue issue : globalIssues) {
            if (issue.getId().equals(issueId)) {
                System.out.println("BoardService.updateIssueStatus: оновлюємо статус задачі " + 
                    issue.getTitle() + " з " + issue.getStatus() + " на " + status);
                issue.setStatus(status);
                return;
            }
        }
        
        // Якщо задачу не знайшли
        System.out.println("BoardService.updateIssueStatus: задачу з ID " + issueId + " не знайдено");
    }
    
    /**
     * Оновлює статус задачі (перегруженная версія для роботи з об'єктом Issue)
     */
    public void updateIssueStatus(Issue issue, Status status) {
        if (issue == null) {
            System.out.println("BoardService.updateIssueStatus: задача є null");
            return;
        }
        
        if (issue.getId() == null) {
            System.out.println("BoardService.updateIssueStatus: задача не має ID");
            issue.setStatus(status);
            return;
        }
        
        System.out.println("BoardService.updateIssueStatus: оновлюємо статус задачі " + 
            issue.getTitle() + " (ID=" + issue.getId() + ") з " + issue.getStatus() + " на " + status);
        
        // Встановлюємо статус для поточного об'єкту
        issue.setStatus(status);
        
        // Також оновлюємо статус в глобальному списку
        updateIssueStatus(issue.getId(), status);
    }
    
    /**
     * Перевіряє, чи має текучий користувач право редагувати конкретну задачу
     * 
     * @param issueId ID задачі
     * @return true, якщо користувач має право редагувати задачу, false в противному випадку
     */
    public boolean canUserEditIssue(Long issueId) {
        // Отримуємо екземпляр сервісу аутентифікації
        AuthService authServiceInstance = AuthService.getInstance();
        
        User currentUser = authServiceInstance.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Адміністратор може редагувати будь-які задачі
        if (currentUser.getRole() == ua.oip.jiralite.domain.enums.Role.ADMIN) {
            return true;
        }
        
        // Гість не може редагувати будь-які задачі
        if (currentUser.getRole() == ua.oip.jiralite.domain.enums.Role.GUEST) {
            return false;
        }
        
        // Користувач може редагувати тільки свої задачі
        // Для цього шукаємо задачу в глобальному списку
        for (Issue issue : globalIssues) {
            if (issue.getId() != null && issue.getId().equals(issueId)) {
                // Перевіряємо, призначена чи задача текучому користувачу
                if (issue.getAssignee() != null && 
                    issue.getAssignee().getId() != null && 
                    currentUser.getId() != null &&
                    issue.getAssignee().getId().equals(currentUser.getId())) {
                    return true;
                }
                // Або користувач є автором задачі
                if (issue.getReporter() != null && 
                    issue.getReporter().getId() != null && 
                    currentUser.getId() != null &&
                    issue.getReporter().getId().equals(currentUser.getId())) {
                    return true;
                }
                return false;
            }
        }
        
        // За замовчуванням забороняємо редагування
        return false;
    }
    
    /**
     * Перевіряє, чи має текучий користувач право редагувати конкретну задачу
     * 
     * @param issue задача
     * @return true, якщо користувач має право редагувати задачу, false в противному випадку
     */
    public boolean canUserEditIssue(Issue issue) {
        if (issue == null || issue.getId() == null) {
            return false;
        }
        return canUserEditIssue(issue.getId());
    }
    
    /**
     * Додає задачу в глобальний список, запобігаючи дублікатам
     */
    private static void addToGlobalIssuesList(Issue issue) {
        if (issue == null) {
            System.out.println("BoardService.addToGlobalIssuesList: задача null, не додаємо");
            return;
        }
        
        System.out.println("BoardService.addToGlobalIssuesList: перед додаванням globalIssues.size = " + globalIssues.size());
        
        // Переконуємося, що задача дійсно має ID
        if (issue.getId() == null) {
            System.out.println("BoardService.addToGlobalIssuesList: задача не має ID, генеруємо новий");
            // Генеруємо унікальний ID, не перекриваючись з базовими демо-задачами (ID 1-3)
            long maxId = 3;
            for (Issue existing : globalIssues) {
                if (existing.getId() != null && existing.getId() > maxId) {
                    maxId = existing.getId();
                }
            }
            issue.setId(maxId + 1);
            System.out.println("BoardService.addToGlobalIssuesList: згенеровано ID " + issue.getId());
        }
        
        // Переконуємося, що задача має статус перед додаванням
        if (issue.getStatus() == null) {
            System.out.println("BoardService.addToGlobalIssuesList: задача не має статусу, встановлюємо TO_DO");
            issue.setStatus(Status.TO_DO);
        }
        
        // Переконуємося, що задача має ключ та назву
        if (issue.getKey() == null && issue.getProject() != null) {
            String projectKey = issue.getProject().getKey();
            issue.setKey(projectKey + "-" + issue.getId());
            System.out.println("BoardService.addToGlobalIssuesList: згенеровано ключ " + issue.getKey() + " для задачі з ID " + issue.getId());
        }
        
        if (issue.getTitle() == null || issue.getTitle().isEmpty()) {
            issue.setTitle("Нова задача " + issue.getId());
            System.out.println("BoardService.addToGlobalIssuesList: встановлено назву за замовчуванням: " + issue.getTitle());
        }
        
        // Перевіряємо, чи не існує вже задача з таким ID
        boolean updated = false;
        for (int i = 0; i < globalIssues.size(); i++) {
            if (globalIssues.get(i).getId().equals(issue.getId())) {
                // Задача з таким ID вже існує
                Issue existingIssue = globalIssues.get(i);
                System.out.println("BoardService.addToGlobalIssuesList: знайдено існуючу задачу з ID " + issue.getId() + 
                    ", статус = " + existingIssue.getStatus());
                
                // Копируем все поля из новой задачи в существующую
                if (issue.getTitle() != null) existingIssue.setTitle(issue.getTitle());
                if (issue.getDescription() != null) existingIssue.setDescription(issue.getDescription());
                if (issue.getStatus() != null) {
                    System.out.println("BoardService.addToGlobalIssuesList: оновлюємо статус з " + 
                        existingIssue.getStatus() + " на " + issue.getStatus());
                    existingIssue.setStatus(issue.getStatus());
                }
                if (issue.getPriority() != null) existingIssue.setPriority(issue.getPriority());
                if (issue.getAssignee() != null) existingIssue.setAssignee(issue.getAssignee());
                if (issue.getProject() != null) existingIssue.setProject(issue.getProject());
                if (issue.getKey() != null) existingIssue.setKey(issue.getKey());
                
                System.out.println("BoardService.addToGlobalIssuesList: оновлено задачу з ID " + issue.getId() + 
                    ", новий статус = " + existingIssue.getStatus());
                
                updated = true;
                break;
            }
        }
        
        // Якщо задачі з таким ID немає, додаємо нову
        if (!updated) {
            System.out.println("BoardService.addToGlobalIssuesList: додаю нову задачу з ID " + issue.getId() + 
                ", статусом " + issue.getStatus() + " та назвою '" + issue.getTitle() + "'");
            globalIssues.add(issue);
        }
        
        System.out.println("BoardService.addToGlobalIssuesList: після додавання globalIssues.size = " + globalIssues.size());
    }
} 