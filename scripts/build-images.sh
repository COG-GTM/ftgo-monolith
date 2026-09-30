#!/usr/bin/env bash
#
# Builds the FTGO container images and optionally loads them into a kind cluster.
#
# Each image is tagged <name>:<short-git-sha> (suffixed with -dirty for uncommitted or untracked changes) and <name>:dev.
#
# Usage: scripts/build-images.sh [--kind-load [cluster]] [--help]
#
# Environment:
#   IMAGE_TAG          overrides the <short-git-sha> tag
#   MAVEN_MIRROR_URL   optional Maven repository mirror passed to the Gradle build (see gradle/init.d/maven-mirror.gradle)
#   DOCKER_BUILD_CACHE_DIR  optional directory for a persistent BuildKit layer cache (docker buildx, type=local; used by CI)

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# name|dockerfile|build context (paths relative to the repo root)
IMAGES=(
  "ftgo-application|ftgo-application/Dockerfile|."
  "ftgo-flyway|ftgo-flyway/Dockerfile|ftgo-flyway"
)

DEFAULT_KIND_CLUSTER=ftgo
KIND_LOAD=
KIND_CLUSTER=$DEFAULT_KIND_CLUSTER

usage() {
  sed -n '3,13s/^# \{0,1\}//p' "${BASH_SOURCE[0]}"
}

while [ $# -gt 0 ]; do
  case "$1" in
    --kind-load)
      KIND_LOAD=yes
      if [ $# -gt 1 ] && [ "${2#-}" = "$2" ]; then
        KIND_CLUSTER=$2
        shift
      fi
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
  shift
done

if [ -z "${IMAGE_TAG:-}" ]; then
  IMAGE_TAG="$(git -C "$ROOT_DIR" rev-parse --short HEAD)"
  if [ -n "$(git -C "$ROOT_DIR" status --porcelain)" ]; then
    IMAGE_TAG="${IMAGE_TAG}-dirty"
  fi
fi

if [ -n "$KIND_LOAD" ]; then
  command -v kind > /dev/null || { echo "kind is not installed" >&2; exit 1; }
  if ! kind get clusters 2> /dev/null | grep -qx "$KIND_CLUSTER"; then
    echo "kind cluster '$KIND_CLUSTER' not found (create it with: kind create cluster --name $KIND_CLUSTER)" >&2
    exit 1
  fi
fi

BUILD_ARGS=()
if [ -n "${MAVEN_MIRROR_URL:-}" ]; then
  BUILD_ARGS+=(--build-arg "MAVEN_MIRROR_URL=$MAVEN_MIRROR_URL")
fi

BUILT=()
for spec in "${IMAGES[@]}"; do
  IFS='|' read -r name dockerfile context <<< "$spec"
  echo "==> Building $name:$IMAGE_TAG and $name:dev"
  BUILD_CMD=(docker build)
  if [ -n "${DOCKER_BUILD_CACHE_DIR:-}" ]; then
    BUILD_CMD=(docker buildx build --load
      --cache-from "type=local,src=$DOCKER_BUILD_CACHE_DIR/$name"
      --cache-to "type=local,dest=$DOCKER_BUILD_CACHE_DIR/$name.new,mode=max")
  fi
  "${BUILD_CMD[@]}" "${BUILD_ARGS[@]+"${BUILD_ARGS[@]}"}" \
    -f "$ROOT_DIR/$dockerfile" \
    -t "$name:$IMAGE_TAG" \
    -t "$name:dev" \
    "$ROOT_DIR/$context"
  if [ -n "${DOCKER_BUILD_CACHE_DIR:-}" ]; then
    # --cache-to type=local never prunes, so replace the cache instead of growing it.
    rm -rf "${DOCKER_BUILD_CACHE_DIR:?}/$name"
    mv "$DOCKER_BUILD_CACHE_DIR/$name.new" "$DOCKER_BUILD_CACHE_DIR/$name"
  fi
  BUILT+=("$name:$IMAGE_TAG" "$name:dev")
done

if [ -n "$KIND_LOAD" ]; then
  for image in "${BUILT[@]}"; do
    echo "==> Loading $image into kind cluster '$KIND_CLUSTER'"
    kind load docker-image "$image" --name "$KIND_CLUSTER"
  done
fi

echo "Built images:"
printf '  %s\n' "${BUILT[@]}"
