#! /bin/bash

set -e
: "${FTGO_ADMIN_PASSWORD:?Set FTGO_ADMIN_PASSWORD to the deployed application's admin password}"

./deployment/kubernetes/scripts/kubernetes-wait-for-ready-pods.sh $(kubectl get pod -l application=ftgo  -o=jsonpath='{.items[*].metadata.name}')

./deployment/kubernetes/scripts/port-forwards.sh

 DOCKER_HOST_IP=localhost ./gradlew :ftgo-end-to-end-tests:cleanTest :ftgo-end-to-end-tests:test
