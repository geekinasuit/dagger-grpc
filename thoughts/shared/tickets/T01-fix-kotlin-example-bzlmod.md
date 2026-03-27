---
id: T01
title: Fix Kotlin example Bzlmod migration
priority: high
phase: infra
status: open
blocked_by: T00
---

## Summary

The `examples/io_grpc/bazel_build_kt` example uses stale WORKSPACE-style repository names (`@io_bazel_rules_kotlin`, `@grpc-kotlin`) that are not declared in its `MODULE.bazel`. This example is likely broken under the current Bzlmod setup.

## Motivation

Both examples are standalone Bazel workspaces treated as real consumer projects in CI. A broken Kotlin example means the KSP processor path is untested end-to-end in CI.

## Affected Files

- `examples/io_grpc/bazel_build_kt/MODULE.bazel` — missing `grpc-kotlin` bazel_dep
- `examples/io_grpc/bazel_build_kt/proto/BUILD.bazel` — loads from `@grpc-kotlin//:kt_jvm_grpc.bzl`
- `examples/io_grpc/bazel_build_kt/client/BUILD.bazel` — loads from `@io_bazel_rules_kotlin`
- `examples/io_grpc/bazel_build_kt/service/armeria/BUILD.bazel` — loads from `@io_bazel_rules_kotlin`

## Acceptance Criteria

- [ ] `bazel build //...` passes from `examples/io_grpc/bazel_build_kt/`
- [ ] `bazel test //...` passes from `examples/io_grpc/bazel_build_kt/`
- [ ] All load statements use current Bzlmod canonical repository names (`@rules_kotlin`, not `@io_bazel_rules_kotlin`)
- [ ] `grpc-kotlin` (or equivalent) is declared as a `bazel_dep` in the example `MODULE.bazel`, or proto/gRPC generation is handled via an approach consistent with the root MODULE.bazel

## Implementation Notes

- The root `MODULE.bazel` does not declare `grpc-kotlin` as a dep. Check if `grpc-java` + `rules_proto_grpc_java` (already declared) can generate the Kotlin stubs, or if `grpc-kotlin` needs to be added.
- `@io_bazel_rules_kotlin` → `@rules_kotlin` is a straightforward rename for all load statements.
- Consider whether the proto compilation pipeline should be unified between the Java and Kotlin examples (both could use `java_grpc_library` + `java_proto_library` since the Kotlin note in the test BUILD files says "kotlin proto generation relies on the java anyway").
