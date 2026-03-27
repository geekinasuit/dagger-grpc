---
id: T11
title: Write real tests for common/ and ksp-apt-bridge
priority: medium
phase: testing
status: open
blocked_by: T00
---

## Summary

Both `CommonTest` and `ModelTest` are empty placeholders. Write real unit tests for the `Validator` (in `common/`) and the `ksp-apt-bridge` model classes.

## Affected Files

- `io_grpc/compiler/common/src/test/kotlin/…/CommonTest.kt` — currently `fun testFoo() {}`
- `ksp-apt-bridge/src/test/kotlin/…/ModelTest.kt` — currently `println("foo")` only

## Acceptance Criteria

### Validator tests (`CommonTest.kt`)

- [ ] `validateAnnotation`: returns `null` + logs error when `@GrpcServiceHandler` is absent
- [ ] `validateGrpcClass`: returns `null` + logs error when `grpcWrapperType` can't be resolved as a class
- [ ] `validateServiceInterface`: returns `null` + logs error when the gRPC class has no `AsyncService` inner interface
- [ ] Happy path: all three validations pass → correct `HandlerMetadata` returned with expected `name`, `packageName`, `grpcClass`, `serviceInterface`, `adapterName`
- [ ] `adapterName` computed property: `"HelloWorldService"` → `"HelloWorldServiceAdapter"`

### ksp-apt-bridge model tests (`ModelTest.kt`)

- [ ] `APTClassDeclaration.qualifiedName` and `simpleName` return correct values from a `TypeElement`
- [ ] `APTClassDeclaration.packageName` is correctly derived from the qualified name
- [ ] `APTClassDeclaration.classKind` maps `ElementKind.CLASS` → `ClassKind.CLASS`, `ElementKind.INTERFACE` → `ClassKind.INTERFACE`
- [ ] `APTAnnotation.shortName` returns the simple name of the annotation type
- [ ] `APTValueArgument.value` converts a `TypeMirror`-valued annotation argument to an `APTType` (the critical path for `grpcWrapperType`)
- [ ] `APTLogger` routes `error()` to `Messager` with `Diagnostic.Kind.ERROR`

## Implementation Notes

- Testing the `Validator` requires KSP `KSClassDeclaration` instances. In the KSP test context (`kotlin-compile-testing-ksp`), these come from a real KSP processing round. Alternatively, create simple mock/stub implementations of the KSP interfaces for unit testing the validator in isolation.
- Testing the bridge model classes requires setting up a `javax.annotation.processing` processing environment. This is straightforward with `google-compile-testing`: use `CompilationSubject` and a custom test processor that captures elements.
- The `APTValueArgument.value` TypeMirror conversion test is the most important — it's the critical path that allows the `grpcWrapperType` class reference to flow through the bridge.
