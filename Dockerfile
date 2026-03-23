# --- STAGE 1: MÔI TRƯỜNG BUILD ---
FROM eclipse-temurin:25-jdk-alpine as builder
WORKDIR /app

# 1. Tối ưu Build Time: Copy Gradle file trước
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 2. Tải thư viện trước (Docker sẽ cache lại, tiết kiệm 15 phút cho lần sau)
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# 3. Copy source code và build
COPY src src
RUN ./gradlew build -x test -x spotbugsMain -x spotbugsTest --no-daemon

# --- STAGE 2: MÔI TRƯỜNG CHẠY ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy file jar từ builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Render tự cung cấp biến môi trường PORT (mặc định là 10000).
# Lệnh này dùng dạng mảng JSON để chạy Java trực tiếp, không qua shell.
# Gắn cứng host 0.0.0.0 để Render luôn "nhìn thấy" cổng mở.
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Dserver.address=0.0.0.0", "-jar", "app.jar"]
