---
id: T16
title: Publish to Bazel Central Registry
priority: medium
phase: distribution
status: open
---

## Summary

Publish `dagger-grpc` to the [Bazel Central Registry (BCR)](https://registry.bazel.build/) so that Bazel projects can depend on it via a standard bzlmod `bazel_dep` declaration without needing a `local_path_override` or manual `archive_override`.

## Current State

The module is fully bzlmod-compatible (`MODULE.bazel` present, no `WORKSPACE` required). It is not yet registered in the BCR. Consumers must currently use `local_path_override` (as the examples do) or `archive_override` pointing at a specific GitHub archive tarball.

## Goals / Acceptance Criteria

- [ ] A source archive (tarball) is produced for the release and its SHA-256 is recorded
- [ ] A BCR entry is submitted to [bazelbuild/bazel-central-registry](https://github.com/bazelbuild/bazel-central-registry) as a pull request containing:
  - `modules/dagger-grpc/<version>/MODULE.bazel`
  - `modules/dagger-grpc/<version>/source.json` (URL + integrity hash pointing to the release archive)
  - `modules/dagger-grpc/metadata.json` (homepage, maintainers, yanked_versions)
- [ ] The BCR PR passes all automated compatibility checks
- [ ] After acceptance, `bazel_dep(name = "dagger-grpc", version = "0.1")` resolves correctly in a fresh consumer project
- [ ] The two example workspaces are updated to use `bazel_dep` (removing `local_path_override`) as a smoke-test

## Implementation Notes

- BCR submission process: fork `bazelbuild/bazel-central-registry`, add the module entry, open a PR. BCR bots run compatibility checks automatically.
- The release archive URL must be stable (GitHub release asset or equivalent) — a branch tarball URL is not suitable.
- `source.json` integrity field uses SRI format: `sha256-<base64>`.
- The module name in `MODULE.bazel` is already `dagger-grpc`, matching the intended BCR name.
- Decide on a release tagging convention (e.g. `v0.1.0`) before submitting; the BCR version string must match the `version` field in `MODULE.bazel`.
- Prerequisite: a GitHub release (tagged archive) should exist before or alongside the BCR submission. Consider whether T17 (Maven release) should be coordinated with the same release event.

## References

- `MODULE.bazel:7-10` — module name and version declaration
- `examples/io_grpc/bazel_build_java/MODULE.bazel` — current `local_path_override` pattern to replace
- `examples/io_grpc/bazel_build_kt/MODULE.bazel` — same
- https://github.com/bazelbuild/bazel-central-registry — submission target
