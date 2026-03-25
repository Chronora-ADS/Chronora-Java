# Multi-stage build
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache deps first
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
RUN ./mvnw -q -DskipTests dependency:go-offline

# Build
COPY src src
RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user
RUN useradd -r -u 1001 appuser
USER appuser

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080

# Basic JVM memory limits (override via JAVA_TOOL_OPTIONS)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-jar", "app.jar"]
