#!/bin/bash

echo "Компиляция проекта..."

# Создание каталогов для скомпилированных классов
mkdir -p target/classes

# Компиляция классов домена
javac -d target/classes jiralite-domain/src/main/java/ua/oip/jiralite/domain/*.java jiralite-domain/src/main/java/ua/oip/jiralite/domain/enums/*.java

# Компиляция сервисов
javac -cp target/classes -d target/classes jiralite-service/src/main/java/ua/oip/jiralite/service/*.java jiralite-service/src/main/java/ua/oip/jiralite/repository/*.java

# Компиляция UI
javac -cp target/classes -d target/classes jiralite-ui/src/main/java/ua/oip/jiralite/ui/*.java jiralite-ui/src/main/java/ua/oip/jiralite/ui/frame/*.java jiralite-ui/src/main/java/ua/oip/jiralite/ui/util/*.java jiralite-ui/src/main/java/ua/oip/jiralite/ui/panel/*.java jiralite-ui/src/main/java/ua/oip/jiralite/ui/listener/*.java jiralite-ui/src/main/java/ua/oip/jiralite/ui/model/*.java

# Копирование ресурсов
mkdir -p target/classes/i18n
cp jiralite-ui/src/main/resources/i18n/*.properties target/classes/i18n/

echo "Запуск приложения..."
java -cp target/classes ua.oip.jiralite.ui.Launcher 