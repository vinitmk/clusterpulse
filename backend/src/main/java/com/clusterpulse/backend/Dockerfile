# syntax=docker/dockerfile:1

# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the Spring Boot fat jar from build stage
COPY --from=build /app/target/*.jar app.jar

# JVM options for container awareness
ENV JAVA_OPTS="-XX:+UseContainerSupport"

# Expose application port
EXPOSE 8080

# Healthcheck: Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

# Run the application
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar