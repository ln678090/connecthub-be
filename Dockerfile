FROM eclipse-temurin:25-jdk-alpine as builder
WORKDIR /app
COPY . .
# CẤP QUYỀN THỰC THI CHO gradlew
RUN chmod +x ./gradlew

# Build project (bỏ qua test để build nhanh hơn)
RUN ./gradlew build -x test -x spotbugsMain -x spotbugsTest

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Render tự động cấp phát biến $PORT (mặc định là 10000)
# Không cần lệnh EXPOSE cứng
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:10000} --server.address=0.0.0.0"]