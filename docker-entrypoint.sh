#!/bin/sh
set -e

# Configuration et vérification des permissions pour les logs
mkdir -p /app/logs /app/logs/archived 2>/dev/null || true
chown -R appuser:appgroup /app/logs 2>/dev/null || true
chmod -R 777 /app/logs 2>/dev/null || true

# Si le conteneur démarre en root, basculer sur appuser via su-exec, sinon exécuter directement
if [ "$(id -u)" = '0' ]; then
    exec su-exec appuser "$@"
else
    exec "$@"
fi
