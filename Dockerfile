# ============================================================
# Stage 1 — build
# Usa a imagem oficial do Maven com JDK 21 (Maven + JDK incluídos)
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia somente o pom.xml primeiro para aproveitar o cache de camadas
COPY pom.xml .
RUN mvn dependency:go-offline -B 2>/dev/null || true

# Copia o restante do código-fonte e compila
COPY src ./src
RUN mvn package -DskipTests -B

# ============================================================
# Stage 2 — runtime
# Imagem mínima com JRE 21 para rodar a aplicação
# ============================================================
FROM eclipse-temurin:21-jre

LABEL maintainer="coupon-api"
LABEL description="API REST de gerenciamento de cupons de desconto"

WORKDIR /app

# Copia somente o JAR produzido no stage de build
COPY --from=build /app/target/*.jar app.jar

# Porta exposta pela aplicação Spring Boot
EXPOSE 8080

# Health check simples via endpoint de status HTTP
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health 2>/dev/null || \
        wget -qO- http://localhost:8080/api/v1/coupons 2>/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
