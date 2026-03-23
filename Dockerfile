FROM eclipse-temurin:25-jdk-alpine as builder
WORKDIR /app
COPY . .
# CẤP QUYỀN THỰC THI CHO gradlew
RUN chmod +x ./gradlew

# Build project (bỏ qua test để build nhanh hơn)
RUN ./gradlew build -x test -x spotbugsMain -x spotbugsTest

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Copy file jar từ stage builder
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]