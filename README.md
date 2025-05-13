# Jira Lite

Проста система керування завданнями, натхненна Jira.

## Вимоги

-   Java 17 або вище
-   Maven 3.6 або вище
-   PostgreSQL 12 або вище

## Команди для швидкого старту

```bash
# Створення скелету (якщо потрібно)
mvn archetype:generate -DgroupId=ua.oip -DartifactId=jira-lite -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

# Завантаження залежностей та збірка
mvn clean package

# Запуск unit-тестів
mvn test

# Пускова точка (LoginFrame)
java -cp target/jira-lite-1.0.0-SNAPSHOT.jar ua.oip.jiralite.ui.LoginFrame
```

## Структура проекту

Проект використовує стандартну Maven-структуру:

```
jira-lite/
 ├─ pom.xml
 ├─ src/
 │  ├─ main/
 │  │  ├─ java/
 │  │  │  └─ ua/
 │  │  │     └─ oip/
 │  │  │        └─ jiralite/
 │  │  │           ├─ domain/              # класи предметної області
 │  │  │           ├─ repository/          # DAO-інтерфейси та їх реалізації
 │  │  │           ├─ service/             # бізнес-логіка
 │  │  │           └─ ui/                  # Swing-інтерфейс
 │  │  └─ resources/
 │  │      ├─ META-INF/
 │  │      │   └─ persistence.xml          # налаштування JPA/Hibernate
 │  │      └─ logback.xml                  # конфігурація логування
 │  └─ test/
 │     ├─ java/
 │     │   └─ ua.oip.jiralite/
 │     │       ├─ domain/
 │     │       │   └─ IssueTest.java
 │     │       └─ service/
 │     │           └─ BoardServiceTest.java
 │     └─ resources/
 │         └─ test-postgres.properties     # налаштування для тестів
```

## Налаштування бази даних

Перед запуском додатку необхідно створити базу даних PostgreSQL:

```sql
CREATE DATABASE jiralite;
CREATE USER jiralite WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE jiralite TO jiralite;
```

## Можливості системи

-   Керування проектами
-   Створення та редагування завдань
-   Коментування завдань
-   Керування користувачами та правами доступу
-   Kanban-дошка для візуалізації робочого процесу

## Вирішення проблем із залежностями

Якщо у вас виникають помилки компіляції, пов'язані з відсутністю залежностей (jakarta.persistence, org.slf4j тощо), виконайте наступні кроки:

1. Переконайтеся, що у вас встановлено Java 17 або вище:

    ```bash
    java -version
    ```

2. Використовуйте Maven або Maven Wrapper для збірки проекту:

    ```bash
    # Якщо Maven встановлено
    mvn clean package -DskipTests

    # Якщо Maven не встановлено, використовуйте Maven Wrapper
    chmod +x mvnw      # Тільки для Unix/Linux/Mac
    ./mvnw clean package -DskipTests
    ```

3. Запустіть додаток:

    ```bash
    # Через Maven
    mvn exec:java -Dexec.mainClass="ua.oip.jiralite.JiraLiteApp"

    # Або через Java напряму
    java -cp target/jira-lite-1.0.0-SNAPSHOT.jar:target/lib/* ua.oip.jiralite.JiraLiteApp
    ```

4. Альтернативно, можна запустити додаток через LoginFrame:

    ```bash
    # Через Maven
    mvn exec:java -Dexec.mainClass="ua.oip.jiralite.ui.LoginFrame"

    # Або через Java напряму
    java -cp target/jira-lite-1.0.0-SNAPSHOT.jar:target/lib/* ua.oip.jiralite.ui.LoginFrame
    ```

## Важлива інформація щодо міграції

У проекті здійснено міграцію з javax.persistence на jakarta.persistence (JPA 3.x), що вимагає:

1. Заміни всіх імпортів javax.persistence._ на jakarta.persistence._
2. Оновлення persistence.xml до версії 3.0 схеми
3. Використання HikariCP для пулу з'єднань
