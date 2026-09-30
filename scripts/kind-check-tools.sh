#!/usr/bin/env bash
#
# Checks that the tools used by the kind targets in the Makefile are installed at the pinned versions.
# Versions are pinned in the Makefile (KIND_VERSION, KUBECTL_VERSION, HELM_VERSION) and passed in via the environment.
# kind and Helm must match major.minor; kubectl may be within one minor version of the cluster (Kubernetes skew policy).

set -euo pipefail

: "${KIND_VERSION:?}" "${KUBECTL_VERSION:?}" "${HELM_VERSION:?}"

errors=0

fail() {
  echo "ERROR: $*" >&2
  errors=$((errors + 1))
}

minor_of() {
  # v1.31.0 -> 31
  echo "$1" | sed -E 's/^v?[0-9]+\.([0-9]+).*/\1/'
}

major_minor_of() {
  # v0.24.0 -> 0.24
  echo "$1" | sed -E 's/^v?([0-9]+\.[0-9]+).*/\1/'
}

check_installed() {
  command -v "$1" > /dev/null || { fail "$1 is not installed (need $2)"; return 1; }
}

if check_installed docker "Docker 24+"; then
  docker info > /dev/null 2>&1 || fail "the Docker daemon is not reachable (is it running and can $(id -un) use it?)"
fi

if check_installed kind "$KIND_VERSION"; then
  have=$(kind version | awk '{print $2}')
  [ "$(major_minor_of "$have")" = "$(major_minor_of "$KIND_VERSION")" ] || fail "kind $have found, need $KIND_VERSION"
fi

if check_installed kubectl "$KUBECTL_VERSION"; then
  have=$(kubectl version --client -o json | sed -nE 's/.*"gitVersion": *"([^"]+)".*/\1/p')
  skew=$(( $(minor_of "$have") - $(minor_of "$KUBECTL_VERSION") ))
  [ "${skew#-}" -le 1 ] || fail "kubectl $have found, need $KUBECTL_VERSION (+/- one minor version)"
fi

if check_installed helm "$HELM_VERSION"; then
  have=$(helm version --template '{{.Version}}')
  [ "$(major_minor_of "$have")" = "$(major_minor_of "$HELM_VERSION")" ] || fail "helm $have found, need $HELM_VERSION"
fi

if [ "$errors" -gt 0 ]; then
  echo "Install the pinned versions listed in deployment/kind/README.md and retry." >&2
  exit 1
fi

echo "Tools OK: docker, kind $KIND_VERSION, kubectl $KUBECTL_VERSION, helm $HELM_VERSION"
