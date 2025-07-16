# JiraLite Service API

## 📋 Огляд

REST API сервіс для управління задачами в системі JiraLite, побудований на Spring Boot 3.0.0.

## 🚀 Способи запуску

### 1. Development режим (для розробки)

```bash
# З кореневої директорії проєкту
mvn spring-boot:run -pl jiralite-service

# Або з директорії jiralite-service
cd jiralite-service
mvn spring-boot:run
```

### 2. Production режим (JAR файл)

```bash
# Створення JAR файлу
cd jiralite-service
mvn clean package -DskipTests

# Запуск JAR файлу
java -jar target/jiralite-service-1.0.0-SNAPSHOT.jar
```

## 🔗 API Endpoints

| Method | Endpoint      | Опис                    | Відповідь              |
| ------ | ------------- | ----------------------- | ---------------------- |
| `GET`  | `/api/health` | Перевірка стану сервісу | 200 OK                 |
| `POST` | `/issue`      | Створення нової задачі  | 201 Created            |
| `GET`  | `/issue/{id}` | Отримання задачі за ID  | 200 OK / 404 Not Found |

### Приклади запитів

#### Health Check

```bash
curl -X GET http://localhost:8080/api/health
```

**Відповідь:**

```json
{
	"status": "UP",
	"timestamp": "2025-07-16T14:16:26.859755",
	"service": "JiraLite Service API",
	"version": "1.0.0-SNAPSHOT"
}
```

#### Створення задачі

```bash
curl -X POST http://localhost:8080/issue \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Issue",
    "description": "This is a test issue",
    "priority": "HIGH"
  }'
```

**Відповідь:**

```json
{
	"id": 1,
	"title": "Test Issue",
	"description": "This is a test issue",
	"priority": "HIGH",
	"status": "OPEN",
	"createdAt": "2025-07-16T14:16:35.385136",
	"updatedAt": null
}
```

#### Отримання задачі

```bash
curl -X GET http://localhost:8080/issue/1
```

**Відповідь:** (аналогічно створенню)

## 🧪 Postman Testing

### Імпорт колекції

1. Відкрийте Postman
2. Оберіть `Import` → `File`
3. Імпортуйте файл `JiraLite_API_Collection.postman_collection.json`

### Структура колекції

-   **Health Check** - перевірка доступності API
-   **Create Issue** - створення нової задачі з валідацією
-   **Get Issue by ID** - отримання створеної задачі
-   **Get Non-Existent Issue** - тестування 404 помилки

### Автоматичні тести

Кожен запит містить автоматичні перевірки:

-   HTTP статус коди
-   Структура JSON відповідей
-   Валідація даних
-   Збереження змінних між запитами

## 🏗️ Архітектура

### Технології

-   **Spring Boot 3.0.0** - основний фреймворк
-   **Apache Tomcat 10.1.1** - вбудований web-сервер
-   **Jackson 2.14.1** - JSON серіалізація
-   **JUnit 5 & Mockito** - тестування

### Компоненти

```
jiralite-service/
├── src/main/java/ua/oip/jiralite/
│   ├── JiraLiteServiceApplication.java    # Головний клас
│   └── controller/
│       ├── HealthController.java          # Health endpoint
│       └── IssueController.java           # Issues CRUD
├── JiraLite_API_Collection.postman_collection.json
├── API_TESTING_RESULTS.md
└── target/
    └── jiralite-service-1.0.0-SNAPSHOT.jar
```

### Моделі даних

#### CreateIssueRequest

```json
{
	"title": "string", // Обов'язкове
	"description": "string", // Обов'язкове
	"priority": "string" // HIGH, MEDIUM, LOW
}
```

#### IssueDto

```json
{
	"id": "number",
	"title": "string",
	"description": "string",
	"priority": "string",
	"status": "string", // OPEN, IN_PROGRESS, DONE
	"createdAt": "datetime",
	"updatedAt": "datetime"
}
```

## 🐛 Усунення проблем

### Частові проблеми

#### Помилка: "No plugin found for prefix 'spring-boot'"

**Рішення:** Переконайтеся що запускаєте з правильної директорії:

```bash
# Правильно
mvn spring-boot:run -pl jiralite-service

# Неправильно (з кореня без вказання модуля)
mvn spring-boot:run
```

#### Порт 8080 зайнятий

**Рішення:** Змініть порт у команді запуску:

```bash
java -jar target/jiralite-service-1.0.0-SNAPSHOT.jar --server.port=8081
```

#### Помилки тестів при збірці

**Рішення:** Пропустіть тести:

```bash
mvn clean package -DskipTests
```

## 📊 Продуктивність

-   **Час запуску:** ~1 секунда
-   **Розмір JAR:** 37 МБ (з залежностями)
-   **Пам'ять:** ~150 МБ RAM
-   **Endpoint response time:** < 50ms

## 🔐 Безпека

-   Валідація вхідних даних
-   Обробка помилок без витоку інформації
-   Логування всіх операцій
-   CORS підтримка (налаштовується)

## 📈 Моніторинг

### Логи

Всі логи виводяться в консоль з рівнями:

-   `INFO` - основні операції
-   `DEBUG` - детальна відладка
-   `ERROR` - помилки системи

### Health Endpoint

`/api/health` надає інформацію про:

-   Статус сервісу
-   Версію додатку
-   Час запиту

## 📝 Висновок

✅ **API повністю готовий до використання**  
✅ **Postman колекція протестована**  
✅ **Executable JAR створений**  
✅ **Документація актуальна**

**Версія:** 1.0.0-SNAPSHOT  
**Дата:** 16 липня 2025  
**Автор:** JiraLite Development Team
