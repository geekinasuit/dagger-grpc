---
id: T00
title: Verify HEAD builds and tests pass at current implementation level
priority: high
phase: infra
status: done
blocks: all other tickets
---

## Summary

Before any new work begins, confirm the repository builds and tests pass cleanly at HEAD. This is the baseline. Identify and fix any regressions or broken state so subsequent tickets start from a known-good point.

## Motivation

The research identified several potential issues (stale Kotlin example build, commented-out toolchain config, partial implementations). Before writing new code it's important to know what "working at current level" means.

## Acceptance Criteria

- [x] `bazel build //...` passes from the repo root (all library + compiler targets)
- [x] `bazel test //...` passes from the repo root (all tests, including placeholder tests)
- [x] `bazel build //...` passes from `examples/io_grpc/bazel_build_java/`
- [x] `bazel test //...` passes from `examples/io_grpc/bazel_build_java/`
- [x] `bazel build //...` passes from `examples/io_grpc/bazel_build_kt/`
- [x] CI passes (or CI failures are all pre-existing and documented)
- [x] Any newly discovered build/test failures are either fixed here or filed as separate tickets

## Resolution

Verified 2026-04-21. All three workspaces build and test clean:
- Root: 26 targets built, 4/4 tests pass
- `examples/bazel_build_java/`: 10 targets built, 2/2 tests pass
- `examples/bazel_build_kt/`: 13 targets built (no test targets)

Remaining noise is pre-existing and non-blocking: protobuf module version mismatch (upstream grpc-java), kapt/Kotlin 2 alpha warning, duplicate guava/truth artifact versions from transitive deps. T08 (KSP test commented out) confirmed as a separate open ticket.

## Implementation Notes

- The Kotlin example (`bazel_build_kt`) is suspected broken due to stale Bzlmod dep references (`@grpc-kotlin`, `@io_bazel_rules_kotlin`). If confirmed broken, document the failure here and track the fix in T01 rather than blocking T00 on it.
- The root `bazel test //...` will run placeholder tests (empty bodies) — these should pass trivially.
- The two real APT processor tests (`DaggerGrpcAPTProcessorTest`) must pass.
- The KSP processor test is known-commented-out (tracked in T08); its placeholder should still compile and the empty test method should pass.
