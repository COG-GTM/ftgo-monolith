---
name: ftgo-test-coverage
description: Raise ftgo-monolith unit-test coverage with a dynamic workflow (baseline -> per-module test writers -> reviewer per unit -> combine + coverage gate). Use when asked to increase/restore test coverage in this repo.
---

# ftgo-test-coverage workflow

Runs `workflow.py` (next to this file) with the `run_workflow` tool. Invoke the builtin
`dynamic-workflows` skill first.

## Before running
1. Edit the parameter block at the top of `workflow.py`:
   - `START_BRANCH`: branch to stack on (`master`, or an open coverage branch).
   - `RUN_TAG`: unique per run, e.g. `$(date +%s)-coverage`. It names every pushed branch.
   - `TARGET_LINE_PCT`, `GROUP_BELOW_MISSED` (modules with fewer missed lines share one "misc" agent).
2. Tell the user the plan and ACU cost (roughly 2-3 ACUs per unit agent), then call
   `run_workflow(workflow_name="ftgo-test-coverage", script_path=<abs path to workflow.py>)`.

## What it does
- **baseline** (1 agent): branches `devin/<RUN_TAG>-base` from `START_BRANCH`, excludes test-support
  modules (`ftgo-end-to-end-tests-common`, `ftgo-test-util`) from `jacocoRootReport`, and reports per-module gaps.
- **write** (1 agent per module with gaps): adds DB-free tests only under `<module>/src/test/**`, pins
  existing bugs instead of fixing them, and pushes `devin/<RUN_TAG>-<module>`.
- **review** (1 per unit, plus at most one revise/re-review round).
- **combine** (1 agent): merges accepted branches into the base branch, adds/updates
  `jacocoRootCoverageVerification` (wired into root `check`) at floor(achieved line %), and pushes.
  CI (`./gradlew build`) runs the gate via a root `build` task. The workflow fails if final coverage < `TARGET_LINE_PCT`; unreviewed branches are never merged. It does not open a PR. The orchestrator opens one from `devin/<RUN_TAG>-base` into `START_BRANCH`.

The fan-out comes from the baseline agent's JaCoCo output, so a rerun only targets modules that still have gaps.
Resume an interrupted run by passing its `run_id`.

## Local verification (JDK 8)
```
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 PATH=$JAVA_HOME/bin:$PATH
./gradlew -PskipDbTests -x :ftgo-end-to-end-tests:test clean check jacocoRootReport
```
