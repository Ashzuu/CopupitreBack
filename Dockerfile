# Étape 1 : Compilation du JAR avec Maven et JDK 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Mise en cache des dépendances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copie des sources et empaquetage
COPY src ./src
RUN mvn clean package -DskipTests -B

# Étape 2 : Image d'exécution légère JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
