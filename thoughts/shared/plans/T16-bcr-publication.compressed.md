<!--COMPRESSED v1; source:T16-bcr-publication.md-->
§META
date:2026-04-21 author:agent ticket:T16 status:draft

§ABBREV
BCR=Bazel-Central-Registry bcr=bazelbuild/bazel-central-registry
p2b=bazel-contrib/publish-to-bcr SRI=subresource-integrity
lpo=local_path_override

§GOAL
Publish dagger-grpc to BCR so consumers use bazel_dep(name="dagger-grpc",version="X.Y.Z") without lpo or archive_override.

§CONTEXT
Module is bzlmod-ready (MODULE.bazel present). No prior releases, no tags, no BCR entry, no publish workflow. Version bumped 0.1→0.1.0 in this work. Examples use lpo for local dev; BCR CI needs lpo removed (handled via patch).

§APPROACH
Two tracks:
1. Repo wiring — .bcr/ templates + publish.yaml + scripts/release.sh (code changes, PR)
2. Onboarding — one-time human steps (fork BCR, CLA, PAT, secret) — cannot be automated

Automation: p2b reusable workflow (v1.2.0) fires on release.published, reads .bcr/ templates, computes SRI hash, opens draft PR on BCR fork. Legacy GitHub App discontinued 2026-06-30; use GH Actions only.

§BCR_ENTRY_FILES
Per version: MODULE.bazel(verbatim) source.json(url+SRI) presubmit.yml patches/(optional)
Module-level: metadata.json(maintainers,repository allowlist,versions[])

§SOURCE_JSON_SCHEMA
url: GitHub release asset URL
integrity: sha256-<base64> (SRI)
strip_prefix: <repo>-<version>
patches: {filename: sha256-<base64>}  [if patches present]
patch_strip: 1  [default]

§PRESUBMIT
matrix: platform[ubuntu2004] × bazel[7.x,8.x]
tasks: build_library(//...) + test_java_example(bcr_test_module:examples/bazel_build_java) + test_kt_example(bcr_test_module:examples/bazel_build_kt)
bcr_test_module uses examples after patch removes lpo

§PATCH_STRATEGY
.bcr/patches/examples_use_registry.patch removes lpo blocks from both example MODULE.bazel files.
Examples committed with bazel_dep version matching root version → patch only removes lpo, no version rewrite.
Update patch + example bazel_dep versions together on each release bump.

§STEPS
Phase1(repo wiring — this PR):
  1a. MODULE.bazel version 0.1→0.1.0
  1b. examples bazel_dep version 0.1→0.1.0
  1c. .bcr/metadata.template.json (cgruber/331234)
  1d. .bcr/source.template.json ({OWNER}/{REPO}/{TAG}/{VERSION} placeholders)
  1e. .bcr/presubmit.yml (ubuntu×bazel7/8, build+test+examples)
  1f. .bcr/patches/examples_use_registry.patch (remove lpo)
  1g. .github/workflows/publish.yaml (p2b v1.2.0, draft:true, attest:false)
  1h. scripts/release.sh (criteria check → gh release create)
  1i. plans/T16-bcr-publication.md + .compressed.md
  1j. T16+T17 github_issue fields patched (22,23)
  1k. MODULE.bazel.lock regenerated

Phase2(onboarding — human):
  2a. Fork bazelbuild/bazel-central-registry → geekinasuit/bazel-central-registry
  2b. Sign Google CLA
  2c. Create Classic PAT (workflow+repo scopes); store as BCR_PUBLISH_TOKEN
  [note: Fine-grained PATs cannot open PRs on public repos — must use Classic]

Phase3(first release):
  3a. scripts/release.sh 0.1.0 (or --dry-run first)
  3b. Monitor publish.yaml: opens draft PR on geekinasuit/bazel-central-registry
  3c. Review BCR entry; click Ready for Review → submits to bazelbuild/bazel-central-registry
  3d. BCR maintainer approves first submission CI
  3e. Verify: fresh project resolves bazel_dep(name="dagger-grpc",version="0.1.0")

Phase4(example updates — parallel to 2/3):
  4a. Test patch locally against the release archive
  4b. Verify BCR CI passes with patched examples

§RELEASE_CRITERIA (scripts/release.sh enforces):
MODULE.bazel version matches tag | clean working copy | HEAD on main | bin/bazel build //... | bin/bazel test //... | both examples build+test

§AGENT_RELEASE_RULES
May cut release only when: user explicitly provides version; user grants tag+release-create permission; all criteria pass.

§OPEN_QUESTIONS
attest:false for v0.1.0; add later via bazel-contrib/release_ruleset
BCR_PUBLISH_TOKEN: Classic PAT needed; Fine-grained won't work
First submission: BCR maintainer manual approval required (one-time)
source.template.json URL assumes named release asset; p2b defaults to auto-generated archive tarball if no asset matches — verify at first release

§DONE
[ ] publish.yaml fires on release and opens draft BCR PR
[ ] BCR presubmit CI passes (all tasks green)
[ ] bazel_dep(name="dagger-grpc",version="0.1.0") resolves in fresh project
[ ] examples build+test after lpo patch applied
