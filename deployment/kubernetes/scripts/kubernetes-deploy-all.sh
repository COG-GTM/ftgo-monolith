#! /bin/bash -e

./deployment/kubernetes/misc/create-db-secret.sh

kubectl apply -f <(cat deployment/kubernetes/stateful-services/*.yml)

./deployment/kubernetes/scripts/kubernetes-wait-for-ready-pods.sh ftgo-mysql-0

kubectl apply -f <(cat */src/deployment/kubernetes/*.yml)
