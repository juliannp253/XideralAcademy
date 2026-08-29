#!/usr/bin/env bash
# Script para compilar y ejecutar la suite completa de pruebas unitarias con JUnit 5 y Mockito.

cd "$(dirname "$0")/.." || exit 1

echo "=========================================================="
echo "  Ejecutando Suite Completa de Tests (JUnit 5 & Mockito)  "
echo "=========================================================="

./mvnw test

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
  echo ""
  echo -e "\033[32m✔ TODOS LOS TESTS PASARON EXITOSAMENTE\033[0m"
else
  echo ""
  echo -e "\033[31m✘ HUBO FALLOS EN LA SUITE DE TESTS\033[0m"
fi

exit $EXIT_CODE
