#!/usr/bin/env bash
#
# Uninstall + reinstall scenario. `helm uninstall` removes the StatefulSet, but not the PVC created from its
# volumeClaimTemplate, and the MySQL Secret is annotated helm.sh/resource-policy: keep. The reinstall adopts the kept
# Secret, reuses its passwords (they match the data on the PVC), and a consumer created before the uninstall is still there.
#
# With --clean the PVC and the Secret are deleted after the uninstall, so the reinstall starts from an empty database
# with new random passwords.
#
# Usage: scripts/demo-reinstall.sh [--clean] (or `make demo-reinstall [CLEAN=1]`).

# shellcheck source=scripts/demo-lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/demo-lib.sh"

CLEAN=
case "${1:-}" in
  --clean) CLEAN=yes ;;
  "") ;;
  *) die "unknown argument: $1" ;;
esac

secret_name() {
  "${KUBECTL[@]}" get secret -l "$MYSQL_SELECTOR" -o jsonpath='{.items[0].metadata.name}'
}

# Fingerprint of the stored MySQL passwords, so the demo can compare them without printing them.
password_fingerprint() {
  "${KUBECTL[@]}" get secret "$1" -o json | jq -r '.data["mysql-root-password"] + .data["mysql-password"]' |
    sha256sum | cut -c1-12
}

require_release
wait_healthy

step "Create data before the uninstall"
CONSUMER_ID=$(create_consumer)
check_consumer "$CONSUMER_ID"
SECRET=$(secret_name)
BEFORE=$(password_fingerprint "$SECRET")
echo "Secret $SECRET password fingerprint: $BEFORE"

step "helm uninstall"
run "${HELM[@]}" uninstall "$RELEASE" --wait --timeout 5m
run "${HELM[@]}" list --all
echo "Left behind:"
run "${KUBECTL[@]}" get pvc,secret -l "app.kubernetes.io/instance=$RELEASE"
run "${KUBECTL[@]}" get pods -l "app.kubernetes.io/instance=$RELEASE"

if [ -n "$CLEAN" ]; then
  step "Clean slate: delete the MySQL PVC and Secret (and the last test pod)"
  run "${KUBECTL[@]}" delete pvc -l "$MYSQL_SELECTOR" --wait
  run "${KUBECTL[@]}" delete secret "$SECRET"
  run "${KUBECTL[@]}" delete pod -l "app.kubernetes.io/instance=$RELEASE,app.kubernetes.io/component=test" --ignore-not-found
fi

step "Reinstall"
deploy_release
show_history
show_pods
wait_healthy

AFTER=$(password_fingerprint "$SECRET")
echo "Secret $SECRET password fingerprint: $AFTER"
if [ -n "$CLEAN" ]; then
  [ "$AFTER" != "$BEFORE" ] || die "expected new passwords after the clean reinstall"
  status=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 10 "$APP_URL/consumers/$CONSUMER_ID")
  echo "GET $APP_URL/consumers/$CONSUMER_ID -> HTTP $status"
  [ "$status" = 404 ] || die "expected consumer $CONSUMER_ID to be gone after the clean reinstall"
  echo "OK: empty database with new passwords"
else
  [ "$AFTER" = "$BEFORE" ] || die "the reinstall changed the MySQL passwords"
  echo "OK: passwords retained"
  check_consumer "$CONSUMER_ID"
fi
run_helm_test
