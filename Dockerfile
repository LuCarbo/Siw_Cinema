# ==============================================================================
# Multi-Stage Dockerfile per SIW Cinema (Java 21 + Spring Boot 3)
# Ottimizzato per il deploy su piattaforme cloud come Render, Railway o Fly.io
# ==============================================================================

# STAGE 1: Compilazione e Packaging con Maven e JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia il file pom.xml e i sorgenti dalla cartella siw
COPY siw/pom.xml .
COPY siw/src ./src

# Compila l'applicazione e genera il JAR eseguibile (saltando i test per velocizzare il build cloud)
RUN mvn clean package -DskipTests

# STAGE 2: Immagine di Runtime minimale con JRE 21 Alpine
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crea un utente non-root per motivi di sicurezza
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia il file JAR generato dallo stage precedente
COPY --from=build /app/target/*.jar app.jar

# Espone la porta predefinita (gestita dinamicamente tramite ${PORT:8080} in application.properties)
EXPOSE 8080

# Comando di avvio con opzioni di ottimizzazione memoria JVM per container cloud
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
