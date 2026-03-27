---
id: T06
title: Implement APT module generation (Java GrpcCallScopeGraph + GrpcHandlersModule)
priority: high
phase: codegen
status: open
blocked_by: T04, T07
---

## Summary

Implement Java module generation in the APT processor. The call to `env.generateModule(handlerMetadatas)` is commented out in `DaggerGrpcAPTProcessor.kt:48-51`. Un-comment it and implement the generation using JavaPoet.

## Affected Files

- `io_grpc/compiler/apt/src/main/kotlin/…/DaggerGrpcAPTProcessor.kt:48-51` — uncomment the call
- `io_grpc/compiler/apt/src/main/kotlin/…/AdapterGenerator.kt` — add module generation (or create `ModuleGenerator.kt`)
- `io_grpc/compiler/apt/src/test/kotlin/…/DaggerGrpcAPTProcessorTest.kt` — add test for module output
- `examples/io_grpc/bazel_build_java/` — update to remove hand-written boilerplate once generation works (T14)

## Acceptance Criteria

- [ ] For a set of `@GrpcServiceHandler`-annotated classes, the APT processor generates a `GrpcCallScopeGraph.java` file containing:
  - `@Subcomponent(modules = {GrpcCallScopeGraphModule.class}) @GrpcCallScope interface GrpcCallScopeGraph`
  - One provision method per handler
  - A nested `Supplier` interface (or reference to a library-defined one — per T04 decision)
- [ ] The processor generates a `GrpcHandlersModule.java` file containing:
  - `@Module abstract class GrpcHandlersModule` (or equivalent)
  - One `@Provides @IntoSet static BindableService` method per handler, constructing `new <Name>Adapter(() -> supplier.callScope().<name>())` (or using `Callable`)
- [ ] Optionally: generates `GrpcCallScopeGraphModule.java`
- [ ] The Java example (`bazel_build_java`) builds with the generated files replacing the hand-written ones
- [ ] A test verifies the generated module and subcomponent output

## Implementation Notes

- The Java adapter uses `Callable<AsyncService>` rather than a Kotlin lambda — the module's binding lambda must match: `() -> supplier.callScope().helloWorld()` as a `Callable`.
- JavaPoet's `MethodSpec`, `TypeSpec`, `JavaFile` — same pattern as `AdapterGenerator.kt`.
- The `HandlerMetadata` list is available from the APT processing loop; same data as KSP gets.
- Ensure `generates_api = 1` on the `java_plugin` in `io_grpc/compiler/apt/BUILD.bazel` is still correct (it is — Dagger needs to see the generated component interfaces).
- Consider a shared `ModuleGenerator` interface/abstraction between KSP and APT, or keep them fully separate (simpler given the KotlinPoet vs JavaPoet difference).
