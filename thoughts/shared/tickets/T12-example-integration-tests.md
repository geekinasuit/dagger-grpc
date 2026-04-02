---
id: T12
title: Write example integration tests (start Armeria server, send real gRPC requests)
priority: medium
phase: testing
status: open
blocked_by: T00
---

## Summary

Both example `ServiceTest` classes are empty placeholders. Write integration tests that start the actual Armeria gRPC server and send real gRPC requests, asserting on response content and header propagation through `GrpcCallContext`.

## Affected Files

- `examples/io_grpc/bazel_build_java/service/armeria/src/test/java/…/ServiceTest.java`
- `examples/io_grpc/bazel_build_kt/service/armeria/src/test/kotlin/…/ServiceTest.kt`

## Acceptance Criteria

**Both examples:**

- [ ] Test spins up an in-process Armeria server on a random port (use `Server.builder()...build()` and `.start().join()`)
- [ ] `SayHello` RPC: client sends a request, asserts the response echoes the `helloText` with the `"Hello: "` prefix
- [ ] `SayGoodbye` RPC: client sends a request, asserts the response echoes `goodbyeText` with `"Goodbye: "` prefix
- [ ] `Whatever` RPC: client sends a request with `whatever = true`, asserts `succeeded = true` in the response
- [ ] Header propagation: client sends a custom metadata header; service reads it via injected `GrpcCallContext`; response reflects the header value (requires a small service modification or a dedicated test endpoint)
- [ ] Server shuts down cleanly after each test (`server.stop().join()`)
- [ ] Test uses `@After`/`afterEach` to ensure server stops even on failure

**Java example** (`ServiceTest.java`):
- [ ] Uses Java gRPC blocking stub for simplicity
- [ ] Uses JUnit 4 (already a dep: `junit:junit:4.13.2`)

**Kotlin example** (`ServiceTest.kt`):
- [ ] Uses Kotlin (coroutine) stub or Java blocking stub — either acceptable
- [ ] Uses JUnit 4 (consistent with existing setup)

## Implementation Notes

- The `ApplicationGraph` + `ExampleServer` classes are already wired end-to-end in both examples. The test can call `ApplicationGraph.builder().build().server().setup()` to get the configured server, then `.start().join()`.
- Alternatively, build just the server in the test without going through `ApplicationGraph`, for faster test isolation.
- For header propagation testing: the `HelloWorldService` already reads `context.headers` and includes them in the response string. Send a custom metadata key/value pair and assert it appears in the response.
- Use an ephemeral port: `ServerBuilder.of(0)` in Armeria picks a random available port; retrieve it with `server.activeLocalPort()`.
- gRPC client setup: `ManagedChannelBuilder.forAddress("127.0.0.1", port).usePlaintext().build()` + the generated blocking stub.
