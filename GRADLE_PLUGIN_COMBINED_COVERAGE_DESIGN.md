# Gradle plugin combined coverage design

Status: instrumentation spike passed; opt-in combined collection implemented in test support.
Verification results and fallback are recorded below.

## Goal and current gap

Measure production plugin execution across unit, Functional/TestKit and real-codegen
E2E tests in one `gradle-plugin` Codecov report. Preserve the existing verification
ownership: unit tests cover isolated decisions; TestKit covers Gradle wiring;
E2E covers real generation/compilation. Coverage does not replace assertions.

The original configuration enabled JaCoCo 0.8.15 only on `test` and reports
`build/jacoco/test.exec`. Functional/E2E test JVMs are not instrumented. More
importantly, those JVMs launch separate Gradle builds through `GradleRunner`:
the plugin executes in nested Gradle daemons, not in the JUnit runner. Enabling
JaCoCo on the outer test tasks alone would not capture that execution.
The original CI uploaded unit coverage before `e2eTest` ran.

The canonical Boot application stack stays entirely outside this design, as do
Maven codegen internals, upstream OAG, generated sources and fixture applications.
The report denominator remains **all production classes of this Gradle plugin**;
no low-coverage class exclusions or threshold changes are proposed.

## Collection path

1. Keep the existing unit report/data unchanged for diagnostics and fallback.
   Add an explicit, opt-in combined-coverage mode; ordinary verification remains
   usable without a coverage agent.
2. Resolve the matching JaCoCo agent runtime and dump tooling as test-only
   dependencies. In shared TestKit support, augment the nested build's
   `org.gradle.jvmargs` with an absolute `-javaagent` path, preserving its existing
   fixture JVM arguments (or explicit test defaults). Cover every functional/E2E runner entry point, including
   alternate Gradle versions and expected-failure builds. Do not use global
   `JAVA_TOOL_OPTIONS`, modify production plugin code, or instrument published jars.
3. Instrument only `io.github.blueprintplatform.openapi.generics.gradle.*` in the
   nested JVM. This limits collection to the target module; report against the
   unchanged `sourceSets.main` classes and sources. The isolated repository must
   publish exactly those class bytes. Reject class-ID mismatches rather than
   silently losing nested coverage.
4. Use agent `output=tcpclient,address=127.0.0.1,dumponexit=false`. The harness
   binds a loopback listener before starting Gradle: no free-port reservation race.
   After each synchronous `build()` / `buildAndFail()`, request a dump over that
   connection without resetting probes. Cumulative snapshots merge safely. Keep
   collection outside Gradle lifecycle callbacks; preserve original build failures
   and attach collection errors rather than masking them.
5. Keep one session/connection per test JVM and Gradle distribution, with a private
   TestKit directory per suite/distribution. Stable agent arguments within a suite
   permit daemon and configuration-cache reuse across fixtures. Separate fixture
   project directories and existing assertions remain unchanged. TestKit owns its
   daemons; this helper neither starts a daemon-control framework nor stops global
   Gradle daemons. Close harness sockets at test-JVM shutdown. A lost daemon/session
   fails collection instead of attempting a hidden restart/recovery.
6. Serialize coverage-mode builds and disable JUnit parallel execution in that mode.
   Bound socket operations to ten seconds. Validate session identity and plugin
   class IDs on every dump; reject empty/mismatched data. Persist unique snapshots
   under `build/jacoco/nested/<suite>/<session>-<invocation>.exec`. Clear each suite's
   old data before its tests, disable outer Test task caching/up-to-date reuse in
   coverage mode, and keep nested cache assertions intact. TestKit dependency caches
   may persist; coverage data cannot carry over between runs.

The initial TCP-server spike captured plugin bootstrap **12/12** and template task
adapter **21/21** lines, with matching source/class IDs and second-build cache reuse.
The TCP-client implementation then passed both existing configuration-cache tests.
Only test support/build/CI configuration changes; production plugin classes and
published artifacts are unchanged.

## Report and CI wiring

Implemented flow:

```text
test -> test.exec ----------------------------------+
functionalTest -> nested/functionalTest/*.exec -------+-> jacocoCombinedReport
e2eTest -> nested/e2eTest/*.exec ----------------------+   -> combined XML + HTML
                                                        -> Codecov: gradle-plugin
```

`jacocoCombinedReport` consumes all execution files (an explicit binary merge
is optional), with only main production class/source directories. Retain the unit
HTML report and per-suite raw snapshots for diagnosis. Require fresh, nonempty
data from all three layers and validate expected plugin classes/session identities;
missing nested data must not masquerade as a successful combined report.

CI moves the single `gradle-plugin` upload after both `check` and `e2eTest` and report
generation. It uploads only combined XML, keeping the existing flag, source paths
and status policy.
Record the combined baseline separately from the current unit-only
76.01% line / 79.62% branch baseline: the measurement changed, not test quality.
The increase represents newly observed execution, not additional behavioral tests.

## Acceptance and fallback

- A small fixture proves bootstrap/wiring and task-action hits absent from its
  outer runner; unit, nested and report class IDs agree without mismatch warnings.
- Full `check` and `e2eTest` pass with and without collection. Existing negative,
  mixed-task, real compilation and minimum/current compatibility evidence remains.
- First-build/store and second-build/reuse assertions still pass under the agent,
  including the minimum tested Gradle distribution; no cache checks are relaxed.
- Two fresh coverage runs produce the same covered lines/branches, with no stale
  files, missing sessions, port collisions or hanging daemons. Record overhead.
  Parallel collection remains unsupported; it needs a separate isolation check.
- One fresh combined XML reaches the existing Codecov flag; canonical Boot tests
  and their sources never enter its execution inputs or denominator.

Agent/classloader interaction, live-daemon loss, JVM-argument changes, shared
endpoints and concurrent writes are the main risks. Do not use shared append files
or assume `--no-daemon` makes TestKit execution data flush reliably. Unexpected JVM
termination makes that coverage run incomplete; fail it rather than reporting zero.

If collection becomes unreliable, omit `-PcombinedCoverage=true` and restore
the existing unit XML upload. Keep Functional/TestKit/E2E as behavioral verification.
Never silently fall back within a run labelled combined, suppress missed classes,
or weaken assertions to obtain a percentage. No production API rollback is needed. The fallback command is
`./gradlew check e2eTest`; upload `build/reports/jacoco/test/jacocoTestReport.xml`.

## Verification result

- Three fresh-data combined runs passed: **65 unit + 26 functional + 3 E2E** tests.
  Every source-line instruction/branch counter matched between runs; no class-ID
  mismatches, missing sessions or port failures. Minimum/current Gradle cache
  reuse and all existing negative/real-codegen assertions passed.
- Unit-only remains **545/717 lines (76.01%), 125/157 branches (79.62%)**.
  Combined is **699/717 lines (97.49%), 134/157 branches (85.35%)**: identical
  production denominator, newly observed execution, no added production tests.
  Bootstrap, template lifecycle, integration, source registrar, task configurer and
  preparation adapter all have 100% line coverage in the combined report.
- Agent-free `./gradlew check e2eTest --rerun-tasks` passed all 94 tests in 32s.
  Initial isolated-cache combined runs took 2m27s / 2m23s; those timings include
  cold private TestKit caches and do not isolate agent overhead.
- Final warm-cache combined run passed in **29s**, with identical source-line and
  branch counters. This is not an isolated overhead benchmark: the agent-free 32s
  run also forced recompilation, whereas the final combined run reused compiled
  inputs. No steady-state slowdown was evident; cold private-cache setup is the
  visible initial cost. Remote CI/Codecov upload remains unverified until the
  pushed workflow completes.

References: [TestKit](https://docs.gradle.org/current/userguide/test_kit.html),
[JaCoCo agent](https://www.jacoco.org/jacoco/trunk/doc/agent.html),
[dump/merge/report tooling](https://www.jacoco.org/jacoco/trunk/doc/cli.html).
