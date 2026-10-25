#!/usr/bin/env bash
# ==============================================================================
# Script de déploiement automatisé CopupitreBack (Environnements Linux / CentOS)
# ==============================================================================

set -euo pipefail

# Définition des couleurs pour l'affichage console
BOLD="\033[1m"
GREEN="\033[0;32m"
YELLOW="\033[0;33m"
RED="\033[0;31m"
BLUE="\033[0;34m"
NC="\033[0m"

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

# Se placer dans le répertoire du script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

log_info "=== Démarrage du déploiement de CopupitreBack ==="
log_info "Répertoire d'exécution : ${SCRIPT_DIR}"

# 1. Vérification du fichier d'environnement .env
if [ ! -f ".env" ]; then
    log_warn "Le fichier .env est introuvable."
    if [ -f ".env.example" ]; then
        log_info "Création automatique du fichier .env à partir de .env.example..."
        cp .env.example .env
        log_warn "Un fichier .env par défaut a été créé. Pensez à ajuster les secrets (JWT_SECRET, DB_PASSWORD) en production !"
    else
        log_error "Erreur : .env et .env.example sont introuvables. Arrêt du déploiement."
        exit 1
    fi
else
    log_success "Fichier d'environnement .env détecté."
fi

# 2. Vérification des prérequis Docker et Docker Compose
if ! command -v docker &> /dev/null; then
    log_error "Docker n'est pas installé sur cette machine. Veuillez installer Docker avant de continuer."
    exit 1
fi

if ! docker info &> /dev/null; then
    log_error "Le démon Docker ne répond pas. Vérifiez que le service est actif (ex: sudo systemctl start docker)."
    exit 1
fi

COMPOSE_CMD=""
if docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    log_error "Ni 'docker compose' ni 'docker-compose' n'ont été trouvés. Veuillez installer le plugin Compose."
    exit 1
fi
log_success "Docker Compose détecté : $COMPOSE_CMD"

# 3. Préparation du répertoire de logs sur l'hôte avec permissions d'écriture
log_info "Préparation du répertoire de logs..."
mkdir -p logs
chmod 777 logs
log_success "Dossier de logs configuré (./logs)"

# 4. Construction et lancement des conteneurs
log_info "Construction des images et démarrage des conteneurs via Docker Compose..."
$COMPOSE_CMD up -d --build --remove-orphans

# 5. Attente active de la santé des conteneurs
wait_for_service() {
    local service_name="$1"
    local max_retries="$2"
    local sleep_sec="$3"
    local count=0

    log_info "Attente de l'état 'healthy' pour le conteneur [${service_name}]..."
    while [ $count -lt "$max_retries" ]; do
        local status
        status=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$service_name" 2>/dev/null || echo "not_found")
        if [ "$status" = "healthy" ]; then
            log_success "Conteneur [${service_name}] est HEALTHY !"
            return 0
        elif [ "$status" = "running" ] && [ "$service_name" = "copupitre-db" ]; then
            # Si pas de healthcheck défini sur db mais running
            log_success "Conteneur [${service_name}] est RUNNING !"
            return 0
        fi
        count=$((count + 1))
        echo -n "."
        sleep "$sleep_sec"
    done
    echo ""
    log_error "Délai dépassé en attendant la santé du conteneur [${service_name}]."
    docker logs "$service_name" --tail 50
    return 1
}

# Vérification de PostgreSQL d'abord (max 40s)
wait_for_service "copupitre-db" 20 2

# Vérification du backend ensuite (max 60s)
wait_for_service "copupitre-back" 30 2

# 6. Validation du point de contrôle HTTP Healthcheck
source .env 2>/dev/null || true
PORT="${BACKEND_PORT:-8080}"
log_info "Test du endpoint HTTP Healthcheck (http://localhost:${PORT}/api/logs)..."

if command -v curl &> /dev/null; then
    HTTP_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${PORT}/api/logs" || echo "000")
    if [ "$HTTP_RESPONSE" -eq 200 ]; then
        log_success "HTTP Healthcheck validé avec succès (HTTP 200 OK)."
    else
        log_warn "Le serveur a répondu avec le statut HTTP ${HTTP_RESPONSE} au lieu de 200. Vérifiez les logs avec : docker logs copupitre-back"
    fi
fi

# 7. Nettoyage des images orphelines
log_info "Nettoyage des images orphelines (dangling images)..."
docker image prune -f || true

# 8. Récapitulatif
echo ""
echo -e "${BOLD}${GREEN}================================================================${NC}"
echo -e "${BOLD}${GREEN}   DÉPLOIEMENT COPUPITREBACK EFFECTUÉ AVEC SUCCÈS !             ${NC}"
echo -e "${BOLD}${GREEN}================================================================${NC}"
echo -e "   - Base de données : copupitre-db (PostgreSQL 16 Alpine)"
echo -e "   - Backend API     : copupitre-back (Spring Boot 4 / Java 21 Temurin)"
echo -e "   - URL de base API : http://localhost:${PORT}/api"
echo -e "   - Healthcheck     : http://localhost:${PORT}/api/logs"
echo -e "   - Fichiers Logs   : ${SCRIPT_DIR}/logs"
echo -e "${BOLD}${GREEN}================================================================${NC}"
