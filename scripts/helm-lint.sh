#!/usr/bin/env bash
#
# Lints the FTGO Helm chart and validates the rendered manifests against the Kubernetes API schemas.
#
#   1. helm lint --strict and helm template | kubeconform -strict for each values set in VALID_CASES
#   2. checks that each values set in INVALID_CASES is rejected by values.schema.json
#
# Usage: scripts/helm-lint.sh (make lint)
#
# Environment:
#   KUBERNETES_VERSION   Kubernetes version whose schemas kubeconform validates against (default 1.31.0, the kind node version)

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

CHART=deployment/helm/ftgo
RELEASE=ftgo
KUBERNETES_VERSION="${KUBERNETES_VERSION:-1.31.0}"

# description|helm values arguments
VALID_CASES=(
  "default values|"
  "values-kind.yaml|-f $CHART/values-kind.yaml"
  "migrations as a hook Job|--set migrations.mode=hook"
)

INVALID_CASES=(
  "migrations.mode=bogus|--set migrations.mode=bogus"
  "app.replicaCount=-1|--set app.replicaCount=-1"
  "app.service.nodePort=80|--set app.service.type=NodePort --set app.service.nodePort=80"
)

for tool in helm kubeconform; do
  command -v "$tool" > /dev/null || { echo "ERROR: $tool is not installed (see the pinned versions in the Makefile)" >&2; exit 1; }
done

failures=0

fail() {
  echo "ERROR: $*" >&2
  if [ -n "${GITHUB_ACTIONS:-}" ]; then
    echo "::error title=Helm chart validation::$*"
  fi
  failures=$((failures + 1))
}

for spec in "${VALID_CASES[@]}"; do
  IFS='|' read -r description args <<< "$spec"
  read -r -a values_args <<< "$args"

  echo "==> helm lint --strict ($description)"
  helm lint --strict "$CHART" "${values_args[@]+"${values_args[@]}"}" || fail "helm lint --strict failed for $description"

  echo "==> helm template | kubeconform -strict -kubernetes-version $KUBERNETES_VERSION ($description)"
  helm template "$RELEASE" "$CHART" "${values_args[@]+"${values_args[@]}"}" \
    | kubeconform -strict -kubernetes-version "$KUBERNETES_VERSION" -summary -output text \
    || fail "rendered manifests are invalid for $description"
done

for spec in "${INVALID_CASES[@]}"; do
  IFS='|' read -r description args <<< "$spec"
  read -r -a values_args <<< "$args"

  echo "==> expecting the chart to reject $description"
  if output=$(helm template "$RELEASE" "$CHART" "${values_args[@]}" 2>&1); then
    fail "helm template accepted invalid values ($description); values.schema.json should reject them"
  else
    echo "rejected as expected:"
    echo "$output" | grep -v 'found symbolic link' | sed 's/^/    /'
  fi
done

if [ "$failures" -gt 0 ]; then
  echo "Helm chart validation failed with $failures error(s)" >&2
  exit 1
fi

echo "Helm chart OK: lint, kubeconform (Kubernetes $KUBERNETES_VERSION) and invalid-values checks passed"
