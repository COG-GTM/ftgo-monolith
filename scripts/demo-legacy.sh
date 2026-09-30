#!/usr/bin/env bash
#
# The "before" part of the demo: what the legacy plain-YAML deployment (deleted in AB-412, still on LEGACY_REF) does on
# the kind cluster. Everything is applied with --dry-run=server, so nothing is created.
# Usage: scripts/demo-legacy.sh (or `make demo-legacy`). LEGACY_REF defaults to origin/master.

# shellcheck source=scripts/demo-lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/demo-lib.sh"

LEGACY_REF=${LEGACY_REF:-origin/master}
LEGACY_DIR=deployment/kubernetes

git -C "$ROOT_DIR" cat-file -e "$LEGACY_REF:$LEGACY_DIR" 2> /dev/null ||
  die "$LEGACY_REF has no $LEGACY_DIR; set LEGACY_REF to a commit from before AB-412 (git fetch origin master?)"

step "Legacy deployment files on $LEGACY_REF"
run git -C "$ROOT_DIR" ls-tree -r --name-only "$LEGACY_REF" -- "$LEGACY_DIR"

step "The deploy script: one glob for MySQL, one for app manifests that do not exist"
run git -C "$ROOT_DIR" show "$LEGACY_REF:$LEGACY_DIR/scripts/kubernetes-deploy-all.sh"
echo "App manifests matching */src/deployment/kubernetes/*.yml on $LEGACY_REF:"
git -C "$ROOT_DIR" ls-tree -r --name-only "$LEGACY_REF" | grep -E '^[^/]+/src/deployment/kubernetes/[^/]+\.yml$' || echo "  (none)"

step "Apply the legacy MySQL manifests to Kubernetes $("${KUBECTL[@]}" version -o json | jq -r .serverVersion.gitVersion) (server dry run)"
for file in stateful-services/ftgo-db-secret.yml stateful-services/ftgo-mysql-deployment.yml; do
  echo "\$ git show $LEGACY_REF:$LEGACY_DIR/$file | kubectl apply --dry-run=server -f -"
  git -C "$ROOT_DIR" show "$LEGACY_REF:$LEGACY_DIR/$file" |
    "${KUBECTL[@]}" apply --dry-run=server -f - 2>&1 | grep -v 'last-applied-configuration' || true
done
echo
echo "Result: the StatefulSet is rejected (apps/v1beta1 was removed in Kubernetes 1.16), and there are no app manifests."
echo "More failures, with apps/v1 patched in: docs/deployment/as-is.md, 'Before evidence'."
