import asyncio
import json

# ---- Parameters (edit per run) ------------------------------------------------
REPO = "COG-GTM/ftgo-monolith"
START_BRANCH = "devin/1792164290-test-coverage"  # branch the run stacks on (e.g. master or an open coverage PR)
RUN_TAG = "1792509769-coverage-90"  # unique per run; used in branch names
TARGET_LINE_PCT = 90
GROUP_BELOW_MISSED = 10  # modules with fewer missed lines are grouped into one "misc" unit
TEST_SUPPORT_MODULES = ["ftgo-end-to-end-tests-common", "ftgo-test-util"]
# --------------------------------------------------------------------------------

BASE_BRANCH = f"devin/{RUN_TAG}-base"

CONTEXT = f"""Repository: https://github.com/{REPO} (Gradle multi-module, Spring Boot 2.0.3, Java 8).
Environment facts (verified):
- Gradle 4.10.2 runs ONLY on JDK 8: `export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH`.
- JaCoCo (0.8.2) is already wired: per-module `jacocoTestReport` (XML at <module>/build/reports/jacoco/test/jacocoTestReport.xml) and root `jacocoRootReport` (build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml).
- Unit tests run without a DB: `./gradlew -PskipDbTests -x :ftgo-end-to-end-tests:test test jacocoTestReport jacocoRootReport`. A single module: `./gradlew -PskipDbTests :<module>:test :<module>:jacocoTestReport`.
- Test classpath: JUnit 4, Mockito, Hamcrest 1.3 (no aMapWithSize etc.), spring-test. Controllers use field @Autowired: use @RunWith(MockitoJUnitRunner.class) + @InjectMocks and standalone MockMvc. For Money JSON register `new MoneyModule()` on the ObjectMapper; Money serializes as a string ("12.34").
- Don't assert Jackson-serialized LocalDateTime against toString() (flaky); use fixed timestamps.
- Exception->HTTP mapping lives in ftgo-application's GlobalExceptionHandler and is NOT loaded in service-module standalone MockMvc tests.
- When creating a branch from a remote branch, push with `git push -u origin <branch>` (a plain push may go to the tracked base branch).
Rules for all test work:
- Never change production code under src/main. If behavior looks buggy, write a test that PINS the current behavior, name it so the bug is obvious (e.g. `..._currentlyThrowsNpe_bug`), and report it.
- Tests must be meaningful (assert observable behavior/values), deterministic, and DB-free. No tests that only call getters with no assertions. No coverage excludes added to make numbers go up.
"""

BASELINE_SCHEMA = {
    "type": "object",
    "properties": {
        "branch": {"type": "string"},
        "root_line_pct_before_exclusion": {"type": "number"},
        "root_line_pct": {"type": "number"},
        "root_branch_pct": {"type": "number"},
        "modules_json": {
            "type": "string",
            "description": 'JSON array: [{"module": "ftgo-domain", "line_missed": 14, "line_covered": 300, "classes_with_missed_lines": ["net.x.Foo (6)"]}] for every module in the aggregate',
        },
        "notes": {"type": "string"},
    },
    "required": ["branch", "root_line_pct", "root_branch_pct", "modules_json"],
}

WRITE_SCHEMA = {
    "type": "object",
    "properties": {
        "branch": {"type": "string"},
        "modules_before_after": {"type": "string", "description": "per module: line% before -> after, branch% before -> after (N/A if no branches)"},
        "tests_added": {"type": "integer"},
        "test_files": {"type": "array", "items": {"type": "string"}},
        "pinned_bugs": {"type": "array", "items": {"type": "string"}},
        "all_tests_pass": {"type": "boolean"},
        "summary": {"type": "string"},
    },
    "required": ["branch", "modules_before_after", "tests_added", "all_tests_pass", "summary"],
}

REVIEW_SCHEMA = {
    "type": "object",
    "properties": {
        "accepted": {"type": "boolean"},
        "findings": {"type": "string", "description": "Concrete, actionable issues; empty if accepted"},
        "verified_tests_pass": {"type": "boolean"},
        "verified_coverage": {"type": "string"},
    },
    "required": ["accepted", "findings", "verified_tests_pass"],
}

COMBINE_SCHEMA = {
    "type": "object",
    "properties": {
        "branch": {"type": "string"},
        "head_sha": {"type": "string"},
        "root_line_pct": {"type": "number"},
        "root_branch_pct": {"type": "number"},
        "gate_min_line_pct": {"type": "number"},
        "per_module_table_markdown": {"type": "string"},
        "merged_branches": {"type": "array", "items": {"type": "string"}},
        "skipped_branches": {"type": "array", "items": {"type": "string"}},
        "pinned_bugs": {"type": "array", "items": {"type": "string"}},
        "all_tests_pass": {"type": "boolean"},
        "notes": {"type": "string"},
    },
    "required": ["branch", "head_sha", "root_line_pct", "root_branch_pct", "per_module_table_markdown", "all_tests_pass"],
}


async def baseline():
    prompt = f"""{CONTEXT}
TASK (baseline for a coverage push; do NOT open a PR):
1. Check out `{START_BRANCH}` and create branch `{BASE_BRANCH}` from it.
2. Run the full DB-free unit test + JaCoCo command and record the root aggregate LINE % (this is `root_line_pct_before_exclusion`).
3. In the root build.gradle, exclude test-support modules {json.dumps(TEST_SUPPORT_MODULES)} from `coverageProjects` (they hold end-to-end test harness code under src/main, e.g. AbstractEndToEndTests, which is not production code and is only exercised by the DB/app-backed e2e suite). Keep everything else unchanged and keep each module's own jacocoTestReport working.
4. Re-run the command; confirm all tests pass. Parse the aggregate XML and every module XML.
5. Commit ("build: exclude test-support modules from aggregate coverage") and `git push -u origin {BASE_BRANCH}`.
Report: branch, root line/branch % (after exclusion), the before-exclusion line %, and modules_json covering EVERY module in the aggregate with line missed/covered and the classes that still have missed lines (with missed-line counts)."""
    return await agent(prompt, phase="baseline", schema=BASELINE_SCHEMA, label="baseline", repos=[REPO], soft_time_limit_minutes=25)


def plan_units(modules):
    todo = sorted((m for m in modules if m["line_missed"] > 0 and m["module"] not in TEST_SUPPORT_MODULES),
                  key=lambda m: (-m["line_missed"], m["module"]))
    units, misc = [], []
    for m in todo:
        (units if m["line_missed"] >= GROUP_BELOW_MISSED else misc).append(m)
    plan = [{"name": m["module"], "modules": [m]} for m in units]
    if misc:
        plan.append({"name": "misc", "modules": misc})
    return plan


def write_prompt(unit, findings=None, branch=None):
    mods = json.dumps(unit["modules"], sort_keys=True, indent=1)
    names = ", ".join(m["module"] for m in unit["modules"])
    target_branch = branch or f"devin/{RUN_TAG}-{unit['name']}"
    if findings is None:
        head = f"""TASK: raise unit-test coverage for module(s): {names}. Target: cover every missed line listed below where a meaningful test is possible (overall project goal: >= {TARGET_LINE_PCT}% aggregate line coverage).
Start from branch `{BASE_BRANCH}`; create `{target_branch}` from it."""
    else:
        head = f"""TASK: REVISE your earlier test work for module(s): {names} on existing branch `{target_branch}` (check it out; it is based on `{BASE_BRANCH}`).
A reviewer requested these changes - address every point:
{findings}"""
    return f"""{CONTEXT}
{head}
Current coverage gaps (from JaCoCo on `{BASE_BRANCH}`):
{mods}
Scope rules: ONLY add or edit files under <module>/src/test/** for the listed module(s) (and that module's own build.gradle testCompile deps if strictly needed). Do NOT touch the root build.gradle, other modules, or any src/main code. Do not open a PR.
Verify: run `./gradlew -PskipDbTests :<module>:test :<module>:jacocoTestReport` for each listed module, all tests green; measure before/after from the module XML. Run each new test class 3 times to check determinism.
Commit and `git push -u origin {target_branch}`. Report the branch, before->after coverage per module, test count, files, and any pinned bugs."""


def review_prompt(unit, result):
    names = ", ".join(m["module"] for m in unit["modules"])
    return f"""{CONTEXT}
TASK: review (do not rewrite) the unit-test branch `{result['branch']}` against base `{BASE_BRANCH}` for module(s): {names}.
Author's report: {json.dumps(result, sort_keys=True)}
Check out the branch, run `git diff {BASE_BRANCH}...HEAD`, run the module tests + jacocoTestReport twice, and verify:
1. Only <module>/src/test/** (or that module's build.gradle test deps) changed; no src/main or root build.gradle edits.
2. Tests pass and are deterministic; no time/order flakiness.
3. Assertions are meaningful (no assertion-free tests, no tautologies, no tests that just mirror implementation).
4. Bugs are pinned, not silently fixed, and pinned-bug tests are clearly named.
5. The reported coverage numbers match what JaCoCo shows.
Accept if all hold (minor nits are fine). Otherwise list concrete, actionable findings. Do not push anything."""


async def unit_flow(unit):
    label = unit["name"]
    try:
        result = await agent(write_prompt(unit), phase="write", schema=WRITE_SCHEMA, label=f"write-{label}",
                             repos=[REPO], soft_time_limit_minutes=35)
    except WorkflowAgentError as e:
        log(f"{label}: writer failed ({e}); skipping unit")
        return {"unit": label, "status": "writer_failed"}
    try:
        review = await agent(review_prompt(unit, result), phase="review", schema=REVIEW_SCHEMA,
                             label=f"review-{label}", repos=[REPO], soft_time_limit_minutes=20)
    except WorkflowAgentError as e:
        log(f"{label}: reviewer failed ({e}); keeping unreviewed branch")
        return {"unit": label, "status": "unreviewed", "result": result}
    if review["accepted"]:
        log(f"{label}: accepted on first review")
        return {"unit": label, "status": "accepted", "result": result, "review": review}
    log(f"{label}: revision requested")
    try:
        result = await agent(write_prompt(unit, review["findings"], result["branch"]), phase="write", schema=WRITE_SCHEMA,
                             label=f"revise-{label}", repos=[REPO], soft_time_limit_minutes=30)
        review2 = await agent(review_prompt(unit, result), phase="review", schema=REVIEW_SCHEMA,
                              label=f"re-review-{label}", repos=[REPO], soft_time_limit_minutes=20)
    except WorkflowAgentError as e:
        log(f"{label}: revision round failed ({e})")
        return {"unit": label, "status": "revision_failed", "result": result, "review": review}
    status = "accepted_after_revision" if review2["accepted"] else "rejected"
    log(f"{label}: {status}")
    return {"unit": label, "status": status, "result": result, "review": review2}


async def combine(base, outcomes):
    mergeable = [o for o in outcomes if o["status"] in ("accepted", "accepted_after_revision", "unreviewed")]
    others = [o for o in outcomes if o not in mergeable]
    prompt = f"""{CONTEXT}
TASK: combine the reviewed coverage branches and add a ratcheting coverage gate. Do NOT open a PR (the orchestrator will).
1. Check out `{BASE_BRANCH}` (root line coverage there: {base['root_line_pct']}%, branch {base['root_branch_pct']}%).
2. Merge each of these branches (no-ff), in order: {json.dumps([o['result']['branch'] for o in mergeable])}. They touch disjoint module test dirs; resolve any conflict conservatively and report it. If a branch breaks the build, skip it and report it in skipped_branches.
3. Run the full DB-free test + JaCoCo command; all tests must pass.
4. In the root build.gradle add a `jacocoRootCoverageVerification` task (type JacocoCoverageVerification, same executionData/source/class dirs as jacocoRootReport) with a LINE COVEREDRATIO minimum = floor(achieved aggregate line %) / 100, and make root `check` depend on it. Verify `./gradlew -PskipDbTests -x :ftgo-end-to-end-tests:test check` passes on JDK 8 (if `check` pulls in DB-backed tests, also add `-x` for them and say which).
5. Commit and `git push origin {BASE_BRANCH}`.
Report: branch, head sha, final aggregate line/branch %, gate value, a markdown table (module | line % before | line % after | branch % after) using the per-module XMLs (before = `{BASE_BRANCH}` before merges; branch % 'N/A' when a module has no branches), merged/skipped branches, and the union of pinned bugs.
Pinned bugs reported by unit authors: {json.dumps(sorted({b for o in mergeable for b in o['result'].get('pinned_bugs', [])}))}
Units not merged (for your notes): {json.dumps([{'unit': o['unit'], 'status': o['status']} for o in others], sort_keys=True)}"""
    return await agent(prompt, phase="combine", schema=COMBINE_SCHEMA, label="combine", repos=[REPO], soft_time_limit_minutes=30)


async def main():
    await register_workflow({
        "name": "ftgo-test-coverage",
        "description": f"Raise ftgo-monolith unit-test line coverage to >= {TARGET_LINE_PCT}% starting from {START_BRANCH}",
        "product": REPO,
        "phases": [
            {"title": "baseline", "detail": "Exclude test-support modules from aggregate, measure per-module gaps", "count": 1},
            {"title": "write", "detail": "Per-module unit-test writers (plus one revision round if review asks)"},
            {"title": "review", "detail": "Independent reviewer per unit"},
            {"title": "combine", "detail": "Merge accepted branches, add coverage gate, final report", "count": 1},
        ],
    })
    base = await baseline()
    modules = json.loads(base["modules_json"])
    log(f"baseline: line {base.get('root_line_pct_before_exclusion')}% -> {base['root_line_pct']}% after excluding test-support; branch {base['root_branch_pct']}%")
    units = plan_units(modules)
    log("units: " + ", ".join(f"{u['name']}({sum(m['line_missed'] for m in u['modules'])} missed)" for u in units))
    if not units:
        log("no coverage gaps left; nothing to do")
        return
    outcomes = await parallel([lambda u=u: unit_flow(u) for u in units])
    for o in outcomes:
        log(f"unit {o['unit']}: {o['status']}" + (f" branch={o['result']['branch']}" if 'result' in o else ""))
    final = await combine(base, outcomes)
    log("FINAL " + json.dumps(final, sort_keys=True))


asyncio.run(main())
