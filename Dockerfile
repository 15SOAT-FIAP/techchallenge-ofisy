# ===== BUILD =====
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# ===== RUNTIME =====
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

ADD --chown=app:app https://dtdg.co/latest-java-tracer dd-java-agent.jar
COPY --chown=app:app --from=build /app/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/app/dd-java-agent.jar", "-jar", "app.jar"]