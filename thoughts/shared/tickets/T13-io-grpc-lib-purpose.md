---
id: T13
title: Define io_grpc/lib/ purpose and implement or remove
priority: low
phase: runtime
status: open
---

## Summary

`io_grpc/lib/` contains a single placeholder file `foo/foo.kt` with no meaningful content and no declared deps in its `BUILD.bazel`. Its intended purpose is unclear. This ticket is to make a decision and act on it.

## Current State

```
io_grpc/lib/
  BUILD.bazel        (declares kt_jvm_library with no deps, no sources globbed)
  src/main/kotlin/
    foo/foo.kt       (placeholder)
```

## Options

### Option A: Runtime helper library
Populate `io_grpc/lib/` with higher-level server-wiring helpers that build on `api/` and `util/armeria/` — e.g., a `DaggerGrpcServer` builder that assembles the Armeria server from a `Set<BindableService>` and automatically registers `GrpcCallContext.Interceptor`. This would reduce boilerplate in `ExampleServer`.

### Option B: Fold into `api/`
If the intended content is just additional runtime types close to the core API, move them into `api/` and delete `lib/`.

### Option C: Fold into `util/armeria/`
If the intended content is Armeria-specific, move it into `util/armeria/` and delete `lib/`.

### Option D: Delete the stub
If there was no concrete plan for `lib/`, remove it to reduce confusion.

## Acceptance Criteria

- [ ] A decision is made among the options above
- [ ] `io_grpc/lib/` either has real content with real deps and a clear purpose, or is deleted
- [ ] `bazel build //...` continues to pass

## Implementation Notes

- This is low priority and purely structural — no user-visible behavior change.
- Resolving this before T05/T06 is not required, but it would be good to know if module generation output should live here vs `api/`.
