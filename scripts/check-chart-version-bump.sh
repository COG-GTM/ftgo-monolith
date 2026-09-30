#!/usr/bin/env bash
#
# Fails if the Helm chart changed since BASE_REF but its Chart.yaml version was not increased.
#
# Usage: scripts/check-chart-version-bump.sh BASE_REF [HEAD_REF]
#   BASE_REF   commit to compare against (in CI: the PR base, or the commit before a push)
#   HEAD_REF   defaults to HEAD

set -euo pipefail

CHART_DIR=deployment/helm/ftgo
CHART_YAML=$CHART_DIR/Chart.yaml

BASE_REF=${1:?usage: $0 BASE_REF [HEAD_REF]}
HEAD_REF=${2:-HEAD}

cd "$(dirname "${BASH_SOURCE[0]}")/.."

error() {
  echo "ERROR: $*" >&2
  if [ -n "${GITHUB_ACTIONS:-}" ]; then
    echo "::error file=$CHART_YAML,title=Chart version not bumped::$*"
  fi
  exit 1
}

chart_version() {
  { git show "$1:$CHART_YAML" 2> /dev/null || true; } | sed -nE 's/^version:[[:space:]]*"?([^"[:space:]]+)"?.*/\1/p'
}

git rev-parse --verify --quiet "$BASE_REF^{commit}" > /dev/null || error "base commit $BASE_REF not found (fetch full history)"

changed=$(git diff --name-only "$BASE_REF" "$HEAD_REF" -- "$CHART_DIR")
if [ -z "$changed" ]; then
  echo "No changes under $CHART_DIR since $(git rev-parse --short "$BASE_REF"); no version bump needed."
  exit 0
fi

echo "Chart files changed since $(git rev-parse --short "$BASE_REF"):"
while IFS= read -r f; do echo "  $f"; done <<< "$changed"

base_version=$(chart_version "$BASE_REF")
head_version=$(chart_version "$HEAD_REF")

[ -n "$head_version" ] || error "could not read version from $CHART_YAML at $HEAD_REF"

if [ -z "$base_version" ]; then
  echo "$CHART_YAML is new at $HEAD_REF (version $head_version); nothing to compare."
  exit 0
fi

if [ "$base_version" = "$head_version" ]; then
  error "$CHART_DIR changed but the chart version is still $base_version. Bump 'version' in $CHART_YAML (SemVer: patch for fixes, minor for new values/features, major for breaking changes)."
fi

highest=$(printf '%s\n%s\n' "$base_version" "$head_version" | sort -V | tail -n1)
if [ "$highest" != "$head_version" ]; then
  error "chart version went backwards: $base_version -> $head_version. 'version' in $CHART_YAML must increase."
fi

echo "Chart version bumped: $base_version -> $head_version"
