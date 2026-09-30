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

# Compares two non-negative decimal integers of any size; prints -1, 0 or 1.
num_cmp() {
  local x=${1#"${1%%[!0]*}"} y=${2#"${2%%[!0]*}"}
  if ((${#x} != ${#y})); then
    ((${#x} < ${#y})) && echo -1 || echo 1
  elif [ "$x" = "$y" ]; then
    echo 0
  else
    [[ $x < $y ]] && echo -1 || echo 1
  fi
}

SEMVER_RE='^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-((0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)(\.(0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*))*))?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?$'

# Prints -1, 0 or 1 comparing two valid SemVer 2.0.0 versions by precedence (build metadata ignored).
semver_cmp() {
  local a b i x y c
  [[ $1 =~ $SEMVER_RE ]]
  a=("${BASH_REMATCH[1]}" "${BASH_REMATCH[2]}" "${BASH_REMATCH[3]}" "${BASH_REMATCH[5]}")
  [[ $2 =~ $SEMVER_RE ]]
  b=("${BASH_REMATCH[1]}" "${BASH_REMATCH[2]}" "${BASH_REMATCH[3]}" "${BASH_REMATCH[5]}")
  for i in 0 1 2; do
    c=$(num_cmp "${a[i]}" "${b[i]}")
    if [ "$c" != 0 ]; then
      echo "$c"
      return
    fi
  done
  if [ "${a[3]}" = "${b[3]}" ]; then echo 0; return; fi
  [ -z "${a[3]}" ] && { echo 1; return; }
  [ -z "${b[3]}" ] && { echo -1; return; }
  local -a pa pb
  IFS=. read -ra pa <<< "${a[3]}"
  IFS=. read -ra pb <<< "${b[3]}"
  for ((i = 0; i < ${#pa[@]} && i < ${#pb[@]}; i++)); do
    x=${pa[i]} y=${pb[i]}
    [ "$x" = "$y" ] && continue
    if [[ $x =~ ^[0-9]+$ && $y =~ ^[0-9]+$ ]]; then
      num_cmp "$x" "$y"
    elif [[ $x =~ ^[0-9]+$ ]]; then
      echo -1
    elif [[ $y =~ ^[0-9]+$ ]]; then
      echo 1
    else
      [[ $x < $y ]] && echo -1 || echo 1
    fi
    return
  done
  ((${#pa[@]} < ${#pb[@]})) && echo -1 || echo 1
}

# The chart directory plus the targets of any symlinks inside it (e.g. files/schema.sql -> mysql/schema.sql).
chart_paths() {
  echo "$CHART_DIR"
  git ls-tree -r "$1" -- "$CHART_DIR" | while read -r mode _ object path; do
    [ "$mode" = 120000 ] || continue
    realpath -m -s --relative-to=. "$(dirname "$path")/$(git cat-file -p "$object")"
  done
}

chart_version() {
  { git show "$1:$CHART_YAML" 2> /dev/null || true; } | sed -nE 's/^version:[[:space:]]*"?([^"[:space:]]+)"?.*/\1/p'
}

git rev-parse --verify --quiet "$BASE_REF^{commit}" > /dev/null || error "base commit $BASE_REF not found (fetch full history)"

mapfile -t paths < <({ chart_paths "$BASE_REF"; chart_paths "$HEAD_REF"; } | sort -u)
changed=$(git diff --name-only "$BASE_REF" "$HEAD_REF" -- "${paths[@]}")
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

for v in "$base_version" "$head_version"; do
  [[ $v =~ $SEMVER_RE ]] || error "chart version '$v' is not valid SemVer 2.0.0"
done

cmp=$(semver_cmp "$base_version" "$head_version")
if [ "$cmp" = 0 ]; then
  error "$CHART_DIR changed but the chart version is still $base_version. Bump 'version' in $CHART_YAML (SemVer: patch for fixes, minor for new values/features, major for breaking changes)."
fi

if [ "$cmp" = 1 ]; then
  error "chart version went backwards: $base_version -> $head_version. 'version' in $CHART_YAML must increase."
fi

echo "Chart version bumped: $base_version -> $head_version"
