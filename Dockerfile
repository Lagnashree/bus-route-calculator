FROM maven:3.8.5-openjdk-18-slim as builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ /app/src/
RUN mvn package
FROM openjdk:17-jdk-alpine
COPY --from=builder /app/target/*.jar /app.jar
CMD ["java", "-jar", "/app.jar"]