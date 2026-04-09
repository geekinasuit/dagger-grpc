---
id: T17
title: Publish Maven-compatible artifacts to Maven Central
priority: medium
phase: distribution
status: open
github_issue: 23
---

## Summary

Publish `dagger-grpc` library JARs to Maven Central so that Gradle, Maven, and other JVM build-tool users can depend on them without using Bazel. This makes the library accessible to the majority of the JVM ecosystem.

## Current State

No Maven publication infrastructure exists. The project builds with Bazel and produces JARs, but there is no mechanism to produce Maven-compatible artifacts (sources JAR, Javadoc JAR, POM) or publish them to Maven Central (via Sonatype OSSRH or the Central Portal).

The publishable targets are:
- `//api` — runtime annotations and `GrpcCallContext`
- `//io_grpc/compiler/ksp` — KSP symbol processor
- `//io_grpc/compiler/apt` — APT annotation processor
- `//ksp-apt-bridge` — KSP/APT bridge library (may be internal-only; decide at implementation time)
- `//util/armeria` — Armeria integration helper (optional; depends on Armeria runtime)

## Goals / Acceptance Criteria

- [ ] Maven coordinates decided and documented (e.g. `com.geekinasuit:dagger-grpc-api:0.1.0`)
- [ ] Bazel build produces per-target: binary JAR, sources JAR, Javadoc JAR, and `pom.xml`
- [ ] Artifacts are PGP-signed (required by Maven Central)
- [ ] CI can publish to Maven Central on tagged releases (GitHub Actions workflow)
- [ ] Published artifacts are resolvable via standard Maven Central coordinates in a Gradle or Maven project
- [ ] `README` or T15 integration guide documents the Maven coordinates

## Implementation Notes

### Artifact production from Bazel

`rules_jvm_external` does not produce Maven-compatible POM files for outbound publication. Options:

- **Option A: `rules_jvm_publish` or `publish_rules`** — Bazel rules designed to produce POM + JAR bundles for Maven Central. Check current maintenance status before adopting.
- **Option B: Gradle publication alongside Bazel** — Add a minimal Gradle wrapper (`build.gradle.kts`) that depends on the Bazel-built JARs and uses the Gradle Maven Publish plugin to generate POMs and publish. Simpler tooling but two build systems.
- **Option C: Manual POM authoring** — Write `pom.xml` templates by hand and use a Bazel `genrule` to produce the bundle. More maintenance burden but no new dependencies.

Recommendation: evaluate Option A first; fall back to Option B if no well-maintained Bazel solution exists.

### Maven Central registration

- Register a namespace at [central.sonatype.com](https://central.sonatype.com/) (the new Central Portal) for the chosen group ID.
- Group ID `com.geekinasuit` requires DNS or GitHub ownership verification for `geekinasuit.com` or the GitHub org.
- PGP key must be published to a keyserver (e.g. `keys.openpgp.org`).

### Coordinates proposal (to confirm)

| Target | Artifact ID |
|---|---|
| `//api` | `dagger-grpc-api` |
| `//io_grpc/compiler/ksp` | `dagger-grpc-ksp` |
| `//io_grpc/compiler/apt` | `dagger-grpc-apt` |
| `//util/armeria` | `dagger-grpc-armeria` |

Group ID: `com.geekinasuit` — Version: `0.1.0`

### Release coordination

Consider aligning the first Maven release with the BCR submission (T16) so both distribution channels debut at the same version.

## References

- `MODULE.bazel:7-10` — current version (`0.1`)
- `api/BUILD.bazel`, `io_grpc/compiler/ksp/BUILD.bazel`, `io_grpc/compiler/apt/BUILD.bazel`, `util/armeria/BUILD.bazel` — targets to publish
- T16 — BCR publication (coordinate release timing)
- T15 — user-facing integration guide (will document Maven coordinates once available)
