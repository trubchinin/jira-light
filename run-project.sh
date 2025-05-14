#!/bin/bash

echo "Компиляция и запуск проекта Jira Lite..."

# Создаем директории для скомпилированных классов
mkdir -p build/{domain,service,ui}

# Путь к зависимостям
LIBS=lib/slf4j-api-1.7.36.jar:lib/slf4j-simple-1.7.36.jar:lib/jakarta-persistence-api.jar

# Компилируем модуль domain
echo "Компиляция модуля domain..."
javac -d build/domain -cp $LIBS \
    jiralite-domain/src/main/java/ua/oip/jiralite/domain/*.java \
    jiralite-domain/src/main/java/ua/oip/jiralite/domain/enums/*.java

if [ $? -ne 0 ]; then
  echo "Ошибка компиляции модуля domain."
  exit 1
fi

# Компилируем модуль service
echo "Компиляция модуля service..."
javac -cp build/domain:$LIBS -d build/service \
    jiralite-service/src/main/java/ua/oip/jiralite/service/*.java \
    jiralite-service/src/main/java/ua/oip/jiralite/repository/*.java

if [ $? -ne 0 ]; then
  echo "Ошибка компиляции модуля service."
  exit 1
fi

# Компилируем модуль ui
echo "Компиляция модуля ui..."
javac -cp build/domain:build/service:$LIBS -d build/ui \
    jiralite-ui/src/main/java/ua/oip/jiralite/ui/*.java \
    jiralite-ui/src/main/java/ua/oip/jiralite/ui/frame/*.java \
    jiralite-ui/src/main/java/ua/oip/jiralite/ui/panel/*.java \
    jiralite-ui/src/main/java/ua/oip/jiralite/ui/model/*.java \
    jiralite-ui/src/main/java/ua/oip/jiralite/ui/util/*.java \
    jiralite-ui/src/main/java/ua/oip/jiralite/ui/listener/*.java

if [ $? -ne 0 ]; then
  echo "Ошибка компиляции модуля ui."
  exit 1
fi

# Копируем ресурсы
echo "Копирование ресурсов..."
mkdir -p build/ui/i18n
cp jiralite-ui/src/main/resources/i18n/*.properties build/ui/i18n/

# Запускаем приложение
echo "Запуск приложения..."
java -cp build/domain:build/service:build/ui:$LIBS ua.oip.jiralite.ui.Launcher 