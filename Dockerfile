# =========================
# Build stage
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy Maven files first (cache optimization)
COPY pom.xml .
# RUN mvn dependency:go-offline -B  # Comment out for debugging

# Copy source and build
COPY src ./src

RUN mvn clean package -DskipTests

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]