---
id: T03
title: Resolve protobuf version skew between Bazel module and Maven
priority: low
phase: infra
status: open
blocked_by: T00
---

## Summary

There are two independent protobuf version pins in the build:
- `protobuf` Bazel module: `23.1` (used as `@protobuf//java/core`)
- Maven artifact: `com.google.protobuf:protobuf-java:4.29.3` (used as `@maven//:com_google_protobuf_protobuf_java`)

These are different versions of the same library appearing on the classpath in different parts of the build.

## Motivation

Version skew between protobuf runtime JARs can cause subtle runtime failures (e.g., generated message classes compiled against one version failing to interoperate with runtime from another). While the build may work today, this is a latent correctness risk.

## Acceptance Criteria

- [ ] Protobuf Java library is sourced from a single consistent version across the build
- [ ] Either all targets use `@protobuf//java/core` (Bazel module, update to a recent version) or all targets use `@maven//:com_google_protobuf_protobuf_java` (Maven, 4.29.3)
- [ ] No mixing of protobuf versions on any single compilation classpath
- [ ] `bazel build //...` and `bazel test //...` continue to pass

## Implementation Notes

- Protobuf Bazel module v23.1 corresponds to approximately protobuf-java 3.23.1 (old 3.x naming). The Maven pin of 4.29.3 is a much newer edition.
- Prefer the Maven artifact (4.29.3) for consistency with other Maven-resolved deps, and update the Bazel module to a version whose Java core matches 4.29.3 — or avoid using `@protobuf//java/core` directly and source everything through Maven.
- Audit all BUILD files for `@protobuf//java/core` usages and replace with the appropriate `@maven//` label.
