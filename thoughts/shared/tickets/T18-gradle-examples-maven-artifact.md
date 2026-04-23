---
id: T18
title: Gradle example projects and Maven artifact publication
priority: medium
phase: distribution
milestone: post-v0.2.0
status: open
blocked_by: T05, T06
blocks: ~
---

## Summary

Add Gradle-based example projects (Java/APT and Kotlin/KSP) that consume the library as a
Maven artifact, and establish the Bazel-to-Maven artifact publication pipeline.

The library itself remains Bazel-built. Maven artifacts are produced from Bazel (using a
Bazel publication rule or `rules_pkg`), published to a local temporary repository for
integration testing, and eventually published to Maven Central (coordinate with T17).

## Motivation

Not all users build with Bazel. Gradle is the dominant build tool for JVM projects. Providing
working Gradle examples with documented wiring is necessary for broad adoption.

## Scope

### Part A — Maven artifact from Bazel

Produce Maven-compatible JARs and POM files from Bazel for:
- `api/` — the runtime annotations and `GrpcCallContext`
- `io_grpc/compiler/ksp/` — the KSP annotation processor
- `io_grpc/compiler/apt/` — the APT annotation processor
- `util/armeria/` — the Armeria server helper (optional; may be separate artifact)

Use a Bazel rule (e.g., `rules_pkg` or a custom deploy JAR rule) to produce artifacts with
correct Maven coordinates, `META-INF/MANIFEST.MF`, and a POM. Publish to a local filesystem
Maven repository for integration testing.

Key requirements:
- APT processor JAR must include `META-INF/services/javax.annotation.processing.Processor`
  (already provided by `@AutoService`)
- KSP processor JAR must include `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`
  (already provided by KSP's service discovery mechanism)
- POM files must declare the correct runtime vs. compile-only dependency split

### Part B — Gradle example projects

Add two standalone Gradle projects (not composite builds; they consume the artifact):

`examples/io_grpc/gradle_build_java/` — Java, APT:
- Standard Gradle Java project
- `repositories { maven { url = uri("...local-repo...") } }`
- APT processor on `annotationProcessor` configuration
- `-Adaggergrpc.package=...` via `compileJava.options.compilerArgs`
- Mirrors the Bazel Java example in structure and handler code

`examples/io_grpc/gradle_build_kt/` — Kotlin, KSP:
- Standard Gradle Kotlin project with KSP plugin
- KSP processor on `ksp` configuration
- `daggergrpc.package` via `ksp { arg("daggergrpc.package", "...") }` in `build.gradle.kts`
- Mirrors the Bazel Kotlin example in structure and handler code

### Part C — Integration guide update

Update `docs/integration-guide.md` (from T15) to add Gradle sections alongside the existing
Bazel sections, referencing the new Gradle example projects.

### Part D — Maven Central (coordinate with T17)

Once the local artifact pipeline works, wire it into T17's Maven Central publication flow.
T17 remains the owner of credentials, release signing, and staging; T18 provides the artifact.

## Acceptance Criteria

- [ ] Bazel produces Maven-compatible JARs + POMs for api, compiler-ksp, compiler-apt
- [ ] Local Maven repo publication works (`bazel run //:publish-local` or equivalent)
- [ ] `examples/io_grpc/gradle_build_java/` builds and passes tests consuming from local repo
- [ ] `examples/io_grpc/gradle_build_kt/` builds and passes tests consuming from local repo
- [ ] Integration guide updated with Gradle sections
- [ ] T17 can use T18's artifact pipeline for Maven Central publication

## Implementation Notes

- The `daggergrpc.package` option name is already compatible with both APT (`-Adaggergrpc.package=...`)
  and KSP Gradle plugin (`ksp { arg("daggergrpc.package", "...") }`).
- `@AutoService` already handles the APT service file; no manual `META-INF/services` needed.
- The Gradle examples should be self-contained projects (not composite builds) to accurately
  reflect how a real user would consume the library.
- Local repo path can be a `file://` URI pointing to a build output directory, or a fixed
  temp path like `~/.m2/repository` (standard `mavenLocal()`).
