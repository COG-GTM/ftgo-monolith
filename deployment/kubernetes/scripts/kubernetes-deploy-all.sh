#! /bin/bash -e

kubectl get secret ftgo-db-secret >/dev/null 2>&1 || {
  echo "Secret ftgo-db-secret is required; run deployment/kubernetes/misc/create-db-secret.sh first"
  exit 1
}

for secret_key in root-password username password; do
  if [ -z "$(kubectl get secret ftgo-db-secret -o "jsonpath={.data.${secret_key}}")" ]; then
    echo "Secret ftgo-db-secret predates the externalized-credentials change. Its values were committed to git and must be treated as compromised. Run 'kubectl delete secret ftgo-db-secret', rerun deployment/kubernetes/misc/create-db-secret.sh, and rotate the MySQL accounts (ALTER USER) on the existing volume." >&2
    exit 1
  fi
done

kubectl apply -f <(cat deployment/kubernetes/stateful-services/*.yml)

./deployment/kubernetes/scripts/kubernetes-wait-for-ready-pods.sh ftgo-mysql-0

kubectl apply -f <(cat */src/deployment/kubernetes/*.yml)
