---
id: T04
title: Design the generated module and subcomponent shape (ADR)
priority: high
phase: codegen
status: open
blocked_by: T00
blocks: T05, T06, T07
---

## Summary

Before implementing module generation (T05/T06), nail down the exact shape of what the processors will generate. The two hand-written files in the examples — `GrpcCallScopeGraph` and `GrpcHandlersModule` — are marked `@Generated("to be generated")` but their exact generated form needs design decisions resolved first.

## Motivation

Several open questions affect the generated API surface. Answering them up front avoids rework in T05/T06/T07.

## Open Design Questions

### 1. Should `GrpcCallScopeGraph.Supplier` be a library interface in `api/`?

The `Supplier` nested interface is always the same shape: `fun callScope(): GrpcCallScopeGraph` (or its equivalent). If it's part of the runtime API rather than generated, users can implement it directly in their `ApplicationGraph` component without depending on any generated type. This avoids a circular dependency between the generated subcomponent and the module that uses it.

**Options:**
- A: Generate `Supplier` as a nested interface inside the generated `GrpcCallScopeGraph`
- B: Define `GrpcCallScopeGraph.Supplier` (or a renamed equivalent) as a library interface in `api/`

### 2. Should `GrpcCallScopeGraphModule` be generated?

It's trivially always `@Module(includes = [GrpcCallContext.Module::class])`. Generating it removes one more hand-written file.

### 3. What is the generated package for the module and subcomponent?

Options: same package as the annotated handler class(es), a fixed package derived from the root package, or a configurable annotation parameter.

### 4. How are multiple handlers in different packages handled?

If `HelloWorldService` and `WhateverService` are in different packages, where does the single generated `GrpcHandlersModule` (which references both adapter classes) live?

### 5. Should `ApplicationGraphModule` (self-binding) be part of the generated output or documented as user-written?

The `ApplicationGraphModule` that binds `ApplicationGraph` as `GrpcCallScopeGraph.Supplier` is currently hand-written. Generating it would require the processor to know about the application component, which it doesn't (and shouldn't). Document as user-written, or find an alternative approach (e.g., `@BindsInstance` on the component builder).

## Acceptance Criteria

- [ ] ADR document written (can be this file, updated, or a separate `thoughts/shared/adr/` entry)
- [ ] All five questions above answered with a clear decision and rationale
- [ ] Expected generated output for a two-handler example is written out as a "golden" code snippet (this becomes the test fixture for T05/T06)
- [ ] T05 and T06 can proceed with a clear spec

## Implementation Notes

- The existing hand-written `GrpcCallScopeGraph.kt` and `GrpcHandlersModule.kt` in `examples/io_grpc/bazel_build_kt/` are the reference starting point.
- The `@Generated("to be generated")` marker on those files was placed intentionally as a breadcrumb.
