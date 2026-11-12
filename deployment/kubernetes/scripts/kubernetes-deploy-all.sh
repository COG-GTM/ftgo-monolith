#! /bin/bash -e

kubectl get secret ftgo-db-secret >/dev/null 2>&1 || {
  echo "Secret ftgo-db-secret is required; run deployment/kubernetes/misc/create-db-secret.sh first"
  exit 1
}

kubectl apply -f <(cat deployment/kubernetes/stateful-services/*.yml)

./deployment/kubernetes/scripts/kubernetes-wait-for-ready-pods.sh ftgo-mysql-0

kubectl apply -f <(cat */src/deployment/kubernetes/*.yml)
