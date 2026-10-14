# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — BUILD
# Maven ile projeyi derle; testleri atla, yalnızca JAR üret.
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /workspace

# Önce yalnızca bağımlılık tanım dosyasını kopyala; Docker layer cache'i
# kaynak kod değişse bile bağımlılık katmanını yeniden kullanır.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Kaynak kodu kopyala ve paketle
COPY src ./src
RUN mvn clean package -DskipTests -q

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 — RUNTIME
# Yalnızca JRE içeren minimal Alpine imajı; build araçları dahil değil.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Build aşamasındaki JAR'ı çalışma aşamasına taşı
COPY --from=build /workspace/target/*.jar app.jar

# 8080 portunu belgele (docker run -p ile eşleştirilmeli)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
