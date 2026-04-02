---
id: T05
title: Implement KSP module generation (GrpcCallScopeGraph + GrpcHandlersModule)
priority: high
phase: codegen
status: open
blocked_by: T04, T07
---

## Summary

Implement the `generateModule` function in the KSP processor so it generates the two remaining boilerplate files: `GrpcCallScopeGraph` (the Dagger `@Subcomponent`) and `GrpcHandlersModule` (the `@Module` with `@IntoSet` bindings).

Currently `module_generator.kt:5-8` only logs and does nothing.

## Affected Files

- `io_grpc/compiler/ksp/src/main/kotlin/…/module_generator.kt` — implement this
- `io_grpc/compiler/ksp/src/test/kotlin/…/DaggerGrpcSymbolProcessorTest.kt` — add test for module output (after T08 unblocks KSP tests)
- `examples/io_grpc/bazel_build_kt/` — update to remove hand-written boilerplate once generation works (tracked in T14)

## Acceptance Criteria

- [ ] For a set of `@GrpcServiceHandler`-annotated classes, the KSP processor generates a `GrpcCallScopeGraph.kt` file containing:
  - `@Subcomponent(modules = [GrpcCallScopeGraphModule::class]) @GrpcCallScope interface GrpcCallScopeGraph`
  - One provision method per handler (e.g. `fun helloWorld(): HelloWorldService`)
  - A nested `Supplier` interface (or reference to a library-defined one — per T04 decision)
- [ ] The processor generates a `GrpcHandlersModule.kt` file containing:
  - `@Module object GrpcHandlersModule` (or `@Module abstract class`)
  - One `@Provides @IntoSet fun …(): BindableService` per handler, constructing `<Name>Adapter { supplier.callScope().<name>() }`
  - The `GrpcCallScopeGraph.Supplier` injected as a constructor param (or via `@Provides` from the app graph)
- [ ] Optionally: generates `GrpcCallScopeGraphModule.kt` (`@Module(includes = [GrpcCallContext.Module::class])`)
- [ ] The Kotlin example (`bazel_build_kt`) builds with the generated files replacing the hand-written ones
- [ ] A test verifies the generated module and subcomponent output (golden file or structural assertion)

## Implementation Notes

- Use KotlinPoet's `FileSpec`/`TypeSpec`/`FunSpec` — same approach as `adapterGenerator.kt`.
- The `generateModule` function already receives `List<HandlerMetadata>` — all the info needed is available.
- `codeGenerator.createNewFile(Dependencies(false, *sourceFiles), packageName, fileName)` is the KSP output API.
- The generated `Supplier` factory method name needs a consistent convention (e.g. always `callScope()`).
- Consider the multi-package case (handlers in different packages) — see T04.
