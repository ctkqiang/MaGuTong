# ============================================================
# 马股通 - Multi-stage Docker Build
# Author: 钟智强
# ============================================================
# Stage 1: Build the application with Maven
# Stage 2: Run with a slim JRE + Playwright Chromium
# ============================================================

# --------------- Stage 1: Build ---------------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Cache Maven dependencies first (layer caching optimisation)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# --------------- Stage 2: Runtime ---------------
FROM eclipse-temurin:17-jre

LABEL maintainer="钟智强 <zhongzhiqiang>"
LABEL description="BursaScrapper - Bursa Malaysia data scraping service"

WORKDIR /app

# Install ALL Playwright system dependencies (Chromium + Firefox + WebKit)
# Using Ubuntu 25.04 (Resolute) package names where *t64 transition applies
RUN apt-get update && apt-get install -y --no-install-recommends \
    libglib2.0-0t64 \
    libnss3 \
    libnspr4 \
    libdbus-1-3 \
    libatk1.0-0t64 \
    libatk-bridge2.0-0t64 \
    libcups2t64 \
    libdrm2 \
    libxkbcommon0 \
    libatspi2.0-0t64 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libpango-1.0-0 \
    libpangocairo-1.0-0 \
    libcairo2 \
    libcairo-gobject2 \
    libasound2t64 \
    libwayland-client0 \
    libxcursor1 \
    libgtk-3-0t64 \
    libgdk-pixbuf-2.0-0 \
    libx11-xcb1 \
    libxcb1 \
    libxext6 \
    libxi6 \
    libxtst6 \
    fonts-liberation \
    fonts-noto-cjk \
    && rm -rf /var/lib/apt/lists/*

# Copy the fat-jar from builder stage
COPY --from=builder /build/target/magu-tong.jar app.jar

# Install Playwright Chromium browser only (skip Firefox/WebKit to reduce image size)
RUN java -cp app.jar -Dloader.main=com.microsoft.playwright.CLI org.springframework.boot.loader.launch.PropertiesLauncher install --with-deps chromium

# Expose the application port
EXPOSE 8080

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
