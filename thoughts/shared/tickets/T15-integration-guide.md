---
id: T15
title: Write user-facing integration guide
priority: low
phase: docs
status: open
blocked_by: T05, T06, T14
---

## Summary

Write a clear, step-by-step integration guide covering both the Java (APT) and Kotlin (KSP) paths. This should be the primary onboarding document for new users of `dagger-grpc`.

## Audience

JVM backend developers who use Dagger 2 and gRPC and want per-call scoping without boilerplate.

## Acceptance Criteria

- [ ] Guide covers the Java/Bazel path (APT processor, `java_library` + plugin)
- [ ] Guide covers the Kotlin/Bazel path (KSP processor, `kt_jvm_library` + plugin)
- [ ] Explains: how to add `dagger-grpc` to a Bazel `MODULE.bazel`
- [ ] Explains: how to annotate a service implementation class (`@GrpcServiceHandler`, `@GrpcCallScope`, `@Inject constructor`)
- [ ] Explains: what the processor generates (adapter + subcomponent + module) — user doesn't have to write it
- [ ] Explains: what the user still writes (top-level `ApplicationGraph` component, server main)
- [ ] Explains: how to inject `GrpcCallContext` for per-call metadata access
- [ ] Includes a minimal working example (proto → service impl → main)
- [ ] Covers: the call-scope pattern and why it exists (1–2 paragraphs)
- [ ] Points to the full examples in `examples/io_grpc/`

## Implementation Notes

- Write in `README.md` at the repo root (currently placeholder-level content), or as a structured `docs/` directory.
- Keep the "minimal example" short enough to be readable in 5 minutes.
- The examples (`bazel_build_java`, `bazel_build_kt`) serve as the extended reference; the guide should link to them.
- Mention the Armeria dependency as the current server integration target; note that other integrations may be added in the future.
