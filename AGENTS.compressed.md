<!--COMPRESSED v1; source:AGENTS.md-->
§META
layer:repo scope:dagger-grpc-repo

§ABBREV
ts=thoughts/shared
opt=/opt/geekinasuit/agents int=$opt/internal pub=$opt/public

§PURPOSE
library+code-generation toolkit: wires gRPC service impls into Dagger 2 DI graph with per-call scoping
each gRPC request → own Dagger subcomponent (call scope) → handler classes instantiated fresh per call (full DI)
primary integration: Armeria as gRPC server runtime
targets: JVM backends (Java+Kotlin); Bazel 8 + Bzlmod; version 0.1.0

§DOCS
$ts/ docs have two forms: <name>.md(human) + <name>.compressed.md(token-efficient,lossless)
compressed uses §SECTION markers + §ABBREV table at top
prefer .compressed.md for active context; .md only when producing human-readable output or compressed absent

§NOINLINE [NON-OPTIONAL — mechanical harness enforcement; violations block every call requiring hand-permission]
FORBIDDEN in any Bash tool call:
  heredocs: cmd << 'EOF'...EOF
  inline multi-line strings: -m "line1\nline2" or -F body="text with `backticks`"
  command substitution: cmd $(other_cmd)
  any string containing backticks | --- | asterisks passed as a shell argument
REQUIRED pattern — no exceptions:
  write body/prompt/message → /tmp/<repo>-<branch>-<purpose>.md via Write tool
  reference by path: --body-file /tmp/... | -F body=@/tmp/... | Agent prompt:"Read instructions from /tmp/..."
unique tmp names: include repo+purpose+context (e.g. /tmp/dagger-grpc-pr-body-32.md); never generic names
THIS RULE APPLIES TO SUBAGENTS TOO: embed it verbatim at the top of every prompt written to /tmp before spawning

§LAYOUT
api/                              Core public API: annotations + GrpcCallContext runtime
io_grpc/compiler/common/          Shared HandlerMetadata model + Validator (KSP-typed)
io_grpc/compiler/ksp/             KSP processor for Kotlin projects (generates *Adapter.kt)
io_grpc/compiler/apt/             APT processor for Java projects (generates *Adapter.java)
io_grpc/lib/                      Placeholder stub (purpose TBD, see T13)
ksp-apt-bridge/                   KSP interface impls backed by javax.lang.model (APT bridge)
util/armeria/                     Thin Armeria GrpcService wrapping helper
third_party/dagger/               Bazel wrapper: dagger runtime + dagger-compiler java_plugin
third_party/processors/           Bazel wrapper: auto-service java_plugin
examples/io_grpc/bazel_build_java/  Standalone Java example (APT); independent MODULE.bazel
examples/io_grpc/bazel_build_kt/    Standalone Kotlin example (KSP); independent MODULE.bazel
bin/                              Hermit hermetic toolchain env
$ts/research/   read before implementing; prefer .compressed.md
$ts/tickets/    overview.md index + ticket files; check before starting new work
$ts/plans/      implementation plans; read before implementing non-trivial features
$ts/handoffs/   session handoff documents

before non-trivial impl: check $ts/tickets/overview.md + $ts/research/ + $ts/plans/ for prior work

§TICKETS
overview.md=canonical index; do NOT maintain ticket inventories elsewhere
any ticket file create|modify|resolve|delete → update overview.md in same op

GitHub Issues — thin layer for PR linkage only:
  create when about to open PR (not before)
  keep thin: title + one-line summary + "**Ticket:** \`$ts/tickets/<filename>.md\`"
  PR ref: "fixes #N" or "closes #N" in PR description
  when opening PR: update ticket file status=resolved + github_issue:N + move to Resolved in overview.md (same branch)

ticket file conventions:
  filename: <kebab>.md in $ts/tickets/
  frontmatter: date status(open|resolved) priority(low|medium|high) area; optional: github_issue:N
  required sections: Summary, Current State|Resolution, Goals|acceptance criteria, References(file:line)

§API
@GrpcServiceHandler(grpcWrapperType:KClass<*>) — @Target(CLASS); marks handler; triggers both processors
@GrpcCallScope — JSR-330 @Scope; marks objects scoped to single gRPC call
@ApplicationScope — JSR-330 @Scope; marks objects scoped to application lifetime
GrpcCallContext — @GrpcCallScope injectable; exposes Metadata, ServerCall authority, attributes

§CODEGEN
two parallel processors share common model via ksp-apt-bridge:
  io_grpc/compiler/common/(HandlerMetadata+Validator, KSP-typed)
      ↑                         ↑
  io_grpc/compiler/ksp/    io_grpc/compiler/apt/
  (KSP types native)       (javax.lang.model via ksp-apt-bridge)
      ↓                         ↓
  KotlinPoet output         JavaPoet output

generated: *Adapter classes (one per @GrpcServiceHandler; bridges gRPC dispatch to Dagger call-scope subcomponent)
NOT YET generated (hand-written, marked @Generated("to be generated")):
  GrpcCallScopeGraph — @Subcomponent with one provision method per handler
  GrpcHandlersModule — @Module providing each *Adapter @IntoSet
  GrpcCallScopeGraphModule — @Module(includes=[GrpcCallContext.Module::class])
generating these = primary pending feature work (T04–T07)

§BUILD
Bazel invocation:
  ALWAYS use bin/bazel (activates Hermit; sets JAVA_HOME etc.)
  NEVER source activate-hermit or set JAVA_HOME manually

Bazel conventions:
  bzlmod only (MODULE.bazel); no WORKSPACE
  --enable_workspace=true = compatibility shim for grpc_kotlin only
  all Kotlin executables: java_binary(runtime_deps=...) not kt_jvm_binary
  NEVER commit generated proto code → .gitignore
  Kotlin targets with internal visibility: associates=[...]; do not change visibility to work around
  third_party/ wraps Maven annotation processors into reusable java_plugin targets → reference from plugins=[...]
  package-pinning BUILD.bazel files: intentional; do not add targets without reason
  MODULE.bazel changes: ALWAYS include MODULE.bazel.lock in same commit

examples = independent workspaces:
  each has own MODULE.bazel with local_path_override to root
  when changing library: build each example independently to verify consumer compatibility

§TESTING
prefer unit tests first; codegen processors testable via in-process compilation (kotlin-compile-testing, compile-testing)
prefer fakes|stubs over mock frameworks; mocks couple to impl; fakes test behavior

current coverage state (narrow):
  APT adapter generation: real assertions
  KSP processor test: disabled (T08 — kotlin-compile-testing + Kotlin 2 compatibility)
  common/ + ksp-apt-bridge + example servers: placeholder tests only

§STYLE
KSP processor: KSP types natively; do NOT introduce APT/javax.lang.model types
APT processor: use ksp-apt-bridge adapters; do NOT duplicate common model
Java output (APT): Callable<AsyncService> with checked-exception wrapping
Kotlin output (KSP): plain () -> AsyncService lambda
minimal deps: std lib or well-established ecosystem; new Maven deps → update MODULE.bazel + commit MODULE.bazel.lock

§VCS
repo uses jj (Jujutsu); .jj/ present
if jj available: use for all commit|branch|log ops; NEVER run git when .jj/ present
if jj not available: fall back to git (jj maintains compatible git backend)
gh CLI for all GitHub API interactions regardless of VCS tool

jj equivalents: git log→jj log; git diff→jj diff; git status→jj status; git commit→jj commit; git push→jj git push

branch+PR required:
  NEVER push directly to main
  jj: jj bookmark create <name> → jj git push → gh pr create
  all PRs: human review+merge required; agent must NOT merge own PRs without explicit one-time grant
  NEVER --admin or CI bypass; if checks failing: stop+report

§WORKFLOW
non-trivial features: three-agent test-driven workflow (design+test → implementation → review)
  design+test agent: public API + complete test suite; not-yet-implemented tests compile but @Ignore + TODO comment
  implementation agent: works from design agent's API+tests; consults design agent for ambiguity;
    may create unit tests for internal impl details; removes @Ignore when impl complete
  review agent: monitors PR comments; addresses or explicitly rejects in-thread;
    large-scope suggestions → file ticket; escalates API changes to design agent

inter-agent norms: respect ownership boundaries(no unilateral cross-domain changes); escalate disagreements after 3 rounds→orchestrator→human; break self-loops immediately→report what was attempted+what blocked+decision needed

commit discipline:
  every commit: build must pass; include minimum scaffolding to restore green if needed
  do NOT mix design/test + implementation in same commit
  do NOT mix unrelated changes in same commit

PR readiness:
  build passes (all targets, all examples)
  all PR description checkboxes checked
  all review comments resolved or explicitly rejected in-thread
  ticket file + overview.md updated to resolved status in same branch

§REFS
| KSP processor entry      | io_grpc/compiler/ksp/src/main/kotlin/…/DaggerGrpcSymbolProcessor.kt |
| APT processor entry      | io_grpc/compiler/apt/src/main/kotlin/…/DaggerGrpcAPTProcessor.kt |
| Shared metadata model    | io_grpc/compiler/common/src/main/kotlin/…/HandlerMetadata.kt |
| KSP-APT bridge           | ksp-apt-bridge/src/main/kotlin/…/APTClassDeclaration.kt |
| Runtime call context     | api/src/main/kotlin/…/GrpcCallContext.kt |
| Armeria server wrapper   | util/armeria/src/main/kotlin/… |
| Java example (APT)       | examples/io_grpc/bazel_build_java/ |
| Kotlin example (KSP)     | examples/io_grpc/bazel_build_kt/ |
| APT processor Bazel wiring | io_grpc/compiler/apt/BUILD.bazel |
| KSP processor Bazel wiring | io_grpc/compiler/ksp/BUILD.bazel |
| third_party plugin wrapping | third_party/dagger/BUILD.bazel, third_party/processors/BUILD.bazel |

§CHAIN Bootstrap — execute exactly one branch (stop after match):
when {
  $int/AGENTS.compressed.md exists → read+follow; exit chain
  $int/AGENTS.md exists → read+follow; exit chain
  $pub/AGENTS.compressed.md exists → read+follow; exit chain
  $pub/AGENTS.md exists → read+follow; exit chain
  ~/.geekinasuit/agents/public/AGENTS.compressed.md exists → read+follow; exit chain
  ~/.geekinasuit/agents/public/AGENTS.md exists → read+follow; exit chain
  else → skip; bootstrap complete
}
Do not read multiple branches. Stop immediately after first match.
If none exist: skip — not required for contributor work; will not be present on most machines.
