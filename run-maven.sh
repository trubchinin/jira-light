#!/bin/bash

echo "Очистка и сборка проекта..."
./mvnw clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "Запуск приложения..."
    ./mvnw exec:java -pl jiralite-ui
else
    echo "Ошибка при сборке проекта"
fi 