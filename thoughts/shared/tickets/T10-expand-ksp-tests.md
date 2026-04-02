---
id: T10
title: Expand KSP processor tests
priority: medium
phase: testing
status: open
blocked_by: T08
---

## Summary

Once the KSP processor test is unblocked (T08), expand it to cover the same cases as T09 for the APT path, plus KSP-specific concerns (deferred processing, incremental processing).

## Affected Files

- `io_grpc/compiler/ksp/src/test/kotlin/…/DaggerGrpcSymbolProcessorTest.kt`

## Acceptance Criteria

**Adapter generation (mirrors T09):**
- [ ] Test: single handler → generated `*Adapter.kt` matches expected Kotlin shape (`() -> AsyncService` lambda, no try/catch)
- [ ] Test: `@GrpcServiceHandler` on an interface → compilation error with clear message
- [ ] Test: `grpcWrapperType` with no `AsyncService` inner interface → error
- [ ] Test: two handlers → two adapter files generated
- [ ] Test: service with multiple RPC methods → all methods overridden in adapter

**KSP-specific:**
- [ ] Test: deferred processing — symbol not yet resolvable in round 1 → correctly returned in `unprocessable` and resolved in round 2

**Module generation (add after T05):**
- [ ] Test: single handler → generated `GrpcCallScopeGraph.kt` matches expected shape
- [ ] Test: single handler → generated `GrpcHandlersModule.kt` matches expected shape
- [ ] Test: two handlers → subcomponent and module both reference both handlers

## Implementation Notes

- Use `kotlin-compile-testing-ksp` (once unblocked by T08).
- The existing `handlerSource` fixture in the test file is already defined; just needs the test body restored and assertions added.
- The `FooServiceGrpc` test fixture (from `foo.proto`) is shared between APT and KSP test dirs — reuse as-is or add a second service for multi-handler tests.
- The generated Kotlin output uses `() -> AsyncService` (no `Callable`, no try/catch) — assertions should check for this distinction vs the Java/APT output.
