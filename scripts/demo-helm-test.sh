#!/usr/bin/env bash
#
# `helm test` on a healthy release (passes), with MySQL scaled to 0 so the app is broken (fails), and after MySQL is
# restored (passes again). Usage: scripts/demo-helm-test.sh (or `make demo-helm-test`).

# shellcheck source=scripts/demo-lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/demo-lib.sh"

require_release

restore_mysql() {
  step "Restore MySQL"
  run "${KUBECTL[@]}" scale statefulset -l "$MYSQL_SELECTOR" --replicas=1
  run "${KUBECTL[@]}" rollout status statefulset -l "$MYSQL_SELECTOR" --timeout 5m
  run "${KUBECTL[@]}" wait deployment -l "$APP_SELECTOR" --for=condition=Available --timeout 5m
  wait_healthy
}

step "helm test on the healthy release (expect PASS)"
wait_healthy
run_helm_test

step "Break the app: scale MySQL to 0"
trap restore_mysql EXIT
run "${KUBECTL[@]}" scale statefulset -l "$MYSQL_SELECTOR" --replicas=0
run "${KUBECTL[@]}" wait pod -l "$MYSQL_SELECTOR" --for=delete --timeout 2m

step "helm test on the broken app (expect FAIL)"
if run_helm_test; then
  die "helm test passed although MySQL is down"
fi
echo "OK: helm test failed as expected"

trap - EXIT
restore_mysql

step "helm test after MySQL is back (expect PASS)"
run_helm_test
