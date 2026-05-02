#!/bin/bash
echo "Subindo banco de testes"
docker compose -f compose.test.yaml up -d
sleep 5

echo "Rodando testes de integração"
./mvnw test -P integration-tests

echo "Derrubando banco de testes"
docker compose -f compose.test.yaml down