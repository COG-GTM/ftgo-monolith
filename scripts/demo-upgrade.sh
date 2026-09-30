#!/usr/bin/env bash
#
# Upgrade scenario: changes one value (app.replicaCount, default 2) with `helm upgrade --wait` and shows `helm history`.
# Other values (values-kind.yaml, image tags) are carried over with --reset-then-reuse-values.
# Usage: REPLICAS=2 scripts/demo-upgrade.sh (or `make demo-upgrade REPLICAS=2`).

# shellcheck source=scripts/demo-lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/demo-lib.sh"

REPLICAS=${REPLICAS:-2}
[[ "$REPLICAS" =~ ^[1-9][0-9]*$ ]] || die "REPLICAS must be a positive integer (the demo checks the app is healthy after the upgrade), got '$REPLICAS'"

require_release

step "Before: revision $(current_revision)"
show_history

step "Upgrade: app.replicaCount=$REPLICAS"
run "${HELM[@]}" upgrade "$RELEASE" "$CHART" --reset-then-reuse-values \
  --set "app.replicaCount=$REPLICAS" --wait --timeout 5m

step "After: revision $(current_revision)"
run "${KUBECTL[@]}" get deployment -l "$APP_SELECTOR"
show_pods
show_history
wait_healthy
