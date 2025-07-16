# 🐛 Bug Report: JIR-29 - Critical NullPointerException in Drag-and-Drop Operations

## 📋 **Issue Summary**

**Issue ID:** JIR-29  
**Title:** Реалізувати перетягування задач (drag-and-drop)  
**Type:** Bug (Incident)  
**Priority:** High  
**Severity:** Critical  
**Reporter:** Нікіта Трубчінін  
**Assignee:** Development Team  
**Created:** 2024-12-19  
**Sprint:** Sprint 3 (UI Implementation)

---

## 🎯 **Issue Description**

### **Проблема:**

Під час реалізації функціоналу drag-and-drop для переміщення задач між колонками дошки виникають критичні помилки `NullPointerException`, які призводять до краху додатка.

### **Вплив на систему:**

-   🚫 Повний крах UI при спробі перетягування задач
-   🚫 Неможливість використання основного функціоналу Kanban дошки
-   🚫 Втрата даних користувача при неочікуваному закритті
-   🚫 Негативний досвід користувача та втрата продуктивності

---

## 🔍 **Детальний опис симптомів**

### **Сценарій 1: NPE в Board.toString()**

```java
Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException
    at ua.oip.jiralite.domain.Board.toString(Board.java:98)
    at ua.oip.jiralite.ui.panel.BoardColumnPanel.updateTitle(BoardColumnPanel.java:156)
    at ua.oip.jiralite.ui.listener.ColumnDropTarget.dragEnter(ColumnDropTarget.java:45)
```

### **Сценарій 2: NPE в User.isActionAllowed()**

```java
Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException
    at ua.oip.jiralite.domain.user.RoleManager.isActionAllowed(RoleManager.java:78)
    at ua.oip.jiralite.domain.User.isActionAllowed(User.java:108)
    at ua.oip.jiralite.ui.listener.IssueCardMouseAdapter.checkDragPermissions(IssueCardMouseAdapter.java:89)
```

---

## 🧪 **Кроки відтворення**

### **Передумови:**

-   Система JiraLite запущена
-   Користувач автентифікований
-   Відкрита дошка проєкту з задачами

### **Сценарій відтворення #1:**

1. 📂 Відкрити проєкт з порожнім/null назвою дошки
2. 🔄 Створити задачу в колонці "TO DO"
3. 🖱️ Розпочати перетягування задачі мишею
4. ⚡ **Результат:** Додаток крашиться з NPE в Board.toString()

### **Сценарій відтворення #2:**

1. 👤 Увійти в систему як звичайний користувач
2. 📋 Відкрити дошку з задачами
3. 🖱️ Спробувати перетягнути задачу передавши null як action
4. ⚡ **Результат:** NPE в User.isActionAllowed() при перевірці прав

### **Додаткові умови:**

-   **Браузер/OS:** Java Swing на Windows/macOS/Linux
-   **Версія JDK:** 11+
-   **Розмір даних:** Будь-який проєкт з ≥1 задачею

---

## ✅ **Очікуваний результат**

-   ✅ Задачі плавно переміщуються між колонками
-   ✅ UI відображає правильну назву дошки навіть при null значеннях
-   ✅ Система коректно обробляє права доступу для drag операцій
-   ✅ Відсутність крашів та виключень

## ❌ **Фактичний результат**

-   ❌ `NullPointerException` при спробі відображення назви дошки
-   ❌ Крах додатка при перевірці прав доступу з null параметрами
-   ❌ Втрата роботи користувача та необхідність перезапуску

---

## 🔧 **Root Cause Analysis**

### **Технічні причини:**

1. **Board.toString()** - відсутня валідація null значень назви дошки
2. **User.isActionAllowed()** - не перевіряються null параметри action
3. **UI Layer** - відсутність defensive programming підходів

### **Код-причини:**

```java
// ❌ Проблемний код в Board.java:98
public String toString() {
    return name; // NPE якщо name = null
}

// ❌ Проблемний код в User.java:108
public boolean isActionAllowed(String action) {
    return RoleManager.isActionAllowed(role.name(), action); // NPE якщо action = null
}
```

---

## 🛠️ **Implemented Solution**

### **Виправлення в Board.java:**

```java
// ✅ Безпечна реалізація
@Override
public String toString() {
    if (name != null && !name.trim().isEmpty()) {
        return name;
    }
    return "Дошка " + (id != null ? id : "без ID");
}
```

### **Виправлення в User.java:**

```java
// ✅ Безпечна реалізація
public boolean isActionAllowed(String action) {
    if (action == null || action.trim().isEmpty()) {
        return false;  // Безпечне повернення для null/порожніх дій
    }
    return role != null && RoleManager.isActionAllowed(role.name(), action);
}
```

---

## 📊 **Статуси та життєвий цикл**

| **Статус**         | **Дата**         | **Коментар**                                    | **Виконавець** |
| ------------------ | ---------------- | ----------------------------------------------- | -------------- |
| 🆕 **TO DO**       | 2024-12-19 09:00 | Баг зареєстровано після тестування UI           | QA Team        |
| 🔄 **IN PROGRESS** | 2024-12-19 10:30 | Розпочато аналіз та виправлення                 | Developer      |
| 🧪 **IN TESTING**  | 2024-12-19 14:15 | Виправлення реалізовано, передано на тестування | Developer      |
| ✅ **DONE**        | 2024-12-19 15:45 | Тестування пройдено, виправлення затверджено    | QA Team        |

---

## 🧪 **Testing Evidence**

### **Тест-кейси для валідації виправлення:**

1. ✅ Створення дошки з null назвою → toString() повертає fallback значення
2. ✅ Виклик isActionAllowed(null) → повертає false без винятків
3. ✅ Drag-and-drop з порожньою назвою дошки → UI працює стабільно
4. ✅ Множинні операції переміщення → відсутність memory leaks

### **Regression Testing:**

-   ✅ Звичайний drag-and-drop функціонал не порушено
-   ✅ Права доступу працюють коректно для валідних параметрів
-   ✅ Performance не погіршено

---

## 🏷️ **Labels and Classification**

**Components:** UI, Domain Model, Security  
**Affects Versions:** v1.0.0-SNAPSHOT  
**Fix Versions:** v1.0.1  
**Keywords:** drag-and-drop, NPE, null-safety, UI-crash

---

## 📈 **Business Impact**

### **До виправлення:**

-   🔴 100% задач з drag-and-drop неможливі
-   🔴 Критичний UX дефект
-   🔴 Втрата продуктивності користувачів

### **Після виправлення:**

-   🟢 Повністю стабільна робота drag-and-drop
-   🟢 Покращена надійність системи
-   🟢 Позитивний користувацький досвід

---

## 📝 **Lessons Learned**

1. **Defensive Programming:** Завжди валідувати null параметри в public методах
2. **UI Testing:** Тестування edge cases для UI компонентів критично важливе
3. **Code Review:** Статичний аналіз коду може виявити такі проблеми раніше
4. **Documentation:** Чітка документація параметрів методів запобігає помилкам

---

**Документ підготовлено:** Development Team  
**Останнє оновлення:** 2024-12-19  
**Статус документу:** ✅ Completed
