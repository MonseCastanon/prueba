# Etapa 1: Compilación con Gradle y Java 17
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

# Etapa 2: Imagen ligera para ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el .jar generado en la etapa anterior
COPY --from=builder /app/build/libs/*.jar app.jar

# Optimización de memoria JVM para el plan gratuito de Render (512 MB RAM)
ENV JAVA_OPTS="-Xmx350m -Xms150m -XX:+UseSerialGC"

# Exponer el puerto
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
