# ====== Giai đoạn 1: Build bằng Maven (không cần cài Maven trên máy) ======
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Tải dependency trước để tận dụng cache của Docker khi chỉ đổi mã nguồn
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

# ====== Giai đoạn 2: Ảnh chạy gọn nhẹ chỉ chứa JRE ======
FROM eclipse-temurin:21-jre
WORKDIR /app

# Chạy bằng user thường thay vì root (an toàn hơn)
RUN useradd -r -u 1001 appuser
COPY --from=build /app/target/payment-wallet-demo.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
