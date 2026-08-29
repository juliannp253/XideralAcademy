#!/usr/bin/env bash
# Script para demostrar la ejecución selectiva de suites de prueba usando @Tag en Maven Surefire.

cd "$(dirname "$0")/.." || exit 1

TAG=${1:-rapido}

echo "=========================================================="
echo "  Ejecutando únicamente los tests con la etiqueta: @Tag(\"$TAG\")"
echo "=========================================================="

./mvnw test -Dgroups="$TAG"

EXIT_CODE=$?

echo ""
echo "Tip: Puedes probar con ./scripts/filtrar.sh rapido  o  ./scripts/filtrar.sh lento"
exit $EXIT_CODE
