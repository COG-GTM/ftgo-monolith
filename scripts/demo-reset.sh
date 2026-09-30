#!/usr/bin/env bash
#
# Returns the kind demo to its start state (docs/demo/helm-on-kind.md): cluster up, every image the chart needs already
# on the node, no Helm release and no MySQL data. `make kind-demo` then only has to deploy, and the demo needs no
# registry access (conference Wi-Fi).
#
#   scripts/demo-reset.sh           delete the cluster if present, recreate it, build + load the app images,
#                                   preload the third-party images (MySQL, helm test curl)
#   scripts/demo-reset.sh --quick   keep the cluster and images: uninstall the release, delete the MySQL PVC + Secret
#                                   and the last helm test pod
#
# Environment: the Makefile variables (CLUSTER, RELEASE, NAMESPACE, CHART, KIND_VALUES); DOCKERHUB_MIRROR (e.g.
# mirror.gcr.io) is tried when a Docker Hub pull fails, e.g. on a rate limit.

# shellcheck source=scripts/demo-lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/demo-lib.sh"

QUICK=
case "${1:-}" in
  --quick) QUICK=yes ;;
  "") ;;
  *) die "unknown argument: $1" ;;
esac

for tool in docker kind make; do
  command -v "$tool" > /dev/null || die "$tool is not installed"
done

NODE="$CLUSTER-control-plane"
DOCKERHUB_MIRROR=${DOCKERHUB_MIRROR:-}

cluster_exists() {
  kind get clusters 2> /dev/null | grep -qx "$CLUSTER"
}

# docker.io reference as containerd names it: mysql:8.0.39 -> docker.io/library/mysql:8.0.39.
containerd_name() {
  local ref=$1 first=${1%%/*}
  if [[ $ref != */* ]]; then
    echo "docker.io/library/$ref"
  elif [[ $first == *.* || $first == *:* || $first == localhost ]]; then
    echo "$ref"
  else
    echo "docker.io/$ref"
  fi
}

on_node() {
  docker exec "$NODE" crictl inspecti "$1" > /dev/null 2>&1
}

# Puts image $1 (repo:tag or repo:tag@sha256:...) on the kind node. Uses docker save | ctr import rather than
# `kind load docker-image`, which fails for multi-platform images with Docker's containerd image store.
preload_image() {
  local ref=$1 tagged=${1%@*} digest=
  [[ $ref == *@* ]] && digest=${ref#*@}
  if on_node "$ref"; then
    echo "$ref: already on $NODE"
    return
  fi
  if ! docker image inspect "$ref" > /dev/null 2>&1 && ! run docker pull -q "$ref"; then
    [ -n "$DOCKERHUB_MIRROR" ] || die "cannot pull $ref; set DOCKERHUB_MIRROR (e.g. mirror.gcr.io) or pull it yourself"
    local path
    path=$(containerd_name "$ref")
    run docker pull -q "$DOCKERHUB_MIRROR/${path#docker.io/}"
    run docker tag "$DOCKERHUB_MIRROR/${path#docker.io/}" "$tagged"
  fi
  [ "$ref" = "$tagged" ] || run docker tag "$ref" "$tagged"
  local platform
  platform="linux/$(docker version -f '{{.Server.Arch}}')"
  echo "\$ docker save --platform $platform $tagged | ctr -n k8s.io images import -   (on $NODE)"
  # ctr can report a layer mismatch for attestation manifests after importing the image itself; verified below.
  docker save --platform "$platform" "$tagged" |
    docker exec -i "$NODE" ctr -n k8s.io images import --snapshotter overlayfs - > /dev/null 2>&1 || true
  if [ -n "$digest" ]; then
    local name
    name=$(containerd_name "$tagged")
    docker exec "$NODE" ctr -n k8s.io images tag --force "$name" "${name%:*}@$digest" > /dev/null
  fi
  on_node "$ref" || die "$ref is not on $NODE after the import"
  echo "$ref: loaded into $NODE"
}

# Third-party images referenced by the chart with the kind values (everything except the locally built ones).
third_party_images() {
  "${HELM[@]}" template "$RELEASE" "$CHART" -f "$KIND_VALUES" \
      --set-string app.image.tag=x --set-string migrations.image.tag=x 2> >(grep -v 'found symbolic link' >&2) |
    sed -n 's/^[[:space:]]*image:[[:space:]]*"\{0,1\}\([^"]*\)"\{0,1\}[[:space:]]*$/\1/p' |
    grep -Ev '^(ftgo-application|ftgo-flyway):' | sort -u
}

if [ -n "$QUICK" ]; then
  cluster_exists || die "cluster '$CLUSTER' does not exist; run scripts/demo-reset.sh without --quick"
  step "Uninstall the release and delete its data"
  if "${HELM[@]}" status "$RELEASE" > /dev/null 2>&1; then
    run "${HELM[@]}" uninstall "$RELEASE" --wait --timeout 5m
  fi
  run "${KUBECTL[@]}" delete pvc,secret -l "$MYSQL_SELECTOR" --ignore-not-found --wait
  run "${KUBECTL[@]}" delete pod -l "app.kubernetes.io/instance=$RELEASE,app.kubernetes.io/component=test" --ignore-not-found
else
  if cluster_exists; then
    step "Delete kind cluster '$CLUSTER'"
    run kind delete cluster --name "$CLUSTER"
  fi
  step "Create the cluster and build + load the app images"
  run make -C "$ROOT_DIR" --no-print-directory CLUSTER="$CLUSTER" kind-up images
fi

step "Preload third-party images into $NODE"
while read -r image; do
  preload_image "$image"
done < <(third_party_images)

step "Start state"
run "${HELM[@]}" list --all
run "${KUBECTL[@]}" get pods,pvc,secret -l "app.kubernetes.io/instance=$RELEASE"
echo "App images: $(cat "$IMAGE_TAG_FILE" 2> /dev/null || echo 'none built')"
if curl -s -o /dev/null --max-time 2 "$APP_URL"; then
  echo "WARNING: $APP_URL still answers; something else is using port 8081" >&2
fi
echo "Ready: run 'make kind-demo'."
