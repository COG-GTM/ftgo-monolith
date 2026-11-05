#! /bin/bash -e

./wait-for-services.sh

echo for API - open http://${DOCKER_HOST_IP:-localhost}:8081/swagger-ui.html
