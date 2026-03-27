---
id: T08
title: Unblock KSP processor test (kotlin-compile-testing + Kotlin 2)
priority: high
phase: testing
status: open
blocked_by: T00
blocks: T10
---

## Summary

The KSP processor test (`DaggerGrpcSymbolProcessorTest.testHandlerGenerator`) is entirely commented out with:

```kotlin
// TODO(cgruber): Fix this, once kotlin-compile-testing works with Kotlin2.
```

The `kotlin-compile-testing-ksp` library (version 1.6.0) does not support Kotlin 2 compilation. This leaves the KSP processor path completely untested.

## Affected Files

- `io_grpc/compiler/ksp/src/test/kotlin/…/DaggerGrpcSymbolProcessorTest.kt:37-46` — commented-out test body
- `MODULE.bazel` / `maven.install` — may need version bump for `kotlin-compile-testing-ksp`

## Acceptance Criteria

- [ ] `DaggerGrpcSymbolProcessorTest.testHandlerGenerator` runs and asserts the generated adapter output
- [ ] The test verifies the generated Kotlin adapter matches the expected structure (analogous to `DaggerGrpcAPTProcessorTest.testSimpleCompilation`)
- [ ] CI passes with the KSP test enabled

## Implementation Notes

### Option A: Upgrade kotlin-compile-testing
- Check if a newer version of `com.github.tschuchortdev:kotlin-compile-testing-ksp` supports Kotlin 2.
- As of early 2025, the library has had Kotlin 2 compatibility work; check https://github.com/tschuchort/kotlin-compile-testing for current status.
- If upgrading, also update `com.github.tschuchortdev:kotlin-compile-testing` to the same version.

### Option B: Use KSP's own test infrastructure
- KSP provides `com.google.devtools.ksp:symbol-processing` test utilities.
- More complex setup but less dependency on a third-party test library.

### Option C: Pin back to Kotlin 1.9
- If module generation (T05) requires Kotlin 2 features, this is not viable long-term.
- Short-term fix only.

**Recommendation:** Try Option A first. If kotlin-compile-testing 1.6+ has a Kotlin 2 release, update the Maven pin and uncomment the test.

### Test fixture

The existing `handlerSource` fixture in the test file (lines 7-33) defines a `Source1` class annotated `@GrpcServiceHandler`. It just needs the test body uncommented (with any API adjustments for the new library version) and an assertion against the generated output matching the Kotlin adapter shape.
