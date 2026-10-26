#!/bin/bash
# Script de démarrage IASC Hub
# Usage: ./start.sh

cd "$(dirname "$0")"

echo "🔧 Libération du port 8080..."
fuser -k 8080/tcp 2>/dev/null
sleep 1

echo "🚀 Démarrage du backend..."
cd iasc-hub/backend
source env.sh && mvn spring-boot:run
