# Этап 1: Сборка зависимостей
FROM maven:3.8.6-eclipse-temurin-17 AS dependencies

WORKDIR /opt/app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Этап 2: Сборка приложения
FROM maven:3.8.6-eclipse-temurin-17 AS builder

WORKDIR /opt/app
COPY --from=dependencies /root/.m2 /root/.m2
COPY . .
RUN mvn package -DskipTests

# Этап 3: Запуск приложения
FROM eclipse-temurin:17-jre-jammy

WORKDIR /opt/app
EXPOSE 37111
COPY --from=builder /opt/app/target/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]