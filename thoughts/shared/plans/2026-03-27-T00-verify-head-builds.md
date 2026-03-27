# T00: Verify HEAD Builds and Tests Pass — Implementation Plan

## Overview

Establish a clean, verified baseline: every workspace in the repository builds and tests pass at HEAD before any new feature work begins. One fix was required (missing `WORKSPACE.bazel`); three warnings are forwarded to existing tickets.

## Current State Analysis

Investigated by running actual builds. The repo has three independent Bazel workspaces:
- Root (`/`) — library modules, compilers, ksp-apt-bridge
- `examples/io_grpc/bazel_build_java/` — standalone Java example workspace
- `examples/io_grpc/bazel_build_kt/` — standalone Kotlin example workspace

### Key Discoveries

- **`WORKSPACE.bazel` missing at repo root** (`/WORKSPACE.bazel` does not exist). Bazel 8 Bzlmod's `local_path_override` requires the target directory to have either `WORKSPACE` or `WORKSPACE.bazel` to be recognised as a valid workspace root. Both example workspaces use `local_path_override(module_name = "dagger-grpc", path = "../../..")` and failed with: `No WORKSPACE file found in .../external/dagger-grpc~override`. Fix: add an empty `WORKSPACE.bazel` at the repo root.

- **`bin/bazel` is the correct invocation** — the Hermit-managed `bin/bazel` shim activates the full Hermit environment including the correct Java toolchain. Activating Hermit manually and running system `bazel` can result in a stale `JAVA_HOME` pointing to a removed JDK (`jdk-17.0.8_7.jdk`) and breaks the build.

- **Kotlin example is NOT broken** — T01 was filed as a suspected issue with `@io_bazel_rules_kotlin` / `@grpc-kotlin`. In practice, the Kotlin example builds and all tests pass. T01 is reduced in scope (see Spin-off Notes below).

## Desired End State

All three workspaces build and test cleanly. A developer cloning the repo and running `bin/bazel build //...` and `bin/bazel test //...` from each workspace gets a fully green result.

### Verification Commands

```
# Root
bin/bazel build //...    # must succeed
bin/bazel test //...     # must show: 4 tests pass

# Java example
cd examples/io_grpc/bazel_build_java
../../bin/bazel build //...   # must succeed
../../bin/bazel test //...    # must show: 2 tests pass

# Kotlin example
cd examples/io_grpc/bazel_build_kt
../../bin/bazel build //...   # must succeed
../../bin/bazel test //...    # must show: 2 tests pass
```

## What We're NOT Doing

- Fixing warnings (forwarded to existing tickets)
- Modifying any source code beyond the WORKSPACE.bazel stub
- Pinning or changing any dependency versions
- Writing any new tests (placeholder tests pass as-is)

## Implementation

### Phase 1: Add `WORKSPACE.bazel` to repo root

**File**: `WORKSPACE.bazel` (new, at repo root)

```
# Empty WORKSPACE.bazel — required by Bazel to recognise this directory as a
# workspace root when referenced via local_path_override from example sub-workspaces.
# All dependency management is done via MODULE.bazel (Bzlmod).
```

This is the only code change required. ✅ **Already done.**

### Phase 2: Verify all workspaces

Run the four build+test commands above and confirm all green. ✅ **Already done.**

Results:

| Workspace | Build | Tests |
|---|---|---|
| Root | ✅ | ✅ 4/4 — `DaggerGrpcAPTProcessorTest` (2 real assertions), `DaggerGrpcSymbolProcessorTest` (placeholder), `CommonTest` (placeholder), `ModelTest` (placeholder) |
| `bazel_build_java` | ✅ | ✅ 2/2 — `confirm_example_builds`, `ServiceTest` (placeholder) |
| `bazel_build_kt` | ✅ | ✅ 2/2 — `confirm_example_builds`, `ServiceTest` (placeholder) |

### Phase 3: Commit

Commit `WORKSPACE.bazel` via `jj commit`.

---

## Spin-off Notes (Warnings Observed, Not Fixed Here)

### Warning 1 — APT processor missing `@SupportedSourceVersion` → T09
```
warning: No SupportedSourceVersion annotation found on
com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcAPTProcessor, returning RELEASE_6.
```
The `DaggerGrpcAPTProcessor` class is missing `@SupportedSourceVersion(SourceVersion.RELEASE_17)`. Java annotation processors should declare the source version they support. Fix: add the annotation to `DaggerGrpcAPTProcessor.kt`. This is a one-liner; fold into **T09** (expand APT tests, which will touch the processor file anyway) or fix as a standalone cleanup.

### Warning 2 — KSP "No valid classes" in round 2 → T08 / T05
```
warning: [ksp] No valid classes were annotated with @GrpcServiceHandler
```
Appears during a second KSP processing round (after the first round generates adapter files). This is expected KSP behaviour — subsequent rounds re-invoke the processor on newly generated sources, which aren't annotated. The warning is harmless but noisy. Fix: in `DaggerGrpcSymbolProcessor.process()`, return early (or suppress the warning) when `resolver.getSymbolsWithAnnotation(...)` returns empty on round 2+. Note for **T05** (KSP module generation) and **T08** (KSP test fixes).

### Warning 3 — KAPT Kotlin 2 alpha → T02
```
warning: support for language version 2.0+ in kapt is in Alpha and
must be enabled explicitly. Falling back to 1.9.
```
KAPT (used by Dagger) is falling back to Kotlin 1.9 mode because the toolchain is unpinned. Fix: pin the Kotlin toolchain explicitly in `BUILD.bazel`. Tracked in **T02**.

### T01 status update — Kotlin example not broken
The Kotlin example builds and tests pass without modification. The suspected `@io_bazel_rules_kotlin` / `@grpc-kotlin` issues do not manifest in practice — the BUILD files load from these repos but the build system resolves them correctly via the current MODULE.bazel. **T01 should be revisited** to confirm whether any actual Bzlmod hygiene work remains, or closed if the example is genuinely clean.

---

## Success Criteria

### Automated Verification
- [ ] `bin/bazel build //...` from repo root exits 0
- [ ] `bin/bazel test //...` from repo root exits 0, shows `4 tests pass`
- [ ] `../../bin/bazel build //...` from `examples/io_grpc/bazel_build_java/` exits 0
- [ ] `../../bin/bazel test //...` from `examples/io_grpc/bazel_build_java/` exits 0, shows `2 tests pass`
- [ ] `../../bin/bazel build //...` from `examples/io_grpc/bazel_build_kt/` exits 0
- [ ] `../../bin/bazel test //...` from `examples/io_grpc/bazel_build_kt/` exits 0, shows `2 tests pass`

### Manual Verification
- None required — this is purely a build/test pass verification.

---

## References

- Ticket: `thoughts/shared/tickets/T00-verify-head-builds.md`
- Related: `thoughts/shared/tickets/T01-fix-kotlin-example-bzlmod.md` (status TBD)
- Related: `thoughts/shared/tickets/T02-pin-kotlin-toolchain.md`
- Related: `thoughts/shared/tickets/T08-unblock-ksp-tests.md`
- Related: `thoughts/shared/tickets/T09-expand-apt-tests.md`
