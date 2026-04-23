# Ticket Overview — dagger-grpc

Source: `thoughts/shared/research/2026-03-27-codebase-overview.md`

## Priority / Dependency Order

```
T00  Verify HEAD builds & tests          HIGH    ✅ DONE
  └─ T01  Kotlin example Bzlmod hygiene  LOW     (builds fine; stale names only)
  └─ T08  Unblock KSP tests              HIGH    (prerequisite for KSP test work)
  └─ T02  Pin Kotlin toolchain           MEDIUM
  └─ T03  Proto version skew             LOW     ✅ DONE

T04  Design module generation shape      HIGH    ✅ DONE
  └─ T07  Extend ksp-apt-bridge          MEDIUM  (prerequisite for T05/T06)
       └─ T05  KSP module generation     HIGH    (core feature)
       └─ T06  APT module generation     HIGH    (core feature)
            └─ T14  Remove boilerplate   MEDIUM  (cleanup after T05+T06)

T09  Expand APT processor tests          MEDIUM  (can start after T00)
T10  Expand KSP processor tests          MEDIUM  (after T08)
T11  common/ + ksp-apt-bridge tests      MEDIUM  (after T00)
T12  Example integration tests           MEDIUM  (after T00)

T13  Define io_grpc/lib/ purpose         LOW
T15  User-facing integration guide       LOW     (after T05+T06)

T16  Publish to Bazel Central Registry   MEDIUM  (after stable release tag)
T17  Publish Maven artifacts             MEDIUM  (after stable release tag; coordinate with T16)
T18  Gradle examples + Maven artifact    MEDIUM  (after T05+T06; Part D coordinates with T17 for Central; see also T15)
```

## Summary Table

| ID  | Title                                         | Priority | Phase        | Status |
|-----|-----------------------------------------------|----------|--------------|--------|
| T00 | Verify HEAD builds and tests pass             | HIGH     | infra        | done   |
| T01 | Update Kotlin example to canonical Bzlmod names (hygiene) | LOW | infra | open   |
| T02 | Pin Kotlin toolchain version                  | MEDIUM   | infra        | open   |
| T03 | Resolve proto version skew                    | LOW      | infra        | done   |
| T04 | Design generated module shape (ADR)           | HIGH     | codegen      | done   |
| T05 | Implement KSP module generation               | HIGH     | codegen      | open   |
| T06 | Implement APT module generation               | HIGH     | codegen      | open   |
| T07 | Extend ksp-apt-bridge for module generation   | MEDIUM   | codegen      | open   |
| T08 | Unblock KSP processor test (Kotlin 2)         | HIGH     | testing      | open   |
| T09 | Expand APT processor tests                    | MEDIUM   | testing      | open   |
| T10 | Expand KSP processor tests                    | MEDIUM   | testing      | open   |
| T11 | Write tests for common/ and ksp-apt-bridge    | MEDIUM   | testing      | open   |
| T12 | Write example integration tests               | MEDIUM   | testing      | open   |
| T13 | Define io_grpc/lib/ purpose                   | LOW      | runtime      | open   |
| T14 | Remove hand-written boilerplate from examples | MEDIUM   | cleanup      | open   |
| T15 | Write user-facing integration guide           | LOW      | docs         | open   |
| T16 | Publish to Bazel Central Registry             | MEDIUM   | distribution | open   |
| T17 | Publish Maven-compatible artifacts            | MEDIUM   | distribution | open   |
| T18 | Gradle examples and Maven artifact pipeline   | MEDIUM   | distribution | open   |
