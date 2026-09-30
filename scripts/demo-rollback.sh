#!/usr/bin/env bash
#
# Bad upgrade + rollback scenario:
#   1. create a consumer on the current (good) revision
#   2. upgrade app.image.tag to a nonexistent tag with --wait --timeout 2m; the upgrade fails, and because the
#      Deployment uses maxUnavailable: 0 the old pods keep serving
#   3. `helm rollback <release> <last good revision> --wait`
#   4. check the app is healthy, the consumer is still there, and `helm test` passes
# Usage: scripts/demo-rollback.sh (or `make demo-rollback`). BAD_TAG and BAD_TIMEOUT override the broken tag and timeout.

# shellcheck source=scripts/demo-lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/demo-lib.sh"

BAD_TAG=${BAD_TAG:-does-not-exist}
BAD_TIMEOUT=${BAD_TIMEOUT:-2m}

require_release
[ "$(current_status)" = deployed ] || die "release '$RELEASE' is $(current_status), not deployed; roll it back first"
GOOD_REV=$(current_revision)

step "Last good revision: $GOOD_REV"
show_history
wait_healthy

step "Create data before the bad upgrade"
CONSUMER_ID=$(create_consumer)
check_consumer "$CONSUMER_ID"

step "Bad upgrade: app.image.tag=$BAD_TAG (expect failure after $BAD_TIMEOUT)"
if run "${HELM[@]}" upgrade "$RELEASE" "$CHART" --reset-then-reuse-values \
     --set-string "app.image.tag=$BAD_TAG" --wait --timeout "$BAD_TIMEOUT"; then
  die "the bad upgrade succeeded"
fi
echo "OK: upgrade failed as expected"

step "Failed revision $(current_revision): the new pod cannot pull its image, the old pod still serves"
show_history
show_pods
wait_healthy

step "Roll back to revision $GOOD_REV"
run "${HELM[@]}" rollback "$RELEASE" "$GOOD_REV" --wait --timeout 5m

step "After rollback: revision $(current_revision)"
show_history
show_pods
wait_healthy
check_consumer "$CONSUMER_ID"
run_helm_test
