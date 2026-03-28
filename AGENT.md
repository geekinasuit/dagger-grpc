# Agent Guide: dagger-grpc

This file is for AI agents and future context windows. It captures working norms, design principles, and conventions for this repository that are not obvious from reading the code alone.

---

## Project Purpose

`dagger-grpc` is a **library and code-generation toolkit** that wires gRPC service implementations into a [Dagger 2](https://dagger.dev/) dependency-injection graph with **per-call scoping**. Each incoming gRPC request gets its own Dagger subcomponent ("call scope"), allowing service handler classes to be instantiated fresh per call (with full DI) rather than being singletons. The primary integration target is [Armeria](https://armeria.dev/) as the gRPC server runtime.

The project targets JVM backends (Java and Kotlin), uses Bazel 8 with Bzlmod, and is at version `0.1`.

---

## Reading This Repo: Compressed Documents

The `thoughts/` directory contains research, plans, tickets, and handoffs. Many documents have two forms:

- `<name>.md` — human-readable, fully prose
- `<name>.compressed.md` — dense structured notation, token-efficient, losslessly equivalent

**Prefer the `.compressed.md` form for active context use.** Use the `.md` form only when producing output for human review or when the compressed form is absent.

The compressed format uses `§SECTION` markers and a `§ABBREV` table at the top for decoding all shorthands. It is self-describing.

---

## Repository Layout (Key Directories)

```
api/                            Core public API: annotations + GrpcCallContext runtime
io_grpc/
  compiler/
    common/                     Shared HandlerMetadata model + Validator (KSP-typed)
    ksp/                        KSP processor for Kotlin projects (generates *Adapter.kt)
    apt/                        APT processor for Java projects (generates *Adapter.java)
  lib/                          Placeholder — stub only, purpose not yet defined (see T13)
ksp-apt-bridge/                 KSP interface impls backed by javax.lang.model (APT bridge)
util/armeria/                   Thin Armeria GrpcService wrapping helper
third_party/
  dagger/                       Bazel wrapper: dagger runtime + dagger-compiler java_plugin
  processors/                   Bazel wrapper: auto-service java_plugin
examples/
  io_grpc/bazel_build_java/     Standalone Java example (APT processor); independent MODULE.bazel
  io_grpc/bazel_build_kt/       Standalone Kotlin example (KSP processor); independent MODULE.bazel
bin/                            Hermit hermetic toolchain env (hermit.hcl, activate-hermit)
thoughts/shared/research/       Research documents (read before implementing)
thoughts/shared/tickets/        Ticket files + overview.md index (check before starting new work)
thoughts/shared/plans/          Implementation plans
thoughts/shared/handoffs/       Session handoff documents
```

Each example is an **independent Bazel workspace** with its own `MODULE.bazel` referencing the root via `local_path_override`. Examples function as real consumer projects and are tested by CI in isolation.

Before starting any non-trivial implementation work: check `thoughts/shared/tickets/overview.md` for a summary of open and resolved work, and `thoughts/shared/research/` for relevant prior research.

---

## Working with Tickets

### overview.md is the Canonical Index

`thoughts/shared/tickets/overview.md` is the single source of truth for all open and resolved work. Individual files in that directory contain full detail; `overview.md` contains the summary. **Do not maintain ticket inventories anywhere else** — other files (AGENT.md, research docs, plans) should reference `overview.md` rather than listing tickets inline.

### Keeping overview.md Current

**Any time you create, modify, resolve, or delete a ticket file, you must also update `overview.md`** in the same operation. Specifically:

- **New ticket**: add a row to the Open table with file link, priority, area, and one-line summary
- **Ticket resolved**: move it from the Open table to the Resolved section
- **Ticket updated** (priority, area, summary changed): update the corresponding row in `overview.md`
- **Ticket deleted without resolution** (e.g. duplicate or invalid): remove its row from `overview.md` entirely

Never leave `overview.md` out of sync with the actual ticket files.

### GitHub Issues: Thin Layer for PR Linkage

Ticket files are the source of truth. GitHub Issues serve one specific purpose: enabling `fixes #N` PR cross-referencing in GitHub's UI.

**Rules:**
- **Do not pre-create GitHub issues for every ticket file.** Only create a GitHub issue when you are about to open a PR for that work.
- **Keep GitHub issues thin** — title, one-line summary, and this exact line in the body so automation can locate the ticket file:
  ```
  **Ticket:** `thoughts/shared/tickets/<filename>.md`
  ```
- **When opening a PR**, include `fixes #N` (or `closes #N`) in the PR description to link the issue.
- **When opening a PR, also update the ticket file and `overview.md` in the same branch.** Set the ticket file's `status` to `resolved`, add a `github_issue: N` frontmatter field, and move its row to the Resolved section of `overview.md`.

### Ticket File Conventions

- Filename: `<kebab-case-description>.md` in `thoughts/shared/tickets/`
- Frontmatter fields: `date`, `status` (open/resolved), `priority` (low/medium/high), `area` (comma-separated)
- Optional frontmatter: `github_issue: <N>` once a GitHub issue has been created
- Required sections: Summary, Current State (or Resolution if resolved), Goals or acceptance criteria, References (file paths with line numbers)

---

## Architecture Overview

### Annotations and Runtime API (`api/`)

| Symbol | Kind | Purpose |
|---|---|---|
| `@GrpcServiceHandler(grpcWrapperType: KClass<*>)` | `@Target(CLASS)` annotation | Marks a class as a gRPC call handler; triggers both processors |
| `@GrpcCallScope` | JSR-330 `@Scope` | Marks objects scoped to a single gRPC call |
| `@ApplicationScope` | JSR-330 `@Scope` | Marks objects scoped to the application lifetime |
| `GrpcCallContext` | `@GrpcCallScope`-scoped injectable | Exposes the current call's Metadata, ServerCall authority, attributes, etc. |

### Codegen Architecture

Two parallel processors share a common model via the `ksp-apt-bridge` library:

```
io_grpc/compiler/common/   (HandlerMetadata + Validator — KSP-typed)
        ↑                           ↑
io_grpc/compiler/ksp/        io_grpc/compiler/apt/
(KSP types natively)         (javax.lang.model via ksp-apt-bridge)
        ↓                           ↓
   KotlinPoet output           JavaPoet output
   Kotlin projects             pure Java projects
```

**Currently generated:** `*Adapter` classes (one per `@GrpcServiceHandler` class) that bridge gRPC dispatch to Dagger call-scope subcomponent provisioning.

**Not yet generated (hand-written, marked `@Generated("to be generated")`):**
- `GrpcCallScopeGraph` — the `@Subcomponent` interface with one provision method per handler
- `GrpcHandlersModule` — the `@Module` providing each `*Adapter` `@IntoSet`
- `GrpcCallScopeGraphModule` — the `@Module(includes = [GrpcCallContext.Module::class])`

Generating these is the primary pending feature work (tickets T04–T07).

---

## Build System Principles

### Bazel Conventions

- **bzlmod only** (`MODULE.bazel`). Do not add a `WORKSPACE` file. The existing `--enable_workspace=true` flag in `.bazelrc` is a compatibility shim for `grpc_kotlin` only.
- All Kotlin executables use `java_binary(runtime_deps=...)` — not `kt_jvm_binary`. This is the `rules_kotlin` convention.
- **Never commit generated proto code.** All generated code is derived at build time. Generated files belong in `.gitignore`.
- Kotlin targets that need `internal` visibility access use `associates = [...]` — do not change visibility modifiers to work around this.
- The `third_party/` directory wraps Maven-sourced annotation processors into reusable Bazel `java_plugin` targets. Reference these from `plugins = [...]` attributes — do not inline processor JARs.
- Package-pinning `BUILD.bazel` files (empty or near-empty) at certain directory roots are intentional. Do not add targets to them without good reason.

### Invoking Bazel

**Always use `bin/bazel`** — it activates Hermit automatically, ensuring `JAVA_HOME` and other toolchain environment variables are set correctly. Never source `activate-hermit` manually or set `JAVA_HOME` by hand.

### Examples as Independent Workspaces

Each example has its own `MODULE.bazel` and uses `local_path_override` to reference the root. When testing changes to the library, build each example workspace independently to verify it still works as a consumer.

---

## Testing Philosophy

### Prefer Unit Tests

Write unit tests first. Codegen processors are testable via in-process compilation (`kotlin-compile-testing`, `compile-testing`). Prefer compile-testing tests over integration tests for processor correctness.

### Prefer Fakes and Stubs Over Mocks

When isolating a component for testing, prefer hand-written fakes or stubs over mock frameworks. Mocks couple tests to implementation details; fakes test behavior.

### Current Test Coverage State

Test coverage is narrow. Only the APT adapter generation has real assertions. The KSP processor test is currently disabled pending a `kotlin-compile-testing` + Kotlin 2 compatibility fix (T08). The common library, ksp-apt-bridge, and both example servers have only placeholder tests.

---

## Code Style Principles

### Idiomatic for Each Processor Technology

- KSP processor code uses KSP types natively — do not introduce APT/javax.lang.model types.
- APT processor code uses the ksp-apt-bridge adapters — do not duplicate the common model.
- Java output (APT) uses `Callable<AsyncService>` with checked-exception wrapping.
- Kotlin output (KSP) uses a plain `() -> AsyncService` lambda.

### Minimal Dependencies

Prefer standard library or well-established ecosystem libraries. Do not add new Maven dependencies without updating `MODULE.bazel` and committing the updated `MODULE.bazel.lock`.

### Committing MODULE.bazel Changes

**Always include `MODULE.bazel.lock` when committing `MODULE.bazel` edits.** The lock file must travel with the manifest change. Current drift between the two is intentional (research phase) — do not regenerate the lock file speculatively.

---

## Version Control

This repository uses **jj (Jujutsu)** as the preferred VCS. A `.jj/` workspace directory is present at the repo root.

- **If `jj` is available**: use it for all commit/branch/log operations. Never run `git` commands when `.jj/` is present — git commands may produce incorrect results or corrupt jj state.
- **If `jj` is not available**: fall back to `git` — jj maintains a compatible git backend.
- Use `gh` CLI for all GitHub API interactions (PRs, issues, repo queries) regardless of VCS tool.

**Key jj equivalents:**

| Instead of… | Use… |
|---|---|
| `git log` | `jj log` |
| `git diff` | `jj diff` |
| `git status` | `jj status` |
| `git commit` | `jj commit` |
| `git push` | `jj git push` |

### Branch and PR Requirements

**It is never permissible to push directly to `main`.** All changes must go through a branch or bookmark and be submitted as a pull request:

- With jj: create a bookmark (`jj bookmark create <name>`), push it, open a PR via `gh pr create`
- With git: create a branch, push it, open a PR via `gh pr create`

**All PRs must be reviewed and merged by a human** unless the user explicitly grants the agent permission to merge a specific PR. An agent must not merge its own PRs without that explicit, one-time grant.

**Never use `--admin` or any CI bypass flag** when merging. If checks are failing, stop and report to the user.

---

## Thoughts Directory Workflow

### Creating Tickets

Tickets live in `thoughts/shared/tickets/`. Filename convention: `<kebab-case-description>.md`. Include at minimum:
- YAML frontmatter: `date`, `status`, `priority`, `area`
- Summary of the problem or goal
- Current state
- Goals or acceptance criteria
- Relevant file references (with line numbers)

### Creating Research Documents

Research documents live in `thoughts/shared/research/`. Filename convention: `YYYY-MM-DD-<description>.md` (with `.compressed.md` companion when practical). Frontmatter fields: `date`, `researcher`, `git_commit`, `branch`, `repository`, `topic`, `tags`, `status`, `last_updated`, `last_updated_by`.

### Creating Plans

Implementation plans live in `thoughts/shared/plans/`. Create a plan before implementing any non-trivial feature. Research before planning; plan before implementing.

**Plans describe intent, not implementation.** Do not write actual code in a plan — use prose, light pseudocode, or short illustrative fragments only when they communicate something prose alone cannot. The implementation agent writes the real code; the plan describes what, why, file layout, dependencies, and success criteria.

---

## Implementation Workflow

Non-trivial features are implemented using a **three-agent, test-driven workflow**. The agents work in separate commits, but coordinate — the implementation agent consults the design agent, and a review agent handles feedback. Each commit must leave the build passing.

### The Three Agents

**Design + Test Agent**
- Works from the plan in `thoughts/shared/plans/`.
- Produces the public API (interfaces, annotations, data classes) and a complete test suite that exercises it.
- Tests that cover not-yet-implemented behavior must compile but must be marked with a `// TODO: un-ignore when implemented` comment (or language-equivalent) so the build stays green.
- Commits API and test code in one or more dedicated commits before implementation begins.

**Implementation Agent**
- Works from the design agent's API and tests.
- Consults the design agent when behavior is ambiguous — does not invent API shape unilaterally.
- Does not change public API shape without design agent agreement.
- May create unit tests for purely internal implementation details (private helpers, non-public subsystems) that the design agent has no visibility into. These tests must be: unit tests only (no integration or end-to-end), simple and clear, and high-coverage for the code they target. They are committed alongside the implementation code they cover.
- Commits implementation code separately from design/test commits.
- When an implementation is complete enough that a previously-ignored test can run, removes the ignore annotation and confirms the test passes before committing.

**Review Agent**
- Monitors the open PR for comments — from the human reviewer and from any automated review systems (CI bots, linters, code review tools).
- For each comment: either addresses it (by coordinating with the design or implementation agent as appropriate) or explicitly rejects it with a written reason in the PR thread.
- May recommend new tests to the design or implementation agent, and may itself create missing tests directly related to reviewer feedback — but is not the primary owner of test coverage. If new tests fail, the review agent works with the implementation agent to fix the implementation (or, if the tests themselves are wrong, corrects them) rather than leaving failures unresolved.
- For API improvements surfaced during review: pass them to the design agent rather than making design changes unilaterally. The review agent describes the concern; the design agent decides the shape.
- For reviewer suggestions that are genuinely useful but would make the PR too large or complex: file a ticket in `thoughts/shared/tickets/`, update `overview.md`, and post the ticket path as the resolution of that comment rather than implementing it in the current PR.
- Before marking a PR ready for human merge:
  - All non-optional PR checklist items (checkboxes in the description or comments) must be checked off.
  - All open review comments must be resolved, explicitly rejected in-thread, or deferred via a filed ticket.
  - The build must be passing.

### Inter-Agent Collaboration and Escalation

These norms apply to all three agents:

- **Respect ownership boundaries.** No agent makes unilateral changes outside its domain. The review agent does not change API shape; the implementation agent does not change the public API without design agent agreement; the design agent does not write implementation internals.

- **Escalate disagreements, don't spin.** When two agents reach a conflicting conclusion, they may iterate up to three rounds to resolve it. If unresolved after three rounds, escalate to the orchestrating agent. If the orchestrating agent cannot resolve it, escalate to the human user for a tie-breaking decision.

- **Break self-loops immediately.** If any agent notices it is making the same change, reverting it, and making it again — or otherwise stalled without forward progress — it must stop and report the loop to the orchestrating agent (or directly to the human if there is no orchestrating agent). Describe: what was attempted, what blocked it, and what decision is needed to proceed.

### Commit Discipline

- Every commit must leave the build passing. If a commit would introduce a compilation error or test failure, it must also include the minimum scaffolding (stub, ignore annotation, etc.) to restore green.
- Do not mix design/test work and implementation work in the same commit.
- Do not mix unrelated changes in the same commit — a formatting fix, a feature, and a refactor are three commits.

### PR Readiness Checklist

A PR is not ready for human review until:
- [ ] Build passes (all targets, all examples)
- [ ] All PR description checkboxes checked (unless marked optional — use judgment)
- [ ] All review comments resolved or explicitly rejected in-thread
- [ ] Ticket file and `overview.md` updated to `resolved` status in the same branch

---

## Known Incomplete Work

See `thoughts/shared/tickets/overview.md` for the canonical summary of open and resolved work items, and `thoughts/shared/tickets/` for individual ticket files.

The highest-priority open items are:
- **T04** — Design the generated module shape (prerequisite for T05/T06)
- **T05/T06** — KSP and APT module generation (core feature; unblocked by T04 + T07)
- **T08** — Unblock KSP processor test (Kotlin 2 + kotlin-compile-testing compatibility)

---

## What Good Looks Like (Reference Points)

| Concern | Reference |
|---|---|
| KSP processor entry point | `io_grpc/compiler/ksp/src/main/kotlin/…/DaggerGrpcSymbolProcessor.kt` |
| APT processor entry point | `io_grpc/compiler/apt/src/main/kotlin/…/DaggerGrpcAPTProcessor.kt` |
| Shared metadata model | `io_grpc/compiler/common/src/main/kotlin/…/HandlerMetadata.kt` |
| KSP-APT bridge | `ksp-apt-bridge/src/main/kotlin/…/APTClassDeclaration.kt` |
| Runtime call context | `api/src/main/kotlin/…/GrpcCallContext.kt` |
| Armeria server wrapper | `util/armeria/src/main/kotlin/…/` |
| Java example (APT consumer) | `examples/io_grpc/bazel_build_java/` |
| Kotlin example (KSP consumer) | `examples/io_grpc/bazel_build_kt/` |
| APT processor Bazel wiring | `io_grpc/compiler/apt/BUILD.bazel` |
| KSP processor Bazel wiring | `io_grpc/compiler/ksp/BUILD.bazel` |
| third_party plugin wrapping | `third_party/dagger/BUILD.bazel`, `third_party/processors/BUILD.bazel` |
