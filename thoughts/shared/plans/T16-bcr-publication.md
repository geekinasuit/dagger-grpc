# Plan: Publish dagger-grpc to the Bazel Central Registry (T16)

**Status:** draft — tentative; update as implementation proceeds  
**Ticket:** `thoughts/shared/tickets/T16-publish-to-bazel-central-registry.md`  
**Researched:** 2026-04-21

---

## 1. Overview

Publishing to the BCR requires two distinct tracks that can proceed partly in parallel:

1. **Release process** — a tagging convention, a release script, and criteria that allow an agent (or human) to cut a version safely
2. **BCR wiring** — template files in this repo + a `publish-to-bcr` GitHub Actions workflow that opens a BCR PR automatically on each release

The one-time **onboarding** steps (fork BCR, register as maintainer, get first PR approved by BCR maintainers) gate the very first publication but do not block building the release infrastructure.

---

## 2. Research Summary

### BCR Entry Structure

Each module version in the BCR lives at `modules/<name>/<version>/` and requires exactly four files:

```
modules/dagger-grpc/
  metadata.json                   ← module-level (one per module, not per version)
  0.1.0/
    MODULE.bazel                  ← verbatim copy of the module's MODULE.bazel at that tag
    source.json                   ← download URL + SRI integrity hash
    presubmit.yml                 ← what BCR CI should build/test
    patches/                      ← optional; .patch files applied to the archive
```

**metadata.json** (module-level, not version-level):
```json
{
  "homepage": "https://github.com/geekinasuit/dagger-grpc",
  "maintainers": [
    {
      "github": "<github-username>",
      "github_user_id": <numeric-id>,
      "name": "<Full Name>",
      "email": "<email>"
    }
  ],
  "repository": ["github:geekinasuit/dagger-grpc"],
  "versions": [],
  "yanked_versions": {}
}
```
`versions` and `yanked_versions` are auto-managed; leave empty in the template.

**source.json**:
```json
{
  "integrity": "sha256-<base64-encoded-sha256-of-archive>",
  "url": "https://github.com/geekinasuit/dagger-grpc/releases/download/v0.1.0/dagger-grpc-v0.1.0.tar.gz",
  "strip_prefix": "dagger-grpc-0.1.0"
}
```
- `integrity` uses SRI format. Computed: `sha256sum archive.tar.gz | xxd -r -p | base64`
- URL must point to a **stable release asset** — GitHub auto-generated archive tarballs at `/archive/refs/tags/` are acceptable but named release assets are preferred for provenance/attestation support
- `strip_prefix` is the top-level directory name inside the archive

**presubmit.yml** (proposed for dagger-grpc):
```yaml
matrix:
  platform: ["ubuntu2004"]
  bazel: ["7.x", "8.x"]
tasks:
  build_library:
    name: "Build dagger-grpc library"
    platform: ${{ platform }}
    bazel: ${{ bazel }}
    build_targets:
      - "//..."
  test_java_example:
    name: "Java example (APT consumer)"
    platform: ${{ platform }}
    bazel: ${{ bazel }}
    bcr_test_module:
      module_path: "examples/io_grpc/bazel_build_java"
      build_targets:
        - "//..."
      test_targets:
        - "//..."
  test_kt_example:
    name: "Kotlin example (KSP consumer)"
    platform: ${{ platform }}
    bazel: ${{ bazel }}
    bcr_test_module:
      module_path: "examples/io_grpc/bazel_build_kt"
      build_targets:
        - "//..."
      test_targets:
        - "//..."
```

Notes:
- BCR CI runs on Buildkite (Linux/macOS Ubuntu). macOS can be added but costs more time.
- Bazel 6.x support is probably not worth maintaining — dagger-grpc uses bzlmod features that require 7+.
- The `bcr_test_module` tasks use the existing example workspaces. Before BCR submission the examples must be updated to use `bazel_dep` instead of `local_path_override` (so BCR CI resolves from the registry, not local). A patch can handle this — see §4.

### publish-to-bcr (Automation Tool)

`bazel-contrib/publish-to-bcr` is a **GitHub Actions reusable workflow** that:
1. Triggers on a repository release event (or workflow_dispatch for manual retry)
2. Reads `.bcr/` template files from the ruleset repo
3. Computes the archive integrity hash
4. Opens a draft PR against `bazelbuild/bazel-central-registry` (or a fork) with the correctly-formed BCR entry

**Important:** The legacy GitHub App that did this is being discontinued **June 30, 2026**. The GH Actions workflow is the current supported path.

Required setup:
- A fork of `bazelbuild/bazel-central-registry` under the `geekinasuit` org (for the workflow to push to)
- A "Classic" GitHub PAT with `workflow` + `repo` scopes, stored as `BCR_PUBLISH_TOKEN` in repo secrets
- `.bcr/` directory in this repo with template files (see §3)
- A `.github/workflows/publish.yaml` that calls the reusable workflow on release

### Onboarding Requirements (One-Time)

1. **Fork the BCR**: create `geekinasuit/bazel-central-registry` as a fork of `bazelbuild/bazel-central-registry`
2. **CLA**: the person opening the first BCR PR must have signed the Google CLA (required by BCR)
3. **First-submission review**: new modules require BCR maintainer approval to unblock their CI. This is a one-time manual review — after that, the module maintainers can approve their own version additions
4. **PAT creation**: create a Classic PAT (not fine-grained — fine-grained can't open PRs on public repos) with `workflow` + `repo` scopes, store as `BCR_PUBLISH_TOKEN`

---

## 3. Files to Add to This Repo

### `.bcr/metadata.template.json`
```json
{
  "homepage": "https://github.com/geekinasuit/dagger-grpc",
  "maintainers": [
    {
      "github": "FILL_IN",
      "github_user_id": 0,
      "name": "FILL_IN",
      "email": "FILL_IN"
    }
  ],
  "repository": ["github:geekinasuit/dagger-grpc"],
  "versions": [],
  "yanked_versions": {}
}
```

### `.bcr/source.template.json`
```json
{
  "integrity": "",
  "strip_prefix": "dagger-grpc-{VERSION}",
  "url": "https://github.com/{OWNER}/{REPO}/releases/download/{TAG}/{REPO}-{TAG}.tar.gz"
}
```
`{OWNER}`, `{REPO}`, `{TAG}`, `{VERSION}` are substituted by publish-to-bcr at release time.

### `.bcr/presubmit.yml`
The presubmit.yml shown in §2 above. It should live at `.bcr/presubmit.yml` (publish-to-bcr copies it into the BCR entry).

### `.bcr/patches/` (optional, likely needed)
The examples currently use `local_path_override` to reference the root module. For BCR testing the examples must use `bazel_dep`. A patch file that replaces the `local_path_override` block in each example's MODULE.bazel with `bazel_dep(name="dagger-grpc", version="{VERSION}")` can be applied by BCR CI. Alternatively, the examples can be updated to conditionally use one or the other — or maintained as two copies.

**Recommendation:** Keep `local_path_override` in the committed example files (for local development) and supply a `.bcr/patches/` patch that switches to `bazel_dep`. This way examples work locally without changes and BCR CI sees the registry-resolved version.

### `.github/workflows/publish.yaml`
```yaml
name: Publish to BCR

on:
  release:
    types: [published]
  workflow_dispatch:
    inputs:
      tag_name:
        description: "Release tag to publish"
        required: true

jobs:
  publish:
    uses: bazel-contrib/publish-to-bcr/.github/workflows/publish.yaml@v0.2.2
    with:
      tag_name: ${{ github.event.release.tag_name || inputs.tag_name }}
      registry_fork: geekinasuit/bazel-central-registry
      attest: false        # set true once release_ruleset workflow is in place
      draft: true          # opens BCR PR as draft; maintainer clicks "Ready for review"
    secrets:
      BCR_PUBLISH_TOKEN: ${{ secrets.BCR_PUBLISH_TOKEN }}
```

---

## 4. Release Process Design

### Version Convention

- **Format:** `v<major>.<minor>.<patch>` (semver, v-prefix) — e.g. `v0.1.0`
- **MODULE.bazel version field:** strip the `v` — e.g. `version = "0.1.0"`
- These must match: the tag, the MODULE.bazel version, and the `strip_prefix` in source.template.json
- Current MODULE.bazel says `version = "0.1"` — before first release, bump to `version = "0.1.0"` for full semver compatibility

### Release Criteria (gates that must pass before cutting a release)

All of the following must be true:

1. **CI green on main** — all jobs in `ci.yaml` pass on the HEAD commit being tagged
2. **MODULE.bazel version matches intended tag** — `version = "X.Y.Z"` in MODULE.bazel matches the `vX.Y.Z` tag being cut
3. **MODULE.bazel.lock is current** — lock file matches MODULE.bazel (not drifted)
4. **No open HIGH-priority tickets blocking release** — check `thoughts/shared/tickets/overview.md`
5. **Both example workspaces build and test cleanly** — `bin/bazel build //...` + `bin/bazel test //...` from each example directory

### Release Script (`scripts/release.sh`)

A shell script (not a Bazel target) that an agent or human can run to perform a release. It should:

1. Verify the criteria above (exit non-zero if any fail)
2. Update `MODULE.bazel` version field to the target version
3. Regenerate `MODULE.bazel.lock` (run `bin/bazel mod deps` or equivalent)
4. Commit the version bump (`jj commit` / `git commit`)
5. Create the tag (`jj tag` or `git tag -a vX.Y.Z`)
6. Push the tag (`jj git push --tags` or `git push --tags`)
7. Create the GitHub release via `gh release create vX.Y.Z --generate-notes`

The publish-to-bcr workflow fires automatically on the `release.published` event.

### Agent Release Criteria

For an agent to perform a release autonomously (without human confirmation on each step), the following must be explicitly granted by the user in advance:

- Permission to push tags to the remote
- Permission to create a GitHub release
- A specific version number to cut (agent does not choose versions unilaterally)

The agent must still verify all release criteria and stop with a report if any fail.

---

## 5. Implementation Sequence

```
Phase 1 — Repo wiring (no external dependencies)
  1a. Bump MODULE.bazel version to "0.1.0"
  1b. Add .bcr/ template files (metadata.template.json, source.template.json, presubmit.yml)
  1c. Write scripts/release.sh
  1d. Add .github/workflows/publish.yaml (the BCR publish workflow)

Phase 2 — Onboarding (requires human action)
  2a. Fork bazelbuild/bazel-central-registry → geekinasuit/bazel-central-registry
  2b. Sign Google CLA (person who will open first BCR PR)
  2c. Create Classic PAT with workflow+repo scopes
  2d. Add BCR_PUBLISH_TOKEN to repo secrets

Phase 3 — First release + BCR submission
  3a. Run release script for v0.1.0 (or use release workflow)
  3b. Monitor publish-to-bcr: it opens a draft PR on geekinasuit/bazel-central-registry fork
  3c. Review the generated BCR entry, click "Ready for Review" (submits to bazelbuild/bazel-central-registry)
  3d. BCR maintainers review first submission, approve CI, merge
  3e. Verify: bazel_dep(name="dagger-grpc", version="0.1.0") resolves in a fresh project

Phase 4 — Example updates (can parallel Phase 2/3)
  4a. Author the patch(es) for example MODULE.bazel files (local_path_override → bazel_dep)
  4b. Add patches to .bcr/patches/
  4c. Update presubmit.yml if needed after patch testing
```

---

## 6. Open Questions

- **Who is the BCR maintainer?** The `metadata.template.json` needs a GitHub username and numeric user ID. This is the identity that BCR will notify on new version PRs and who can approve them.
- **attest: true or false?** Attestation (SLSA provenance) requires using `bazel-contrib/release_ruleset` as the release workflow. Is that desirable for v0.1.0, or add later?
- **Bazel 6.x support in presubmit.yml?** dagger-grpc uses bzlmod and Kotlin rules that require Bazel 7+. Recommend dropping Bazel 6.x from the matrix.
- **macOS in BCR CI matrix?** BCR CI runs on macOS are slower and costlier. Recommend Ubuntu-only for the initial submission, add macOS later.
- **Release tarball vs auto-generated archive?** The publish-to-bcr tool defaults to using the GitHub auto-generated tarball (`/archive/refs/tags/`). This is acceptable. Named release assets are only needed if attestation is enabled.
- **MODULE.bazel.lock drift**: currently drifted intentionally (research phase). Must be resolved before cutting a release.

---

## 7. Success Criteria

- [ ] `bin/bazel test //...` passes from root and both examples at v0.1.0 tag
- [ ] `publish.yaml` workflow fires on release and opens a draft BCR PR automatically
- [ ] BCR PR passes all presubmit checks
- [ ] `bazel_dep(name = "dagger-grpc", version = "0.1.0")` resolves in a fresh external project
- [ ] Example workspaces are updated to use `bazel_dep` and verified to work

---

## References

- [BCR contribution guide](https://github.com/bazelbuild/bazel-central-registry/blob/main/docs/README.md)
- [BCR policies](https://github.com/bazelbuild/bazel-central-registry/blob/main/docs/bcr-policies.md)
- [publish-to-bcr](https://github.com/bazel-contrib/publish-to-bcr)
- `MODULE.bazel:7-10` — current version declaration
- `examples/io_grpc/bazel_build_java/MODULE.bazel` — local_path_override to patch
- `examples/io_grpc/bazel_build_kt/MODULE.bazel` — local_path_override to patch
- `.github/workflows/ci.yaml` — existing CI to confirm passes before release
