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

---

## Decisions (confirmed)

### Q1 — No `Supplier` interface; use `@Subcomponent.Factory`

**Decision:** Drop the `GrpcCallScopeGraph.Supplier` nested interface entirely. Use `@Subcomponent.Factory` instead.

**Rationale:** `@Subcomponent.Factory` was designed for exactly this pattern. When `GrpcCallScopeGraphModule` declares `subcomponents = [GrpcCallScopeGraph::class]`, Dagger automatically makes `GrpcCallScopeGraph.Factory` injectable in the parent component — no explicit binding, no user-written module needed. This eliminates `Supplier` from the generated API surface and removes all user boilerplate from the wiring.

**Dagger version floor:** `@Subcomponent.Factory` requires Dagger 2.25+ (released 2019). Acceptable for this project.

---

### Q2 — Generate `GrpcCallScopeGraphModule`

**Decision:** Yes, generate it.

**Rationale:** It is always `@Module(includes = [GrpcCallContext.Module::class], subcomponents = [GrpcCallScopeGraph::class])` with no body. Completely mechanical; zero variation across applications. Generating it means users never write or maintain it. The `subcomponents` declaration is what makes `GrpcCallScopeGraph.Factory` injectable in the parent component — the mechanism that eliminates `ApplicationGraphModule`.

---

### Q3 — Require explicit processor option `daggergrpc.package`

**Decision:** The target package for all generated classes is specified via a required processor option. No auto-derivation in the MVP.

- KSP: `ksp { arg("daggergrpc.package", "com.example.armeria.dagger") }`
- APT: `-Adaggergrpc.package=com.example.armeria.dagger`

If the option is absent, the processor emits a compile error. Auto-derivation from the common ancestor package of annotated classes may be added as a V2 convenience feature.

---

### Q4 — Multi-package handlers: no special case needed

**Decision:** The explicit `daggergrpc.package` option resolves this naturally. Handler and adapter classes are public; cross-package references from the generated classes work without visibility issues.

---

### Q5 — `ApplicationGraphModule` is eliminated

**Decision:** `ApplicationGraphModule` is no longer needed and should not be documented as user-written boilerplate. With `@Subcomponent.Factory`, Dagger automatically provides `GrpcCallScopeGraph.Factory` to the parent component once `GrpcCallScopeGraphModule` (which declares the subcomponent) is included. The user wires exactly one thing: include `GrpcHandlersModule` in their `@Component`.

The existing hand-written `ApplicationGraphModule` in both examples will be deleted as part of T05/T06 once code generation produces the correct output.

---

### Naming — strip `Service` suffix from provision methods

**Decision:** Provision method names on `GrpcCallScopeGraph` are derived from the handler simple class name, lowercased first character, with the `Service` suffix stripped. Provider method names on `GrpcHandlersModule` follow the same base name with `Handler` appended.

- `HelloWorldService` → provision: `helloWorld()`, provider: `helloWorldHandler(...)`
- `WhateverService` → provision: `whatever()`, provider: `whateverHandler(...)`

---

## Generated Output Golden — Two-Handler Case

Reference handlers: `HelloWorldService` and `WhateverService` in `com.example.armeria.services`.
Configured package: `com.example.armeria.dagger`.

### Kotlin (KSP output)

**`GrpcCallScopeGraph.kt`**
```kotlin
package com.example.armeria.dagger

import com.example.armeria.services.HelloWorldService
import com.example.armeria.services.WhateverService
import com.geekinasuit.daggergrpc.api.GrpcCallScope
import dagger.Subcomponent
import javax.annotation.Generated

@Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcSymbolProcessor")
@Subcomponent(modules = [GrpcCallScopeGraphModule::class])
@GrpcCallScope
interface GrpcCallScopeGraph {
  fun helloWorld(): HelloWorldService
  fun whatever(): WhateverService

  @Subcomponent.Factory
  interface Factory {
    fun create(): GrpcCallScopeGraph
  }
}
```

**`GrpcCallScopeGraphModule.kt`**
```kotlin
package com.example.armeria.dagger

import com.geekinasuit.daggergrpc.api.GrpcCallContext
import dagger.Module
import javax.annotation.Generated

@Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcSymbolProcessor")
@Module(
  includes = [GrpcCallContext.Module::class],
  subcomponents = [GrpcCallScopeGraph::class],
)
object GrpcCallScopeGraphModule
```

**`GrpcHandlersModule.kt`**
```kotlin
package com.example.armeria.dagger

import com.example.armeria.services.HelloWorldServiceAdapter
import com.example.armeria.services.WhateverServiceAdapter
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.grpc.BindableService
import javax.annotation.Generated

@Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcSymbolProcessor")
@Module(includes = [GrpcCallScopeGraphModule::class])
object GrpcHandlersModule {
  @Provides @IntoSet
  fun helloWorldHandler(factory: GrpcCallScopeGraph.Factory): BindableService =
    HelloWorldServiceAdapter { factory.create().helloWorld() }

  @Provides @IntoSet
  fun whateverHandler(factory: GrpcCallScopeGraph.Factory): BindableService =
    WhateverServiceAdapter { factory.create().whatever() }
}
```

**User-written `ApplicationGraph.kt` (after T05/T06 — no `ApplicationGraphModule`)**
```kotlin
package com.example.armeria.dagger

import com.geekinasuit.daggergrpc.api.ApplicationScope
import com.example.armeria.ExampleServer
import dagger.Component

@Component(modules = [GrpcHandlersModule::class])
@ApplicationScope
interface ApplicationGraph {
  fun server(): ExampleServer

  @Component.Builder
  interface Builder {
    fun build(): ApplicationGraph
  }

  companion object {
    fun builder(): Builder = DaggerApplicationGraph.builder()
  }
}
```

---

### Java (APT output)

**`GrpcCallScopeGraph.java`**
```java
package com.example.armeria.dagger;

import com.example.armeria.services.HelloWorldService;
import com.example.armeria.services.WhateverService;
import com.geekinasuit.daggergrpc.api.GrpcCallScope;
import dagger.Subcomponent;
import javax.annotation.processing.Generated;

@Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcAPTProcessor")
@Subcomponent(modules = {GrpcCallScopeGraphModule.class})
@GrpcCallScope
public interface GrpcCallScopeGraph {
  HelloWorldService helloWorld();
  WhateverService whatever();

  @Subcomponent.Factory
  interface Factory {
    GrpcCallScopeGraph create();
  }
}
```

**`GrpcCallScopeGraphModule.java`**
```java
package com.example.armeria.dagger;

import com.geekinasuit.daggergrpc.api.GrpcCallContext;
import dagger.Module;
import javax.annotation.processing.Generated;

@Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcAPTProcessor")
@Module(
  includes = {GrpcCallContext.Module.class},
  subcomponents = {GrpcCallScopeGraph.class}
)
public interface GrpcCallScopeGraphModule {}
```

**`GrpcHandlersModule.java`**
```java
package com.example.armeria.dagger;

import com.example.armeria.services.HelloWorldServiceAdapter;
import com.example.armeria.services.WhateverServiceAdapter;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import javax.annotation.processing.Generated;

@Generated("com.geekinasuit.daggergrpc.iogrpc.compile.DaggerGrpcAPTProcessor")
@Module(includes = {GrpcCallScopeGraphModule.class})
abstract class GrpcHandlersModule {
  @Provides @IntoSet
  public static BindableService helloWorldHandler(GrpcCallScopeGraph.Factory factory) {
    return new HelloWorldServiceAdapter(() -> factory.create().helloWorld());
  }

  @Provides @IntoSet
  public static BindableService whateverHandler(GrpcCallScopeGraph.Factory factory) {
    return new WhateverServiceAdapter(() -> factory.create().whatever());
  }
}
```

**User-written `ApplicationGraph.java` (after T05/T06 — no `ApplicationGraphModule`)**
```java
package com.example.armeria.dagger;

import com.geekinasuit.daggergrpc.api.ApplicationScope;
import com.example.armeria.ExampleServer;
import dagger.Component;

@Component(modules = {GrpcHandlersModule.class})
@ApplicationScope
public interface ApplicationGraph {
  ExampleServer server();

  @Component.Builder
  interface Builder {
    ApplicationGraph build();
  }

  static ApplicationGraph create() {
    return DaggerApplicationGraph.create();
  }
}
```

---

## Acceptance Criteria

- [x] ADR document written (this file)
- [x] All five questions answered and confirmed by owner
- [x] Expected generated output for a two-handler example written as golden code snippets (Kotlin + Java)
- [x] T05 and T06 can proceed with a clear spec

## Implementation Notes

- The existing hand-written `GrpcCallScopeGraph`, `GrpcHandlersModule`, `GrpcCallScopeGraphModule`, and `ApplicationGraphModule` in both examples will be deleted as part of T05/T06 and replaced by actually-generated output.
- The `@Generated("to be generated")` marker on those files was placed intentionally as a breadcrumb.
- `GrpcCallScopeGraphModule` must declare `subcomponents = [GrpcCallScopeGraph::class]` (Kotlin) / `subcomponents = {GrpcCallScopeGraph.class}` (Java) — this is what makes `GrpcCallScopeGraph.Factory` injectable without any user-written binding module.
- Dagger version floor for `@Subcomponent.Factory`: 2.25+ (released 2019).
