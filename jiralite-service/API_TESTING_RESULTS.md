# JiraLite API Testing Results

## Обзор

Данный документ содержит результаты тестирования REST API сервиса JiraLite. Все тесты выполнены успешно с кодами ответа 200 OK и 201 Created.

## API Endpoints

### 1. Health Check Endpoint

**Endpoint:** `GET /api/health`  
**URL:** http://localhost:8080/api/health  
**Ожидаемый результат:** 200 OK

**Ответ сервера:**

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
**Ожидаемый результат:** 201 Created

**Тело запроса:**

```json
{
	"title": "Test Issue",
	"description": "This is a test issue",
	"priority": "HIGH"
}
```

**Ответ сервера:**

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
**Ожидаемый результат:** 200 OK

**Ответ сервера:**

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
**Ожидаемый результат:** 404 Not Found

**Ответ сервера:** (пустое тело)

**HTTP Status:** ✅ **404 Not Found**

---

## Технические детали

### Архитектура API

-   **Фреймворк:** Spring Boot 3.0.0
-   **Сервер:** Apache Tomcat 10.1.1 (embedded)
-   **Порт:** 8080
-   **Формат данных:** JSON
-   **Хранилище:** In-memory (ConcurrentHashMap)

### Функциональность

1. **Health Check** - проверка состояния сервиса
2. **Issue Management** - создание и получение задач
3. **Error Handling** - корректная обработка ошибок (404)
4. **Data Validation** - валидация входящих данных
5. **JSON Serialization** - автоматическая сериализация/десериализация

### Postman Collection

Создана полная коллекция тестов в файле `JiraLite_API_Collection.postman_collection.json` включающая:

-   Автоматические тесты для проверки HTTP статусов
-   Валидация структуры ответов
-   Сохранение переменных между запросами
-   Тестирование всех сценариев использования

## Заключение

✅ **Все API endpoints работают корректно**  
✅ **HTTP статусы соответствуют ожиданиям**  
✅ **JSON структуры валидны**  
✅ **Обработка ошибок реализована**  
✅ **Postman коллекция готова для импорта**

**Дата тестирования:** 16 июля 2025  
**Версия API:** 1.0.0-SNAPSHOT  
**Тестировщик:** JiraLite Development Team
