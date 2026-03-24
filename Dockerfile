# --- STAGE 1: MÔI TRƯỜNG BUILD ---
FROM eclipse-temurin:25-jdk-alpine as builder
WORKDIR /app

# Cache Gradle
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# Build
COPY src src
RUN ./gradlew build -x test -x spotbugsMain -x spotbugsTest --no-daemon

# --- STAGE 2: MÔI TRƯỜNG CHẠY ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# Ép hệ thống dùng IPv4 và ép cứng cổng 10000 ra mọi IP
EXPOSE 10000
# THÊM CỜ KÍCH HOẠT PROFILE PROD Ở ĐÂY
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-Dspring.profiles.active=prod", "-Dserver.port=10000", "-Dserver.address=0.0.0.0", "-jar", "app.jar"]