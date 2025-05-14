#!/bin/bash

echo "Сборка проекта Jira Lite..."

# Очистка и сборка проекта
mvn clean package -DskipTests

# Проверяем результат сборки
if [ $? -eq 0 ]; then
    echo "Сборка успешно завершена!"
    echo "Для запуска UI приложения выполните:"
    echo "mvn -pl jiralite-ui exec:java"
    echo ""
    echo "Для запуска тестового примера выполните:"
    echo "./run-test.sh"
else
    echo "При сборке возникли ошибки. Проверьте вывод Maven."
    exit 1
fi 