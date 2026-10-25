# ==============================================================================
# Étape 1 : Builder (Compilation et empaquetage Maven)
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Mise en cache optimale des dépendances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copie du code source et construction de l'archive exécutable sans exécuter les tests
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Étape 2 : Runner (Environnement d'exécution léger et sécurisé JRE 21 Alpine)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine AS runner

# Installation de curl pour le HEALTHCHECK HTTP et su-exec pour l'exécution non-root
RUN apk add --no-cache curl su-exec

# Création d'un groupe et utilisateur système non-root dédié (UID/GID 1000)
RUN addgroup -S -g 1000 appgroup && adduser -S -u 1000 -G appgroup appuser

# Configuration du répertoire de travail et des dossiers de logs
WORKDIR /app
RUN mkdir -p /app/logs /app/logs/archived && chown -R appuser:appgroup /app

# Récupération de l'artefact JAR compilé depuis l'étape de build
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

# Script d'entrée pour la gestion des permissions de volumes et bascule vers appuser
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh && chmod +x /usr/local/bin/docker-entrypoint.sh

# Optimisation JVM adaptée aux conteneurs (limites mémoire cgroup & entropie)
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Port d'écoute du serveur Spring Boot
EXPOSE 8080

# Healthcheck périodique sur le point de contrôle /api/logs
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/api/logs || exit 1

# Démarrage via entrypoint qui ajuste les droits des volumes et bascule vers appuser
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]
