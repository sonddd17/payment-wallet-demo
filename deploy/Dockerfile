# Image chạy production: dùng file jar đã build sẵn (không build Maven trên server ít RAM)
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -r -u 1001 appuser
COPY payment-wallet-demo.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
