---
id: T07
title: Extend ksp-apt-bridge to support module generation
priority: medium
phase: codegen
status: open
blocked_by: T04
blocks: T05, T06
---

## Summary

The `ksp-apt-bridge` currently implements only the subset of KSP interface members needed for adapter generation. Module generation (T05/T06) will likely require additional APT bridge members to be implemented — specifically anything needed to inspect handler class annotations, supertype information, or constructor details that are not currently used.

This ticket is to identify and implement those additional bridge members, driven by the requirements of T05/T06.

## Currently Unimplemented (TODO) Members

Key gaps in the bridge (all `TODO("Not yet implemented")`):

- `APTClassDeclaration`: `superTypes`, `primaryConstructor`, `containingFile`, `isCompanionObject`, `findActuals`, `findExpects`, `getSealedSubclasses`
- `APTFunctionDeclaration`: return type, modifiers, type parameters, annotations, body
- `APTPropertyDeclaration`: all members
- `APTType`: `isAssignableFrom`, `makeNullable`, `replace`, `starProjection`, `isMarkedNullable`, `isError`, `isFunctionType`
- `APTTypeArgument`: all members
- `APTAnnotation`: `defaultArguments`, `location`, `origin`, `parent`, `useSiteTarget`

## Acceptance Criteria

- [ ] All KSP bridge members needed by module generation (T05/T06) are implemented
- [ ] Members are implemented only as needed (do not implement speculatively)
- [ ] Existing adapter generation tests continue to pass
- [ ] New bridge members have unit tests in `ksp-apt-bridge/src/test/` (builds on T11)

## Implementation Notes

- This ticket is driven by T05/T06 — implement bridge members on demand as the module generation code is written and run.
- Most likely needed: `APTClassDeclaration.containingFile` (for dependency tracking in generated file `Dependencies`), and reading scope annotations (e.g. checking `@GrpcCallScope` is present on the handler class).
- The bridge README notes it is not intended for general use, only for dagger-grpc's needs — keep the implementation scope minimal.
