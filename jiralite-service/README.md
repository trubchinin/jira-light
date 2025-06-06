# Модуль jiralite-service

Модуль `jiralite-service` містить сервісні класи, що реалізують бізнес-логіку для системи управління задачами Jira Lite.

## Структура

### Основні сервіси:

-   `AuthService` - сервіс авторизації, автентифікації та управління користувачами
-   `BoardService` - сервіс для управління дошками та пов'язаними з ними проектами
-   `IssueService` - сервіс для управління задачами та їхніми статусами

## Архітектура

Сервісний шар реалізує патерн Singleton для більшості сервісів, забезпечуючи єдину точку доступу до бізнес-логіки.

Взаємодія з даними здійснюється через доменні об'єкти з модуля `jiralite-domain`.

## Залежності

Модуль залежить від:

-   `jiralite-domain` - доменні об'єкти, з якими працюють сервіси
-   Бібліотек логування (SLF4J + Logback) для ведення журналу операцій

## Використання

Сервіси використовуються в UI-шарі для:

-   Авторизації користувачів
-   Відображення списку проектів та дошок
-   Управління задачами (створення, редагування, переміщення)
-   Роботи з Kanban-дошкою
