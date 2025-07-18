# Use OpenJDK 21 with Alpine for smaller image size
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy Maven wrapper and root pom.xml first for better layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy all module pom.xml files for dependency resolution
COPY shared/pom.xml ./shared/
COPY model/pom.xml ./model/
COPY infra/pom.xml ./infra/
COPY core/pom.xml ./core/
COPY security/pom.xml ./security/
COPY web/pom.xml ./web/
COPY tests/pom.xml ./tests/

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml files don't change)
RUN ./mvnw dependency:go-offline -B

# Copy all module source code
COPY shared/src ./shared/src
COPY model/src ./model/src
COPY infra/src ./infra/src
COPY core/src ./core/src
COPY security/src ./security/src
COPY web/src ./web/src
COPY tests/src ./tests/src

# Build the application (web module will contain the executable JAR)
RUN ./mvnw clean package -DskipTests

# Create a new stage for the runtime image (multi-stage build for smaller final image)
#FROM openjdk:21-jre-slim
FROM openjdk:21-jdk-slim

WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built JAR from the web module (the entry point)
COPY --from=0 /app/web/target/*.jar app.jar

# Change ownership to spring user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose the port your Spring Boot app runs on
EXPOSE 8088

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8088/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]