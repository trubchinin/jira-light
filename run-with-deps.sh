#!/bin/bash

# Установка переменных для зависимостей
DEPS_DIR="lib"
mkdir -p $DEPS_DIR

# Скачиваем необходимые зависимости, если их еще нет
if [ ! -f "$DEPS_DIR/slf4j-api-2.0.9.jar" ]; then
  echo "Скачиваем SLF4J API..."
  curl -L -o "$DEPS_DIR/slf4j-api-2.0.9.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar"
fi

if [ ! -f "$DEPS_DIR/slf4j-simple-2.0.9.jar" ]; then
  echo "Скачиваем SLF4J Simple..."
  curl -L -o "$DEPS_DIR/slf4j-simple-2.0.9.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar"
fi

if [ ! -f "$DEPS_DIR/jakarta.persistence-api-3.1.0.jar" ]; then
  echo "Скачиваем Jakarta Persistence API..."
  curl -L -o "$DEPS_DIR/jakarta.persistence-api-3.1.0.jar" "https://repo1.maven.org/maven2/jakarta/persistence/jakarta.persistence-api/3.1.0/jakarta.persistence-api-3.1.0.jar"
fi

echo "Компиляция проекта..."

# Создание каталогов для скомпилированных классов
mkdir -p target/classes

# Подготовка CLASSPATH
CLASSPATH="$DEPS_DIR/slf4j-api-2.0.9.jar:$DEPS_DIR/slf4j-simple-2.0.9.jar:$DEPS_DIR/jakarta.persistence-api-3.1.0.jar:target/classes"

# Компиляция классов домена
javac -cp "$CLASSPATH" -d target/classes jiralite-domain/src/main/java/ua/oip/jiralite/domain/enums/*.java
javac -cp "$CLASSPATH" -d target/classes jiralite-domain/src/main/java/ua/oip/jiralite/domain/*.java

# Компиляция сервисов и репозиториев
javac -cp "$CLASSPATH" -d target/classes jiralite-service/src/main/java/ua/oip/jiralite/repository/*.java
javac -cp "$CLASSPATH" -d target/classes jiralite-service/src/main/java/ua/oip/jiralite/service/*.java

# Компиляция моделей UI
mkdir -p target/classes/ua/oip/jiralite/ui/model
mkdir -p target/classes/ua/oip/jiralite/ui/util

# Компиляция UI
javac -cp "$CLASSPATH" -d target/classes jiralite-ui/src/main/java/ua/oip/jiralite/ui/util/*.java
javac -cp "$CLASSPATH" -d target/classes jiralite-ui/src/main/java/ua/oip/jiralite/ui/model/*.java
javac -cp "$CLASSPATH" -d target/classes jiralite-ui/src/main/java/ua/oip/jiralite/ui/listener/*.java
javac -cp "$CLASSPATH" -d target/classes jiralite-ui/src/main/java/ua/oip/jiralite/ui/panel/*.java
javac -cp "$CLASSPATH" -d target/classes jiralite-ui/src/main/java/ua/oip/jiralite/ui/frame/*.java
javac -cp "$CLASSPATH" -d target/classes jiralite-ui/src/main/java/ua/oip/jiralite/ui/*.java

# Копирование ресурсов
mkdir -p target/classes/i18n
cp jiralite-ui/src/main/resources/i18n/*.properties target/classes/i18n/

echo "Запуск приложения..."
java -cp "$CLASSPATH" ua.oip.jiralite.ui.Launcher 