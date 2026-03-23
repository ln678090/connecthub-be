FROM eclipse-temurin:25-jdk-alpine as builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test -x spotbugsMain -x spotbugsTest

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Khai báo biến môi trường chuẩn bị cho Spring Boot
ENV SERVER_PORT=10000
ENV SERVER_ADDRESS=0.0.0.0

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
