# -------------------------------------------------------------
# Stage 1: Build the Application with Maven & OpenJDK 21
# -------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and pre-fetch dependencies for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package the Spring Boot executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# -------------------------------------------------------------
# Stage 2: Production Runtime with Java 21 & Native Tesseract OCR
# -------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install native Tesseract OCR libraries for Linux container ANPR support
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    tesseract-ocr \
    libtesseract-dev \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*

# Copy built JAR from build stage
COPY --from=build /app/target/smart-parking-system-1.0.0.jar app.jar

# Dynamic Port Binding (Railway injects $PORT)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
