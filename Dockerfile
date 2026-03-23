# --- STAGE 1: MÔI TRƯỜNG BUILD ---
FROM eclipse-temurin:25-jdk-alpine as builder
WORKDIR /app

# 1. CHỈ COPY CÁC FILE CẤU HÌNH GRADLE TRƯỚC (BÍ QUYẾT ĐỂ BUILD NHANH)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 2. TẢI THƯ VIỆN TRƯỚC
# Docker sẽ lưu bộ nhớ tạm (cache) bước này. Lần sau bạn sửa code, nó sẽ BỎ QUA bước tải lại thư viện.
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# 3. BÂY GIỜ MỚI COPY SOURCE CODE VÀO
COPY src src

# 4. BUILD PROJECT SIÊU TỐC
RUN ./gradlew build -x test -x spotbugsMain -x spotbugsTest --no-daemon

# --- STAGE 2: MÔI TRƯỜNG CHẠY (NHẸ & ỔN ĐỊNH) ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy file jar đã build từ Stage 1
COPY --from=builder /app/build/libs/*.jar app.jar

# ÉP CỨNG PORT VÀ IP TẠI ĐÂY ĐỂ ĐÁNH BẠI LỖI "No open ports detected"
ENTRYPOINT ["java", "-Dserver.port=10000", "-Dserver.address=0.0.0.0", "-jar", "app.jar"]
