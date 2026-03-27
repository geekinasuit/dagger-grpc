---
id: T02
title: Pin Kotlin toolchain version explicitly
priority: medium
phase: infra
status: open
blocked_by: T00
---

## Summary

The root `BUILD.bazel` has a commented-out `define_kt_toolchain` call. As a result, the build uses the default Kotlin toolchain bundled with `rules_kotlin 2.1.0` with no explicit language/API version or JVM target pinned.

## Motivation

Unpinned toolchain means:
- Kotlin language version can silently change when `rules_kotlin` is upgraded
- JVM bytecode target is unspecified, which can cause compatibility issues between modules
- Reproducibility is weakened

## Affected Files

- `BUILD.bazel:3-8` — commented-out `define_kt_toolchain` invocation
- `examples/io_grpc/bazel_build_java/BUILD.bazel` — may need a toolchain too
- `examples/io_grpc/bazel_build_kt/BUILD.bazel` — may need a toolchain too

## Acceptance Criteria

- [ ] `define_kt_toolchain` is uncommented and configured in the root `BUILD.bazel`
- [ ] Explicit Kotlin language version, API version, and JVM target are set
- [ ] Both example sub-workspaces also pin the same toolchain (or inherit it if possible)
- [ ] `bazel build //...` and `bazel test //...` continue to pass

## Implementation Notes

- Decide on Kotlin language/API version: given KSP pin of `1.9.0-1.0.12`, the Kotlin version should be 1.9.x. Consider whether upgrading to Kotlin 2.x is also in scope here or a separate ticket (note: T08 mentions kotlin-compile-testing is broken with Kotlin 2).
- JVM target: JVM 11 or 17 are reasonable choices for a 2024+ project.
- `rules_kotlin` 2.1.0 toolchain configuration reference: https://github.com/bazelbuild/rules_kotlin
