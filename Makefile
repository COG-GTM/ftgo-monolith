# Local Kubernetes workflow for FTGO: kind cluster + Helm chart. See deployment/kind/README.md.
#
#   make kind-demo   create the cluster, build and load images, deploy, smoke test and run the end-to-end tests
#   make kind-down   delete the cluster
#
# Release lifecycle demos against a deployed release (deployment/kind/README.md, "Release lifecycle"):
#   make helm-test | demo-helm-test | demo-upgrade | demo-rollback | demo-reinstall

KIND_VERSION    ?= v0.24.0
KUBECTL_VERSION ?= v1.31.0
HELM_VERSION    ?= v3.16.2

CLUSTER      ?= ftgo
RELEASE      ?= ftgo
NAMESPACE    ?= default
CHART        := deployment/helm/ftgo
KIND_CONFIG  := deployment/kind/kind-config.yaml
KIND_VALUES  := $(CHART)/values-kind.yaml
APP_URL      := http://localhost:8081
IMAGE_TAG_FILE := build/kind-image-tag

KUBE_CONTEXT := kind-$(CLUSTER)
HELM         := helm --kube-context $(KUBE_CONTEXT) --namespace $(NAMESPACE)
KUBECTL      := kubectl --context $(KUBE_CONTEXT) --namespace $(NAMESPACE)

# Environment for scripts/demo-*.sh.
DEMO_ENV     := CLUSTER='$(CLUSTER)' RELEASE='$(RELEASE)' NAMESPACE='$(NAMESPACE)' CHART='$(CHART)' \
                KIND_VALUES='$(KIND_VALUES)' APP_URL='$(APP_URL)' IMAGE_TAG_FILE='$(IMAGE_TAG_FILE)'
REPLICAS     ?= 2

export KIND_VERSION KUBECTL_VERSION HELM_VERSION

.PHONY: kind-demo tools kind-up images deploy smoke e2e kind-down \
        helm-test demo-helm-test demo-upgrade demo-rollback demo-reinstall

# kind-demo's prerequisites are sequential steps.
.NOTPARALLEL:

kind-demo: kind-up images deploy smoke e2e

tools:
	@scripts/kind-check-tools.sh

kind-up: tools
	@if kind get clusters 2>/dev/null | grep -qx '$(CLUSTER)'; then \
	  if docker port '$(CLUSTER)-control-plane' 30081/tcp 2>/dev/null | grep -q ':8081$$'; then \
	    echo "kind cluster '$(CLUSTER)' already exists"; \
	  else \
	    echo "ERROR: kind cluster '$(CLUSTER)' exists but does not map localhost:8081; run 'make kind-down' first" >&2; exit 1; \
	  fi; \
	else \
	  kind create cluster --name '$(CLUSTER)' --config '$(KIND_CONFIG)' --wait 120s; \
	fi

# Every build gets a unique tag (<sha>[-dirty]-<epoch>), so each rebuild changes the pod template and rolls the app.
images: tools
	@mkdir -p '$(dir $(IMAGE_TAG_FILE))'
	@tag=$$(git rev-parse --short HEAD); \
	if [ -n "$$(git status --porcelain)" ]; then tag="$$tag-dirty"; fi; \
	tag="$$tag-$$(date +%s)"; \
	IMAGE_TAG="$$tag" scripts/build-images.sh --kind-load '$(CLUSTER)' && echo "$$tag" > '$(IMAGE_TAG_FILE)'

# Deploys the tag from the last `make images`, or the chart default (dev) if there is none.
deploy: tools
	$(HELM) upgrade --install '$(RELEASE)' '$(CHART)' -f '$(KIND_VALUES)' --create-namespace \
	  $$(tag=$$(cat '$(IMAGE_TAG_FILE)' 2>/dev/null) && echo "--set-string app.image.tag=$$tag --set-string migrations.image.tag=$$tag") \
	  --wait --timeout 10m
	$(KUBECTL) get pods -l app.kubernetes.io/instance='$(RELEASE)'

smoke:
	curl -fsS --retry 10 --retry-delay 3 --retry-all-errors '$(APP_URL)/actuator/health'
	@echo
	curl -fsS -o /dev/null '$(APP_URL)/' && echo "UI OK: $(APP_URL)/"

e2e:
	@"$${JAVA_HOME:+$$JAVA_HOME/bin/}java" -version 2>&1 | grep -q '"1\.8\.' || \
	  { echo "ERROR: the Gradle build needs Java 8 (set JAVA_HOME to a JDK 8)" >&2; exit 1; }
	DOCKER_HOST_IP=localhost ./gradlew :ftgo-end-to-end-tests:cleanTest :ftgo-end-to-end-tests:test

helm-test: tools
	$(HELM) test '$(RELEASE)' --logs --hide-notes --timeout 3m

demo-helm-test: tools
	$(DEMO_ENV) scripts/demo-helm-test.sh

demo-upgrade: tools
	$(DEMO_ENV) REPLICAS='$(REPLICAS)' scripts/demo-upgrade.sh

demo-rollback: tools
	$(DEMO_ENV) scripts/demo-rollback.sh

demo-reinstall: tools
	$(DEMO_ENV) scripts/demo-reinstall.sh $(if $(CLEAN),--clean)

kind-down: tools
	kind delete cluster --name '$(CLUSTER)'
