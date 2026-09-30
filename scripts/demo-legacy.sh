#!/usr/bin/env bash
#
# The "before" part of the demo: what the legacy plain-YAML deployment (deleted in AB-412, still on LEGACY_REF) does on
# the kind cluster. Everything is applied with --dry-run=server, so nothing is created.
# Usage: scripts/demo-legacy.sh (or `make demo-legacy`). LEGACY_REF defaults to the last master commit before AB-412.

# shellcheck source=scripts/demo-lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/demo-lib.sh"

LEGACY_REF=${LEGACY_REF:-4823d1911675c108855ed2f9d01e0ee312df9435}
LEGACY_DIR=deployment/kubernetes

git -C "$ROOT_DIR" cat-file -e "$LEGACY_REF:$LEGACY_DIR" 2> /dev/null ||
  die "$LEGACY_REF has no $LEGACY_DIR (shallow clone? run: git fetch --unshallow); or set LEGACY_REF to a commit from before AB-412"

step "Legacy deployment files on $LEGACY_REF"
run git -C "$ROOT_DIR" ls-tree -r --name-only "$LEGACY_REF" -- "$LEGACY_DIR"

step "The deploy script: one glob for MySQL, one for app manifests that do not exist"
run git -C "$ROOT_DIR" show "$LEGACY_REF:$LEGACY_DIR/scripts/kubernetes-deploy-all.sh"
echo "App manifests matching */src/deployment/kubernetes/*.yml on $LEGACY_REF:"
git -C "$ROOT_DIR" ls-tree -r --name-only "$LEGACY_REF" | grep -E '^[^/]+/src/deployment/kubernetes/[^/]+\.yml$' || echo "  (none)"

step "Apply the legacy MySQL manifests to Kubernetes $("${KUBECTL[@]}" version -o json | jq -r .serverVersion.gitVersion) (server dry run)"
for file in stateful-services/ftgo-db-secret.yml stateful-services/ftgo-mysql-deployment.yml; do
  echo "\$ git show $LEGACY_REF:$LEGACY_DIR/$file | kubectl apply --dry-run=server -f -"
  manifest=$(git -C "$ROOT_DIR" show "$LEGACY_REF:$LEGACY_DIR/$file")
  output=$("${KUBECTL[@]}" apply --dry-run=server -f - <<< "$manifest" 2>&1) && status=0 || status=$?
  grep -v 'last-applied-configuration' <<< "$output" || true
  if [ "$file" = stateful-services/ftgo-mysql-deployment.yml ]; then
    if [ "$status" -eq 0 ] || ! grep -q 'no matches for kind "StatefulSet" in version "apps/v1beta1"' <<< "$output"; then
      die "expected the apps/v1beta1 StatefulSet to be rejected; got the output above (exit $status)"
    fi
  else
    [ "$status" -eq 0 ] || die "unexpected failure applying $file (exit $status)"
  fi
done
echo
echo "Result: the StatefulSet is rejected (apps/v1beta1 was removed in Kubernetes 1.16), and there are no app manifests."
echo "More failures, with apps/v1 patched in: docs/deployment/as-is.md, 'Before evidence'."
