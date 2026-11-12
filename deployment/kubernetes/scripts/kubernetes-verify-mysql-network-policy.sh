#! /bin/bash -e

suffix="$$-$RANDOM"
allowed_pod="ftgo-mysql-policy-allowed-$suffix"
blocked_pod="ftgo-mysql-policy-blocked-$suffix"

cleanup() {
  kubectl delete pod "$allowed_pod" "$blocked_pod" --ignore-not-found --wait=true --timeout=30s >/dev/null 2>&1 || true
}
trap cleanup EXIT

kubectl apply -f - <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: $allowed_pod
  labels:
    svc: ftgo-application
spec:
  terminationGracePeriodSeconds: 0
  restartPolicy: Never
  containers:
    - name: mysql-client
      image: mysql:5.7.13
      command: ["sh", "-c", "sleep 300"]
      env:
        - name: MYSQL_PWD
          valueFrom:
            secretKeyRef:
              name: ftgo-db-secret
              key: password
---
apiVersion: v1
kind: Pod
metadata:
  name: $blocked_pod
spec:
  terminationGracePeriodSeconds: 0
  restartPolicy: Never
  containers:
    - name: mysql-client
      image: mysql:5.7.13
      command: ["sh", "-c", "sleep 300"]
      env:
        - name: MYSQL_PWD
          valueFrom:
            secretKeyRef:
              name: ftgo-db-secret
              key: password
EOF

kubectl wait --for=condition=Ready "pod/$allowed_pod" "pod/$blocked_pod" --timeout=180s

allowed_output=$(kubectl exec "$allowed_pod" -- mysql --connect-timeout=5 --host=ftgo-mysql --port=3306 --user=mysqluser --batch --skip-column-names --execute='SELECT 1')
if [ "$allowed_output" != "1" ]; then
  printf 'Labeled probe returned unexpected output: %s\n' "$allowed_output" >&2
  exit 1
fi
printf 'Labeled probe connected to ftgo-mysql:3306\n'

set +e
blocked_output=$(kubectl exec "$blocked_pod" -- mysql --connect-timeout=5 --host=ftgo-mysql --port=3306 --user=mysqluser --batch --skip-column-names --execute='SELECT 1' 2>&1)
blocked_status=$?
set -e
if [ "$blocked_status" -eq 0 ]; then
  echo "Unlabelled probe unexpectedly connected to ftgo-mysql:3306" >&2
  exit 1
fi
if ! printf '%s\n' "$blocked_output" | grep -Eiq "ERROR 2003 .*Can't connect to MySQL server.*\\(110\\)|timed out"; then
  printf 'Unlabelled probe failed for a reason other than a connection timeout:\n%s\n' "$blocked_output" >&2
  exit 1
fi
printf 'Unlabelled probe timed out as blocked by the NetworkPolicy\n'
