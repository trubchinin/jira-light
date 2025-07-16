# Результати тестування JiraLite API

## Огляд

Цей документ містить результати тестування REST API сервісу JiraLite. Всі тести виконано успішно з кодами відповіді 200 OK та 201 Created.

## API Endpoints

### 1. Health Check Endpoint

**Endpoint:** `GET /api/health`  
**URL:** http://localhost:8080/api/health  
**Очікуваний результат:** 200 OK

**Відповідь сервера:**

```json
{
	"version": "1.0.0-SNAPSHOT",
	"status": "UP",
	"service": "JiraLite Service API",
	"timestamp": "2025-07-16T14:16:26.859755"
}
```

**HTTP Status:** ✅ **200 OK**

---

### 2. Create Issue Endpoint

**Endpoint:** `POST /issue`  
**URL:** http://localhost:8080/issue  
**Content-Type:** application/json  
**Очікуваний результат:** 201 Created

**Тіло запиту:**

```json
{
	"title": "Test Issue",
	"description": "This is a test issue",
	"priority": "HIGH"
}
```

**Відповідь сервера:**

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

**HTTP Status:** ✅ **201 Created**

---

### 3. Get Issue by ID Endpoint

**Endpoint:** `GET /issue/{id}`  
**URL:** http://localhost:8080/issue/1  
**Очікуваний результат:** 200 OK

**Відповідь сервера:**

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

**HTTP Status:** ✅ **200 OK**

---

### 4. Get Non-Existent Issue

**Endpoint:** `GET /issue/{id}`  
**URL:** http://localhost:8080/issue/999  
**Очікуваний результат:** 404 Not Found

**Відповідь сервера:** (порожнє тіло)

**HTTP Status:** ✅ **404 Not Found**

---

## Технічні деталі

### Архітектура API

-   **Фреймворк:** Spring Boot 3.0.0
-   **Сервер:** Apache Tomcat 10.1.1 (вбудований)
-   **Порт:** 8080
-   **Формат даних:** JSON
-   **Сховище:** In-memory (ConcurrentHashMap)

### Функціональність

1. **Health Check** - перевірка стану сервісу
2. **Issue Management** - створення та отримання задач
3. **Error Handling** - коректна обробка помилок (404)
4. **Data Validation** - валідація вхідних даних
5. **JSON Serialization** - автоматична серіалізація/десеріалізація

### Postman Collection

Створено повну колекцію тестів у файлі `JiraLite_API_Collection.postman_collection.json` що включає:

-   Автоматичні тести для перевірки HTTP статусів
-   Валідація структури відповідей
-   Збереження змінних між запитами
-   Тестування всіх сценаріїв використання

## Висновок

✅ **Всі API endpoints працюють коректно**  
✅ **HTTP статуси відповідають очікуванням**  
✅ **JSON структури валідні**  
✅ **Обробка помилок реалізована**  
✅ **Postman колекція готова для імпорту**

**Дата тестування:** 16 липня 2025  
**Версія API:** 1.0.0-SNAPSHOT  
**Тестувальник:** JiraLite Development Team
