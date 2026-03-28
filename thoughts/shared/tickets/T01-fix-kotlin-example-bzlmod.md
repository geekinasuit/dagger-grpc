---
id: T01
title: Update Kotlin example BUILD files to canonical Bzlmod repo names (hygiene)
priority: low
phase: infra
status: open
blocked_by: T00
---

## Summary

The `examples/io_grpc/bazel_build_kt` example BUILD files use old WORKSPACE-style repository names that predate Bzlmod. **The build and tests currently pass** (confirmed in T00), so this is a hygiene ticket only — update to canonical names to avoid relying on undocumented compatibility behaviour that may break in a future Bazel version.

## Background

Originally filed as HIGH priority, suspected broken. T00 confirmed:
- `../../bin/bazel build //...` passes ✅
- `../../bin/bazel test //...` passes ✅

The old names appear to resolve correctly, either as Bzlmod compatibility aliases or via transitive module resolution.

## Stale Names to Update

| File | Old name | Canonical Bzlmod name |
|---|---|---|
| `proto/BUILD.bazel:1` | `@com_google_protobuf//bazel:proto_library.bzl` | `@protobuf//bazel:proto_library.bzl` |
| `proto/BUILD.bazel:2` | `@grpc-kotlin//:kt_jvm_grpc.bzl` | TBD — see note below |
| `service/armeria/BUILD.bazel:1` | `@io_bazel_rules_kotlin//kotlin:jvm.bzl` | `@rules_kotlin//kotlin:jvm.bzl` |
| `client/BUILD.bazel:1` | `@io_bazel_rules_kotlin//kotlin:jvm.bzl` | `@rules_kotlin//kotlin:jvm.bzl` |

**Note on `@grpc-kotlin`:** The `grpc-kotlin` module is not declared as a `bazel_dep` in the example's `MODULE.bazel`. It builds anyway — presumably via transitive resolution from `grpc-java` or another dep. Clarify the correct canonical reference before updating `proto/BUILD.bazel`. Alternatively, migrate the Kotlin example proto compilation to use `java_grpc_library` + `java_proto_library` (same approach as the Java example), since the test BUILD files already note "kotlin proto generation relies on the java anyway."

## Acceptance Criteria

- [ ] `proto/BUILD.bazel` uses `@protobuf` (not `@com_google_protobuf`)
- [ ] `service/armeria/BUILD.bazel` uses `@rules_kotlin` (not `@io_bazel_rules_kotlin`)
- [ ] `client/BUILD.bazel` uses `@rules_kotlin` (not `@io_bazel_rules_kotlin`)
- [ ] `proto/BUILD.bazel` either uses the canonical `@grpc-kotlin` name (declared in MODULE.bazel) or migrates to `java_grpc_library` + `java_proto_library`
- [ ] `../../bin/bazel build //...` and `../../bin/bazel test //...` continue to pass after changes

## Implementation Notes

- This is a safe, mechanical rename for `@com_google_protobuf` and `@io_bazel_rules_kotlin` — confirmed aliases are stable.
- The `@grpc-kotlin` situation needs a quick investigation before changing: check the example's `MODULE.bazel.lock` to see which module is actually providing it. Easiest fix may be to align with the Java example and drop Kotlin-specific proto generation entirely.
- No source code changes required, only BUILD file load statement updates.
