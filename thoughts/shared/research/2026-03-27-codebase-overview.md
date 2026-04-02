---
date: 2026-03-27T00:00:00-07:00
researcher: cgruber
git_commit: b5c6206366573f9ae2f654aa9ab090f11a75feda
branch: main
repository: dagger-grpc
topic: "Codebase overview: structure, Bazel build, codegen approach, and roadmap to done"
tags: [research, codebase, bazel, dagger, grpc, ksp, apt, codegen, kotlin, java, jujutsu]
status: complete
last_updated: 2026-03-27
last_updated_by: cgruber
last_updated_note: "Added Jujutsu (jj) VCS note — repo uses jj, not git"
---

# Research: Codebase Overview — dagger-grpc

**Date**: 2026-03-27
**Researcher**: cgruber
**Git Commit**: [b5c6206](https://github.com/geekinasuit/dagger-grpc/commit/b5c6206366573f9ae2f654aa9ab090f11a75feda)
**Branch**: main
**Repository**: geekinasuit/dagger-grpc

---

## Version Control

This repository uses **[Jujutsu (`jj`)](https://github.com/martinvonz/jj)** as its VCS. A `.jj/` workspace directory is present at the repo root. **Do not use `git` commands** — use `jj` equivalents:

| Instead of… | Use… |
|---|---|
| `git log` | `jj log` |
| `git diff` | `jj diff` |
| `git status` | `jj status` |
| `git commit` | `jj describe` + `jj new` (or `jj commit`) |
| `git push` | `jj git push` |

Jujutsu uses git as a storage backend, so a `.git/` directory also exists, but the working copy is managed by jj and git commands may produce incorrect results or corrupt jj state.

---

## Research Question

Provide a general view of the codebase, the Bazel build system, tool and dep versions (not just a list — how they work), the project purpose and structure, the codegen approach (what is currently generated, what is planned, the APT/KAPT vs KSP distinction), and identify a "done" checkpoint with discrete ticket-worthy steps.

---

## Summary

`dagger-grpc` is a library and code-generation toolkit that wires gRPC service implementations into a [Dagger 2](https://dagger.dev/) dependency-injection graph with **per-call scoping**. Each incoming gRPC request gets its own Dagger subcomponent ("call scope"), allowing service handler classes to be instantiated fresh per call (with full DI) rather than being singletons. The primary integration target is [Armeria](https://armeria.dev/) as the gRPC server runtime.

The project is at version `0.1`, targets JVM backends (Java or Kotlin), uses Bazel 8.1.1 with Bzlmod, and is licensed BSD 3-Clause.

**Current state:** The processors generate `*Adapter` classes that bridge the gRPC dispatch layer to the call-scope subgraph. The two key remaining pieces of boilerplate — `GrpcCallScopeGraph` (the Dagger subcomponent) and `GrpcHandlersModule` (the `@IntoSet` bindings) — are still hand-written by the user, but are marked `@Generated("to be generated")` in the examples, signalling they are intended to be generated.

---

## Detailed Findings

### 1. Module Structure

```
dagger-grpc/
├── api/                          Core public API: annotations + GrpcCallContext runtime
├── io_grpc/
│   ├── compiler/
│   │   ├── common/               Shared HandlerMetadata model + Validator (KSP-typed)
│   │   ├── ksp/                  KSP processor for Kotlin projects (generates *Adapter.kt)
│   │   └── apt/                  APT processor for Java projects (generates *Adapter.java)
│   └── lib/                      Placeholder — single stub foo.kt, no deps declared
├── ksp-apt-bridge/               KSP interface impls backed by javax.lang.model (APT)
├── util/armeria/                 Thin Armeria GrpcService wrapping helper
├── third_party/
│   ├── dagger/                   Bazel wrapper: dagger runtime + dagger-compiler java_plugin
│   └── processors/               Bazel wrapper: auto-service java_plugin
├── examples/
│   ├── io_grpc/bazel_build_java/ Standalone Java example (APT processor)
│   └── io_grpc/bazel_build_kt/   Standalone Kotlin example (KSP processor)
└── bin/                          Hermit hermetic toolchain env (hermit.hcl, activate-hermit)
```

Each example is an **independent Bazel workspace** (its own `MODULE.bazel`) that references the root as `local_path_override`. The CI pipeline discovers examples dynamically and runs each in isolation, meaning examples function as real consumer projects.

---

### 2. Annotations and Runtime API (`api/`)

Three annotations and one key runtime class:

| Symbol | Kind | Purpose |
|---|---|---|
| `@GrpcServiceHandler(grpcWrapperType: KClass<*>)` | `@Target(CLASS)` annotation | Marks a class as a gRPC call handler; triggers both processors. Carries the gRPC outer class (e.g. `HelloWorldServiceGrpc::class`). |
| `@GrpcCallScope` | JSR-330 `@Scope` | Marks objects scoped to a single gRPC call within the call-scope Dagger subcomponent. |
| `@ApplicationScope` | JSR-330 `@Scope` | Marks objects scoped to the application lifetime. |
| `GrpcCallContext` | `@GrpcCallScope`-scoped injectable class | Exposes the current call's `Metadata`, `ServerCall` authority, attributes, security level, readiness, cancellation. |

**How `GrpcCallContext` works** (`api/src/main/kotlin/…/GrpcCallContext.kt`):

```
ThreadLocal (static on Interceptor)
    ↑ set by Interceptor.interceptCall()
    ↓ read by Module.context() @Provides

GrpcCallContext.Interceptor  →  gRPC ServerInterceptor
    stores Pair(Metadata, ServerCall) into ThreadLocal before each call

GrpcCallContext.Module  →  Dagger @Module
    @Provides @GrpcCallScope fun context() = callContextThreadLocal.get()

GrpcCallContext  →  @Inject class
    wraps dagger.Lazy<Pair<Metadata, ServerCall>>; exposes .headers, .authority, etc.
```

Users register `GrpcCallContext.Interceptor` when building the Armeria server, and inject `GrpcCallContext` into their service class. It works because the interceptor fires on the same thread before dispatch reaches the service method.

---

### 3. Dagger Wiring and Call-Scope Architecture

The overall pattern (currently hand-written; generation is the goal):

```
@ApplicationScope @Component
ApplicationGraph
  └─ extends GrpcCallScopeGraph.Supplier
  └─ provides ExampleServer
  └─ modules: [ApplicationGraphModule, GrpcHandlersModule]

    ApplicationGraphModule  →  @Binds ApplicationGraph as GrpcCallScopeGraph.Supplier

    GrpcHandlersModule  →  @Provides @IntoSet BindableService  (one per @GrpcServiceHandler class)
        HelloWorldServiceAdapter { supplier.callScope().helloWorld() }
        WhateverServiceAdapter   { supplier.callScope().whatever()   }

@GrpcCallScope @Subcomponent
GrpcCallScopeGraph
  ├─ modules: [GrpcCallScopeGraphModule]   (which includes GrpcCallContext.Module)
  ├─ fun helloWorld(): HelloWorldService
  └─ fun whatever(): WhateverService
  └─ nested interface Supplier { fun callScope(): GrpcCallScopeGraph }
```

**End-to-end call flow:**

1. Server startup: `ApplicationGraph.builder().build()` creates the app-scope component.
2. `ExampleServer` injects `Set<BindableService>`; for each service, calls `wrapService(it, GrpcCallContext.Interceptor())` → Armeria `GrpcService`.
3. Request arrives → `GrpcCallContext.Interceptor.interceptCall` stores `Pair(headers, call)` in `ThreadLocal`.
4. gRPC dispatches to the adapter method (e.g. `HelloWorldServiceAdapter.sayHello`).
5. The adapter calls its provider: `service()` (Kotlin lambda) or `service.call()` (Java `Callable`).
6. The lambda calls `supplier.callScope()` → new `GrpcCallScopeGraph` subcomponent instance created.
7. `.helloWorld()` provisions a fresh `@GrpcCallScope`-scoped `HelloWorldService` via Dagger.
8. `HelloWorldService` injects `GrpcCallContext`; Dagger resolves it through `GrpcCallContext.Module` which reads the `ThreadLocal` (set in step 3).
9. RPC method body executes with full DI access to call-scoped and app-scoped objects.

---

### 4. What Is Currently Code-Generated

Both the KSP and APT processors generate `*Adapter` classes. Nothing else is generated yet.

**Generated: `<ClassName>Adapter`**

Java (APT, via JavaPoet):
```java
@Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcAPTProcessor")
public class HelloWorldServiceAdapter implements BindableService, HelloWorldServiceGrpc.AsyncService {
  private final Callable<HelloWorldServiceGrpc.AsyncService> service;
  public HelloWorldServiceAdapter(Callable<HelloWorldServiceGrpc.AsyncService> service) { ... }

  @Override public void sayHello(SayHelloRequest req, StreamObserver<SayHelloResponse> obs) {
    try { service.call().sayHello(req, obs); } catch (Exception e) { throw new RuntimeException(e); }
  }
  // ... one override per AsyncService method ...

  @Override public ServerServiceDefinition bindService() {
    return HelloWorldServiceGrpc.bindService(this);
  }
}
```

Kotlin (KSP, via KotlinPoet):
```kotlin
@Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcSymbolProcessor")
class HelloWorldServiceAdapter(private val service: () -> HelloWorldServiceGrpc.AsyncService)
    : BindableService, HelloWorldServiceGrpc.AsyncService {

  override fun sayHello(req: SayHelloRequest, obs: StreamObserver<SayHelloResponse>) =
      service().sayHello(req, obs)
  // ...

  override fun bindService(): ServerServiceDefinition =
      HelloWorldServiceGrpc.bindService(this)
}
```

The key difference: Java uses `Callable<AsyncService>` (checked exception → RuntimeException rethrow); Kotlin uses a plain `() -> AsyncService` lambda.

**Not yet generated (hand-written, marked `@Generated("to be generated")`):**

1. `GrpcCallScopeGraph` — the `@Subcomponent` interface with one provision method per handler.
2. `GrpcHandlersModule` — the `@Module` providing each `*Adapter` `@IntoSet` with its lambda wiring.
3. `GrpcCallScopeGraphModule` — the `@Module(includes = [GrpcCallContext.Module::class])` that brings call-context into the subcomponent. (Arguably generatable too, since it's trivially always the same shape.)

---

### 5. Codegen Architecture: APT vs. KSP

The project has two parallel processors that share a common model via the `ksp-apt-bridge` library.

```
io_grpc/compiler/common/   (HandlerMetadata + Validator)
        ↑                           ↑
io_grpc/compiler/ksp/        io_grpc/compiler/apt/
(KSP types natively)         (javax.lang.model via ksp-apt-bridge)
        ↓                           ↓
   KotlinPoet output           JavaPoet output
   () -> AsyncService        Callable<AsyncService>
   kt_ksp_plugin (Bazel)      java_plugin (Bazel)
   Kotlin projects            pure Java projects
```

**Why the bridge exists:** `HandlerMetadata` and `Validator` in `common/` are written entirely against the KSP type system (`KSClassDeclaration`, `KSAnnotation`, `KSType`, etc.). Rather than duplicating all this logic in APT-native form, the `ksp-apt-bridge` library provides concrete KSP interface implementations backed by `javax.lang.model` elements. The APT processor wraps each `TypeElement` in `APTClassDeclaration(typeElement)` and then calls the exact same `Validator::validate` as the KSP processor.

**How each processor is registered in Bazel:**

- **KSP:** `io_grpc/compiler/ksp/BUILD.bazel` defines a `kt_ksp_plugin` rule; the processor is discovered via `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider` checked in as a source resource. Consumers put `@dagger-grpc//io_grpc/compiler/ksp:plugin` in `plugins = [...]` on their `kt_jvm_library`.
- **APT:** `io_grpc/compiler/apt/BUILD.bazel` defines a `java_plugin` rule with explicit `processor_class`; the `@AutoService(Processor::class)` annotation on `DaggerGrpcAPTProcessor` generates the `META-INF/services/javax.annotation.processing.Processor` entry at build time (via the `//third_party/processors:auto-service` plugin applied to the processor library itself). Consumers put `@dagger-grpc//io_grpc/compiler/apt:plugin` in `plugins = [...]` on their `java_library`.

**The ksp-apt-bridge — what's implemented vs. TODO:**

Fully implemented (needed for current adapter generation):
- `APTClassDeclaration`: `annotations`, `declarations`, `getAllFunctions()`, `qualifiedName`, `simpleName`, `packageName`, `classKind`
- `APTAnnotation`: `shortName`, `arguments`
- `APTValueArgument`: `name`, `value` (including TypeMirror → APTType conversion for class-valued annotation args)
- `APTFunctionDeclaration`: `parameters`, `simpleName`
- `APTLogger`: full KSPLogger → Messager bridge with source-element attribution

Not yet implemented (`TODO("Not yet implemented")`):
- Most of `APTFunctionDeclaration` (return type, modifiers, type parameters, annotations, etc.)
- All of `APTPropertyDeclaration`
- All of `APTTypeArgument`
- Most type-system methods on `APTType` and `APTClassDeclaration` (superTypes, primaryConstructor, containingFile, etc.)

The partial implementation is sufficient for adapter generation. Module generation will likely require extending the bridge further (e.g., reading annotations on the handler class to determine scoping).

---

### 6. Bazel Build System

**Version:** Bazel 8.1.1 (`.bazelversion`). Modern Bzlmod (`MODULE.bazel`), no WORKSPACE file. Developer environment managed via Hermit (`bin/hermit.hcl`).

**No `.bazelrc` file** — all flags are at defaults.

**Root module:** `module(name = "dagger-grpc", version = "0.1")` — this name is what consumers reference as `@dagger-grpc//...`.

**Bazel module dependencies and their roles:**

| Dep | Version | Role in this build |
|---|---|---|
| `bazel_skylib` | `1.7.1` | `build_test` rule used in example top-level BUILD files to assert targets build |
| `rules_java` | `8.9.0` | `java_library`, `java_plugin`, `java_binary`, `java_test` — used for APT processor, Java example, third_party wrapping |
| `rules_kotlin` | `2.1.0` | `kt_jvm_library`, `kt_jvm_test`, `kt_jvm_binary`, `kt_ksp_plugin` — Kotlin compilation throughout |
| `rules_jvm_external` | `6.7` | Maven artifact resolution; provides `@maven//…` labels |
| `rules_proto` | `7.1.0` | Transitive dep; `proto_library` in practice loaded from `@protobuf` |
| `protobuf` | `23.1` | `proto_library` rule; `@protobuf//java/core` compile dep |
| `grpc` | `1.69.0` | gRPC C-core; transitive dep of grpc-java |
| `grpc-java` | `1.69.0` | `java_grpc_library` rule for generating Java gRPC stubs from protos |
| `rules_proto_grpc_java` | `5.0.1` | `java_proto_library` rule for generating Java message classes |

**Kotlin toolchain note:** The root `BUILD.bazel` has `define_kt_toolchain` commented out — the build uses the default Kotlin toolchain bundled with rules_kotlin 2.1.0. No explicit Kotlin language/API version or JVM target is pinned.

**Key Maven dependencies (via `maven.install`):**

| Artifact | Version | Used for |
|---|---|---|
| `com.google.dagger:dagger` | `2.55` | Dagger runtime (wrapped at `//third_party/dagger`) |
| `com.google.dagger:dagger-compiler` | `2.55` | Dagger APT processor (wrapped as `java_plugin` at `//third_party/dagger:dagger-compiler`, `generates_api=1`) |
| `com.google.devtools.ksp:symbol-processing-api` | `1.9.0-1.0.12` | KSP processor API + ksp-apt-bridge |
| `com.squareup:kotlinpoet-jvm` | `2.1.0` | Kotlin source codegen (KSP processor) |
| `com.squareup:kotlinpoet-ksp` | `2.1.0` | KSP-specific KotlinPoet extensions |
| `com.squareup:javapoet` | `1.13.0` | Java source codegen (APT processor) |
| `com.google.auto:auto-common` | `1.2.2` | APT utilities (ksp-apt-bridge, APT processor) |
| `com.google.auto.service:auto-service` | `1.1.1` | Generates META-INF/services for APT processor registration |
| `com.linecorp.armeria:armeria-grpc` | `1.26.4` | Armeria gRPC integration (util/armeria, examples) |
| `com.linecorp.armeria:armeria` | `1.26.4` | Armeria HTTP/2 server (examples) |
| `io.grpc:grpc-kotlin-stub` | `1.4.1` | gRPC Kotlin coroutine stubs (Kotlin example) |
| `io.grpc:grpc-netty-shaded` | `1.71.0` | gRPC Netty transport (examples) |
| `io.grpc:grpc-protobuf` | `1.71.0` | gRPC Protobuf integration |
| `io.grpc:grpc-stub` | `1.71.0` | gRPC stub base |
| `com.github.tschuchortdev:kotlin-compile-testing` | `1.6.0` | In-process Kotlin/APT test compilation |
| `com.github.tschuchortdev:kotlin-compile-testing-ksp` | `1.6.0` | KSP-specific compile testing |
| `com.google.testing.compile:compile-testing` | `0.21.0` | Java APT compile testing framework (APT tests) |
| `com.google.truth:truth` | `1.4.4` | Test assertions |
| `javax.inject:javax.inject` | `1` | JSR-330 (bundled into `//third_party/dagger`) |

**Notable version skew:** `@protobuf//java/core` (from the Bazel module, v23.1) and `@maven//:com_google_protobuf_protobuf_java` (v4.29.3) both appear in the build. The Bazel module version and Maven version are independent pins and may diverge.

**How APT plugins are wired through Bazel:**

```
third_party/processors/BUILD.bazel
  java_plugin(name="auto-service", processor_class="AutoServiceProcessor", ...)

io_grpc/compiler/apt/BUILD.bazel
  kt_jvm_library(name="apt", plugins=["//third_party/processors:auto-service"], ...)
    → @AutoService on DaggerGrpcAPTProcessor generates META-INF/services at build time
  java_plugin(name="plugin", processor_class="DaggerGrpcAPTProcessor", deps=[":apt"])

examples/bazel_build_java/service/armeria/BUILD.bazel
  java_library(plugins=["@dagger-grpc//io_grpc/compiler/apt:plugin",
                         "@dagger-grpc//third_party/dagger:dagger-compiler"], ...)
    → Both APT processors run at compile time; Dagger generates component impls,
      dagger-grpc generates *Adapter.java files
```

**How KSP plugins are wired through Bazel:**

```
io_grpc/compiler/ksp/BUILD.bazel
  kt_jvm_library(name="compile", resources=glob(["META-INF/services/**"]), ...)
    → META-INF/services/SymbolProcessorProvider is a source file
  kt_ksp_plugin(name="plugin", processor_class="DaggerGrpcProcessor", deps=[":compile"])

examples/bazel_build_kt/service/armeria/BUILD.bazel
  kt_jvm_library(plugins=["@dagger-grpc//io_grpc/compiler/ksp:plugin",
                           "@dagger-grpc//third_party/dagger:dagger-compiler"], ...)
    → rules_kotlin routes kt_ksp_plugin and APT java_plugin appropriately
```

**The `associates` pattern:** Several `kt_jvm_library` and `kt_jvm_test` targets use the `associates` attribute to gain `internal` visibility across Kotlin modules:
- `io_grpc/compiler/apt`: `associates = ["//io_grpc/compiler/common"]`
- `io_grpc/compiler/ksp`: `associates = ["//io_grpc/compiler/common"]`
- Test targets associate with the library under test

**The `third_party/` wrapping pattern:** Bazel's `java_plugin` requires a declared Bazel target with a `processor_class`, not just a JAR. The `third_party/` directory wraps Maven-sourced annotation processors into reusable plugin targets that can be referenced cleanly across the build and from example sub-workspaces.

---

### 7. Test Coverage

| Test | Status | What it tests |
|---|---|---|
| `DaggerGrpcAPTProcessorTest.testSimpleCompilation` | **Real** | APT processor generates correct `*Adapter.java` for `@GrpcServiceHandler` class |
| `DaggerGrpcAPTProcessorTest.testValidationFailAnnotationOnInterface` | **Real** | APT processor emits error when annotation applied to interface |
| `DaggerGrpcSymbolProcessorTest.testHandlerGenerator` | **Commented out** | TODO: `kotlin-compile-testing` broken with Kotlin 2 |
| `CommonTest.testFoo` | Placeholder | Empty method body |
| `ModelTest.testFoo` | Placeholder | `println("foo")` only |
| `ServiceTest` (Java example) | Placeholder | Empty `placeholderTest()` |
| `ServiceTest` (Kotlin example) | Placeholder | Empty `placeholderTest()` |

Test coverage is narrow: only the APT adapter generation has real assertions. The KSP processor, the common library, the ksp-apt-bridge model, and both example servers have no meaningful tests.

---

### 8. Known Issues and Incomplete Areas

1. **`module_generator.kt` is a stub** (`io_grpc/compiler/ksp/…/module_generator.kt:5-8`): `generateModule` logs and does nothing. The KSP processor calls it but no files are written.

2. **APT module generation is commented out** (`DaggerGrpcAPTProcessor.kt:48-51`): `env.generateModule(handlerMetadatas)` is commented out.

3. **KSP processor test disabled** (`DaggerGrpcSymbolProcessorTest.kt:37-46`): `// TODO(cgruber): Fix this, once kotlin-compile-testing works with Kotlin2.`

4. **ksp-apt-bridge partially implemented**: Many `APTFunctionDeclaration`, `APTPropertyDeclaration`, `APTType`, `APTTypeArgument` members are `TODO("Not yet implemented")`. Only the subset needed for adapter generation is implemented.

5. **`io_grpc/lib/` is a stub**: Contains a single `foo.kt` placeholder with no meaningful content or deps. Purpose unclear.

6. **Kotlin example Bazel build uses stale WORKSPACE-style deps** (`bazel_build_kt/proto/BUILD.bazel`, `client/BUILD.bazel`): Uses `@io_bazel_rules_kotlin` (old) and `@grpc-kotlin` (not declared in `MODULE.bazel`). This example likely does not build with current Bzlmod.

7. **`define_kt_toolchain` commented out** in root `BUILD.bazel:3-8`: Kotlin language/API version and JVM target are unpinned.

8. **Proto version skew**: `@protobuf//java/core` (Bazel module, v23.1) vs `@maven//:com_google_protobuf_protobuf_java` (v4.29.3) are two independent pins.

---

## Architecture Insights

- **The adapter pattern solves a fundamental gRPC/DI impedance mismatch**: gRPC requires a long-lived `BindableService` object registered at startup, but DI wants to create short-lived, scope-aware objects per request. The `*Adapter` class is a gRPC-registered singleton that lazily delegates to a freshly-created Dagger-managed service instance per call.

- **ThreadLocal for call context** is idiomatic for synchronous/thread-per-request gRPC interceptors, but it implies the Dagger subcomponent must be created on the same thread the interceptor runs. This works for Armeria's default threading model but could be fragile with async dispatch models.

- **`ksp-apt-bridge` is an intentional architecture choice** (documented in its README) rather than a shortcut: it lets the shared `common/` code be written once in idiomatic KSP style, and the APT bridge "pays the translation tax" to support Java-only projects. The tradeoff is that the bridge must be extended whenever new KSP API surface is needed.

- **Examples are real consumer projects** (standalone `MODULE.bazel` with `local_path_override`), not just internal Bazel packages. This is a strong design choice that catches integration breakage in CI that purely internal tests wouldn't.

---

## "Done" Checkpoint

The project is "done" (v1.0 complete) when a developer can:

1. Add `dagger-grpc` to their Bazel workspace (Java or Kotlin).
2. Annotate their gRPC service implementation classes with `@GrpcServiceHandler` and `@GrpcCallScope`.
3. Define a top-level `@ApplicationScope @Component` with `ExampleServer` as a provision.
4. Write a `main()` that starts the server.

…and get a **fully functional, Dagger-wired gRPC server with zero additional boilerplate**. No hand-written `GrpcCallScopeGraph`, `GrpcHandlersModule`, `GrpcCallScopeGraphModule`, or `ApplicationGraphModule`. The processors generate everything.

---

## Ticket Breakdown: Steps to Done

### Phase 1 — Infrastructure & Build Fixes

**T1: Fix Kotlin example Bzlmod migration**
- `bazel_build_kt/proto/BUILD.bazel` loads from `@grpc-kotlin` (not in MODULE.bazel).
- `bazel_build_kt/client/BUILD.bazel` and `service/armeria/BUILD.bazel` load from `@io_bazel_rules_kotlin` (old name).
- Fix: add `grpc-kotlin` as a `bazel_dep` in the example `MODULE.bazel`, update load statements to use `@rules_kotlin`.
- Scope: example sub-workspace only; no library code changes.

**T2: Pin Kotlin toolchain version**
- Uncomment and configure `define_kt_toolchain` in root `BUILD.bazel`.
- Explicitly set Kotlin language version, API version, and JVM target.
- Propagate toolchain settings to example sub-workspaces.
- Scope: root + both example MODULE.bazel files.

**T3: Resolve proto version skew**
- Audit uses of `@protobuf//java/core` vs `@maven//:com_google_protobuf_protobuf_java`.
- Standardize on a single protobuf artifact source (prefer Maven or prefer Bazel module, consistently).
- Scope: root MODULE.bazel + affected BUILD files.

---

### Phase 2 — Module Generation (Core Feature)

**T4: Design generated GrpcCallScopeGraph shape**
- Define the exact structure the processor should generate for `GrpcCallScopeGraph`.
- Key questions: Does it include `GrpcCallScopeGraphModule` (trivially always `includes = [GrpcCallContext.Module::class]`)? Is the `Supplier` nested interface generated or required by the runtime API?
- Output: ADR (or comments/tests) specifying the expected generated code shape.
- Scope: design-only; no code changes yet.

**T5: Implement KSP module generation**
- Implement `module_generator.kt`'s `generateModule` function to generate both:
  - `GrpcCallScopeGraph.kt` — `@Subcomponent(modules=[GrpcCallScopeGraphModule::class]) @GrpcCallScope interface` with one provision method per handler + nested `Supplier` interface.
  - `GrpcHandlersModule.kt` — `@Module object` with one `@Provides @IntoSet fun …(): BindableService` per handler, constructing the `*Adapter` with a lambda calling `supplier.callScope().serviceName()`.
  - Optionally: `GrpcCallScopeGraphModule.kt` — `@Module(includes=[GrpcCallContext.Module::class]) object`.
- Requires KotlinPoet for multi-file output from one processor round.
- Scope: `io_grpc/compiler/ksp/`, `module_generator.kt`; extend `ksp-apt-bridge` if needed (T7).

**T6: Implement APT module generation (Java)**
- Mirror T5 for the APT processor.
- Uncomment `env.generateModule(handlerMetadatas)` in `DaggerGrpcAPTProcessor.kt:48-51`.
- Implement Java module generation in `AdapterGenerator.kt` (or a new `ModuleGenerator.kt`), generating Java equivalents of the Dagger module + subcomponent using JavaPoet.
- Key difference: constructor takes `Callable<AsyncService>`, not a Kotlin lambda.
- Scope: `io_grpc/compiler/apt/`; likely requires extending ksp-apt-bridge (T7).

**T7: Extend ksp-apt-bridge as needed for module generation**
- Module generation will need to read annotations on handler classes (e.g. confirming `@GrpcCallScope`, extracting package name for generated module placement).
- Fill in `TODO("Not yet implemented")` members of the bridge as needed by T5/T6.
- Scope: `ksp-apt-bridge/`; driven by the requirements discovered in T5/T6.

---

### Phase 3 — Testing

**T8: Unblock KSP processor test (kotlin-compile-testing + Kotlin 2)**
- Investigate current state of `kotlin-compile-testing` and Kotlin 2 compatibility.
- Either: upgrade `kotlin-compile-testing-ksp` to a version that supports Kotlin 2, or migrate to an alternative in-process KSP testing approach.
- Uncomment and fix `DaggerGrpcSymbolProcessorTest.testHandlerGenerator`.
- Scope: `io_grpc/compiler/ksp/src/test/`.

**T9: Expand APT processor tests**
- Add tests for: multi-handler input (two `@GrpcServiceHandler` classes → two adapters), error case for wrong `grpcWrapperType` (class without `AsyncService` inner interface), generated module output (once T6 is done).
- Scope: `io_grpc/compiler/apt/src/test/`.

**T10: Expand KSP processor tests (after T8)**
- Add tests mirroring T9 for the KSP path.
- Add test for generated module output (once T5 is done).
- Scope: `io_grpc/compiler/ksp/src/test/`.

**T11: Write real tests for `common/` and `ksp-apt-bridge`**
- `CommonTest.testFoo()` and `ModelTest.testFoo()` are placeholders.
- Write unit tests for `Validator` (all pass/fail paths) and for the bridge's type resolution (e.g. `APTValueArgument.value` TypeMirror conversion).
- Scope: `io_grpc/compiler/common/src/test/`, `ksp-apt-bridge/src/test/`.

**T12: Write integration tests for examples**
- `ServiceTest.java` and `ServiceTest.kt` are empty placeholders.
- Write tests that start the Armeria server and send real gRPC requests using the generated client stubs, asserting correct responses and header propagation via `GrpcCallContext`.
- Scope: `examples/io_grpc/bazel_build_java/service/armeria/src/test/`, `examples/io_grpc/bazel_build_kt/service/armeria/src/test/`.

---

### Phase 4 — Runtime & Cleanup

**T13: Define `io_grpc/lib/` purpose and implement if needed**
- `io_grpc/lib/` is a stub with only `foo.kt`. Either:
  - Move runtime helpers (e.g. `util/armeria`, `GrpcCallContext`) into `lib/` and make it the canonical runtime dep, or
  - Remove `lib/` if `api/` and `util/armeria` are sufficient, or
  - Populate `lib/` with a planned server-builder API that wraps the full Armeria setup.
- Scope: architecture decision + `io_grpc/lib/`, `util/armeria/`.

**T14: Remove generated code from examples**
- Once T5 and T6 are complete and generating `GrpcCallScopeGraph`, `GrpcHandlersModule`, and `GrpcCallScopeGraphModule`, remove the hand-written versions from both examples.
- Remove the `@Generated("to be generated")` annotations (they should be replaced by real `@Generated` annotations on the generated files).
- Scope: both example projects.

---

### Phase 5 — Documentation

**T15: Write user-facing integration guide**
- Step-by-step: add to Bazel MODULE.bazel, annotate your service class, define ApplicationGraph, write main(). Show both Java (APT) and Kotlin (KSP) variants.
- Document the call-scope pattern, how to inject `GrpcCallContext`, and how to add application-scoped deps.
- Scope: `README.md` or `docs/`.

---

## Code References

- `api/src/main/kotlin/…/GrpcServiceHandler.kt` — `@GrpcServiceHandler` annotation definition
- `api/src/main/kotlin/…/GrpcCallContext.kt` — runtime context, interceptor, and Dagger module
- `api/src/main/kotlin/…/GrpcCallScope.kt:10-11` — `@GrpcCallScope` scope annotation
- `io_grpc/compiler/common/src/main/kotlin/…/metadata.kt:6-19` — `HandlerMetadata` shared model
- `io_grpc/compiler/common/src/main/kotlin/…/validations.kt` — `Validator` (validates annotation + AsyncService inner interface)
- `io_grpc/compiler/ksp/src/main/kotlin/…/DaggerGrpcSymbolProcessor.kt:12-28` — KSP processing loop
- `io_grpc/compiler/ksp/src/main/kotlin/…/adapterGenerator.kt:20-95` — KotlinPoet adapter generation
- `io_grpc/compiler/ksp/src/main/kotlin/…/module_generator.kt:5-8` — **stub, generates nothing**
- `io_grpc/compiler/apt/src/main/kotlin/…/DaggerGrpcAPTProcessor.kt:14-53` — APT processing loop; module call commented out at line 48-51
- `io_grpc/compiler/apt/src/main/kotlin/…/AdapterGenerator.kt:19-113` — JavaPoet adapter generation
- `ksp-apt-bridge/src/main/kotlin/…/APTClassDeclaration.kt` — KSP `KSClassDeclaration` over APT `TypeElement`
- `ksp-apt-bridge/src/main/kotlin/…/APTValueArgument.kt:36-43` — critical TypeMirror → APTType conversion
- `util/armeria/src/main/kotlin/…/armeria_grpc_util.kt:7-12` — `wrapService()` helper
- `examples/io_grpc/bazel_build_kt/service/armeria/src/main/kotlin/…/dagger/GrpcCallScopeGraph.kt:9-19` — hand-written subcomponent (target for generation)
- `examples/io_grpc/bazel_build_kt/service/armeria/src/main/kotlin/…/dagger/GrpcHandlersModule.kt:13-27` — hand-written module (target for generation)
- `io_grpc/compiler/apt/src/test/kotlin/…/DaggerGrpcAPTProcessorTest.kt:29-68` — expected generated adapter output (ground truth for Java generation)
- `io_grpc/compiler/ksp/src/test/kotlin/…/DaggerGrpcSymbolProcessorTest.kt:37-46` — **commented out, TODO Kotlin2**
- `MODULE.bazel:22-63` — full Maven artifact list with versions
- `BUILD.bazel:3-8` — commented-out `define_kt_toolchain`

---

## Open Questions

1. **Should `GrpcCallScopeGraph.Supplier` be part of the runtime API** (in `api/`) rather than generated? If it's always the same interface shape, making it a library type (e.g. `interface GrpcCallScopeFactory`) could simplify the generated subcomponent and the application component contract.

2. **Should `ApplicationGraphModule` (self-binding of ApplicationGraph as Supplier) be eliminated** by making `ApplicationGraph` extend a framework-provided interface directly, or by using an `@BindsInstance` factory approach?

3. **ThreadLocal for `GrpcCallContext`**: Is this the right approach for async Armeria dispatch, or should there be a coroutine-context or gRPC `Context`-based approach for Kotlin coroutines? Relevant once the Kotlin path is more mature.

4. **Square Wire support** (mentioned in `io_grpc/README.md`): Is this planned for the current project phase, or deferred post-v1?

5. **`io_grpc/lib/` purpose**: What is meant to live there? Is it the server-builder helper API, the runtime library for things currently in `api/`, or something else?

6. **Should `util/armeria/` become `util/armeria/` + `util/grpc-netty/` etc.?** Currently Armeria is the only integration target. Is direct `grpc-netty` server wiring planned?
