---
id: T14
title: Remove hand-written boilerplate from examples after module generation is complete
priority: medium
phase: cleanup
status: open
blocked_by: T05, T06
---

## Summary

Once T05 (KSP module generation) and T06 (APT module generation) are complete and generating `GrpcCallScopeGraph`, `GrpcHandlersModule`, and (optionally) `GrpcCallScopeGraphModule`, remove the hand-written versions of those files from both examples. The examples should then demonstrate the fully processor-driven workflow.

## Affected Files

**Kotlin example (`bazel_build_kt`):**
- `…/dagger/GrpcCallScopeGraph.kt` — delete (marked `@Generated("to be generated")`)
- `…/dagger/GrpcHandlersModule.kt` — delete (marked `@Generated("to be generated")`)
- `…/dagger/GrpcCallScopeGraphModule.kt` — delete if now generated

**Java example (`bazel_build_java`):**
- `…/dagger/GrpcCallScopeGraph.java` — delete (marked `@Generated("to be generated")`)
- `…/dagger/GrpcHandlersModule.java` — delete
- `…/dagger/GrpcCallScopeGraphModule.java` — delete if now generated

**Both examples:**
- `ApplicationGraph` — update to extend/implement the generated `Supplier` type
- `ApplicationGraphModule` — update or remove based on T04 design decisions

## Acceptance Criteria

- [ ] No hand-written files carry `@Generated("to be generated")` in either example
- [ ] Both examples build end-to-end using only processor-generated subcomponent/module files
- [ ] Both example integration tests (T12) continue to pass
- [ ] CI passes for both examples

## Implementation Notes

- This ticket proves the processor output is complete and correct — if the examples don't build after deletion, it reveals gaps in T05/T06.
- `ApplicationGraph` still needs to be hand-written by the user (it's the top-level component, which the processor has no knowledge of). Update documentation to clarify this is intentional.
- The `ApplicationGraphModule` situation depends on the T04 design decision about `GrpcCallScopeGraph.Supplier`.
