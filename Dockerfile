# ===== BUILD =====
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# ===== RUNTIME =====
FROM eclipse-temurin:21-jre

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --chown=app:app --from=build /app/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]