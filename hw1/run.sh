#!/bin/bash
set -e  # остановить при любой ошибке

echo "Компиляция проекта..."
mvn clean compile

echo "Сборка jar-файла..."
mvn package

JAR_FILE=$(ls target/*.jar | grep -v original | head -n 1)

echo "Запуск приложения..."
java -jar "$JAR_FILE"
