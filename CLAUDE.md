# Claude Code Instructions

Read `AGENTS.md` in full before beginning any work. All norms, conventions, architecture descriptions, and workflow rules in that file apply to this session.

## Preparatory Context

After reading `AGENTS.md`, load the following into context:

1. **Ticket index** — read `thoughts/shared/tickets/overview.md` to understand open and resolved work before starting any non-trivial task.
2. **Research documents** — for any task touching a relevant subsystem, read the applicable documents in `thoughts/shared/research/`. Prefer `.compressed.md` forms when present; fall back to `.md` only when the compressed form is absent or when producing human-readable output.
3. **Active plans** — read any relevant files in `thoughts/shared/plans/` before implementing non-trivial features.

## Key Rules (Summary — See AGENTS.md for Full Detail)

- Use `jj` (not `git`) for all VCS operations when `.jj/` is present.
- Use `bin/bazel` to invoke Bazel (never source `activate-hermit` manually).
- Never push directly to `main`; all changes go through a branch/bookmark and a PR.
- Always include `MODULE.bazel.lock` when committing `MODULE.bazel` changes.
- Keep `thoughts/shared/tickets/overview.md` in sync with any ticket file changes.
