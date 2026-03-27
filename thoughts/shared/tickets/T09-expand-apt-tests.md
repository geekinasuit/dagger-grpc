---
id: T09
title: Expand APT processor tests
priority: medium
phase: testing
status: open
blocked_by: T00
---

## Summary

The APT processor currently has two tests: `testSimpleCompilation` (single handler, happy path) and `testValidationFailAnnotationOnInterface` (error case). Expand coverage for edge cases, multi-handler scenarios, and — once T06 is done — module generation output.

## Affected Files

- `io_grpc/compiler/apt/src/test/kotlin/…/DaggerGrpcAPTProcessorTest.kt`

## Acceptance Criteria

**Error cases:**
- [ ] Test: `@GrpcServiceHandler` with a `grpcWrapperType` that has no `AsyncService` inner interface → compilation error with a clear message
- [ ] Test: `@GrpcServiceHandler` with a non-class `grpcWrapperType` (e.g. an interface) → error

**Multi-handler:**
- [ ] Test: two `@GrpcServiceHandler` classes in the same compilation unit → two `*Adapter` files generated, each correct
- [ ] Test: two handlers with different package names → adapters generated in correct packages

**Adapter correctness:**
- [ ] Test: a service with multiple RPC methods → all methods are overridden in the generated adapter
- [ ] Test: a service with zero RPC methods (edge case) → adapter still compiles

**Module generation (add after T06):**
- [ ] Test: single handler → generated `GrpcCallScopeGraph.java` matches expected shape
- [ ] Test: single handler → generated `GrpcHandlersModule.java` matches expected shape
- [ ] Test: two handlers → both provision methods in `GrpcCallScopeGraph`, both bindings in `GrpcHandlersModule`

## Implementation Notes

- Use `google-compile-testing` (`@maven//:com_google_testing_compile_compile_testing`) for all tests — same framework as existing tests.
- The `FooServiceGrpc` test fixture (generated from `foo.proto` in the test directory) can be reused for most cases. Add a `foo2.proto` or a second service in `foo.proto` for multi-handler tests.
- Use `JavaFileObjects.forSourceString` or `forResource` to create test inputs.
- `Compilation.generatedSourceFiles()` returns all generated files; assert on count and content.
