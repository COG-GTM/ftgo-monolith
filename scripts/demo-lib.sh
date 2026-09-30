# shellcheck shell=bash
# shellcheck disable=SC2034  # used by the scripts that source this file
# Shared helpers for the scripts/demo-*.sh release lifecycle scenarios. Sourced, not executed.
#
# Environment (the Makefile passes its values): CLUSTER, RELEASE, NAMESPACE, CHART, KIND_VALUES, APP_URL, IMAGE_TAG_FILE.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

CLUSTER=${CLUSTER:-ftgo}
RELEASE=${RELEASE:-ftgo}
NAMESPACE=${NAMESPACE:-default}
CHART=${CHART:-$ROOT_DIR/deployment/helm/ftgo}
KIND_VALUES=${KIND_VALUES:-$CHART/values-kind.yaml}
APP_URL=${APP_URL:-http://localhost:8081}
IMAGE_TAG_FILE=${IMAGE_TAG_FILE:-$ROOT_DIR/build/kind-image-tag}

HELM=(helm --kube-context "kind-$CLUSTER" --namespace "$NAMESPACE")
KUBECTL=(kubectl --context "kind-$CLUSTER" --namespace "$NAMESPACE")

APP_SELECTOR="app.kubernetes.io/instance=$RELEASE,app.kubernetes.io/component=application"
MYSQL_SELECTOR="app.kubernetes.io/instance=$RELEASE,app.kubernetes.io/component=mysql"

step() { printf '\n==> %s\n' "$*"; }
die() { echo "ERROR: $*" >&2; exit 1; }

# Prints the command, then runs it.
run() {
  printf '$'; printf ' %q' "$@"; printf '\n'
  "$@"
}

for tool in helm kubectl curl jq; do
  command -v "$tool" > /dev/null || die "$tool is not installed"
done

require_release() {
  "${HELM[@]}" status "$RELEASE" > /dev/null 2>&1 ||
    die "release '$RELEASE' not found in kind-$CLUSTER/$NAMESPACE; run 'make kind-up images deploy' first"
}

current_revision() {
  "${HELM[@]}" status "$RELEASE" -o json | jq -r '.version'
}

current_status() {
  "${HELM[@]}" status "$RELEASE" -o json | jq -r '.info.status'
}

wait_healthy() {
  run curl -fsS --retry 60 --retry-delay 3 --retry-all-errors --max-time 10 "$APP_URL/actuator/health"
  echo
}

# Same install/upgrade as `make deploy`: values-kind.yaml plus the image tag from the last `make images`.
deploy_release() {
  local args=(-f "$KIND_VALUES" --create-namespace --wait --timeout 10m)
  if [ -s "$IMAGE_TAG_FILE" ]; then
    local tag
    tag=$(cat "$IMAGE_TAG_FILE")
    args+=(--set-string "app.image.tag=$tag" --set-string "migrations.image.tag=$tag")
  fi
  run "${HELM[@]}" upgrade --install "$RELEASE" "$CHART" "${args[@]}"
}

show_pods() {
  run "${KUBECTL[@]}" get pods -l "app.kubernetes.io/instance=$RELEASE" -o wide
}

show_history() {
  run "${HELM[@]}" history "$RELEASE"
}

run_helm_test() {
  run "${HELM[@]}" test "$RELEASE" --logs --hide-notes --timeout 3m
}

# Creates a consumer named "Demo User" and prints its id.
create_consumer() {
  local response
  response=$(curl -fsS --max-time 10 -H 'Content-Type: application/json' \
    -d '{"name":{"firstName":"Demo","lastName":"User"}}' "$APP_URL/consumers")
  echo "POST $APP_URL/consumers -> $response" >&2
  echo "$response" | jq -er '.consumerId'
}

# Reads consumer $1 back and checks its name. GET /consumers/{id} returns consumerId 0 (known app bug), so only the name is compared.
check_consumer() {
  local id=$1 response name
  response=$(curl -fsS --max-time 10 "$APP_URL/consumers/$id") || { echo "GET $APP_URL/consumers/$id failed" >&2; return 1; }
  echo "GET $APP_URL/consumers/$id -> $response"
  name=$(echo "$response" | jq -r '"\(.name.firstName) \(.name.lastName)"')
  [ "$name" = "Demo User" ] || { echo "expected name 'Demo User', got '$name'" >&2; return 1; }
  echo "OK: consumer $id is still '$name'"
}
