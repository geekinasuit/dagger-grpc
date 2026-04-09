#!/usr/bin/env bash
# scripts/release.sh — Cut a dagger-grpc release.
#
# Usage:
#   scripts/release.sh <version>            # e.g. 0.1.0
#   scripts/release.sh --dry-run <version>  # validate only, no side effects
#
# What this script does:
#   1. Validates all release criteria (see §CRITERIA below)
#   2. Creates a GitHub release + tag via `gh release create`
#   3. The publish.yaml workflow fires automatically on the release event,
#      opening a draft BCR PR at geekinasuit/bazel-central-registry
#
# §CRITERIA — all must pass before tagging:
#   - VERSION argument matches MODULE.bazel version field
#   - Working copy is clean (no uncommitted changes)
#   - HEAD commit is on main
#   - bin/bazel build //... passes
#   - bin/bazel test //... passes
#   - Both example workspaces build and test cleanly
#
# §AGENT RELEASE RULES — an agent may run this script only when:
#   - The user has explicitly provided the version to cut
#   - The user has granted permission to push tags and create a release
#   - All criteria above pass (script enforces this)
#
# Prerequisites (one-time human setup — see T16 plan):
#   - geekinasuit/bazel-central-registry fork exists
#   - BCR_PUBLISH_TOKEN secret set in repo settings
#   - Google CLA signed

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
BAZEL="${REPO_ROOT}/bin/bazel"

DRY_RUN=false
VERSION=""

# Parse arguments
for arg in "$@"; do
  case "${arg}" in
    --dry-run) DRY_RUN=true ;;
    --*)       echo "Unknown flag: ${arg}" >&2; exit 1 ;;
    *)
      if [[ -z "${VERSION}" ]]; then
        VERSION="${arg}"
      else
        echo "Unexpected argument: ${arg}" >&2; exit 1
      fi
      ;;
  esac
done

if [[ -z "${VERSION}" ]]; then
  echo "Usage: $0 [--dry-run] <version>   (e.g. 0.1.0)" >&2
  exit 1
fi

TAG="v${VERSION}"
PASS="[PASS]"
FAIL="[FAIL]"
INFO="[INFO]"

echo "${INFO} Release criteria check for ${TAG}"
echo ""

ERRORS=0

check() {
  local desc="$1"
  local result="$2"   # "ok" or error message
  if [[ "${result}" == "ok" ]]; then
    echo "${PASS} ${desc}"
  else
    echo "${FAIL} ${desc}: ${result}"
    ERRORS=$((ERRORS + 1))
  fi
}

# ── Criterion 1: VERSION matches MODULE.bazel ────────────────────────────────
MODULE_VERSION="$(grep -m1 'version = ' "${REPO_ROOT}/MODULE.bazel" | sed 's/.*version = "\(.*\)".*/\1/')"
if [[ "${MODULE_VERSION}" == "${VERSION}" ]]; then
  check "MODULE.bazel version matches ${VERSION}" "ok"
else
  check "MODULE.bazel version matches ${VERSION}" \
    "MODULE.bazel says \"${MODULE_VERSION}\"; expected \"${VERSION}\""
fi

# ── Criterion 2: Clean working copy ─────────────────────────────────────────
JJ_STATUS="$(jj status 2>&1)"
if echo "${JJ_STATUS}" | grep -q "^Working copy changes:"; then
  check "Working copy is clean" "uncommitted changes present (run: jj status)"
else
  check "Working copy is clean" "ok"
fi

# ── Criterion 3: HEAD is on main ─────────────────────────────────────────────
HEAD_BRANCHES="$(jj log --no-graph -r @ --template 'bookmarks' 2>/dev/null)"
if echo "${HEAD_BRANCHES}" | grep -q "main"; then
  check "HEAD commit is on main" "ok"
else
  check "HEAD commit is on main" "current @ does not have main bookmark (${HEAD_BRANCHES:-none})"
fi

# ── Criterion 4: CI green (build + test) ─────────────────────────────────────
echo ""
echo "${INFO} Running bin/bazel build //... (this may take a moment)"
if "${BAZEL}" build //... > /tmp/release_build.log 2>&1; then
  check "bin/bazel build //..." "ok"
else
  check "bin/bazel build //..." "build failed — see /tmp/release_build.log"
fi

echo "${INFO} Running bin/bazel test //..."
if "${BAZEL}" test //... > /tmp/release_test.log 2>&1; then
  check "bin/bazel test //..." "ok"
else
  check "bin/bazel test //..." "tests failed — see /tmp/release_test.log"
fi

# ── Criterion 5: Example workspaces ──────────────────────────────────────────
for example in examples/io_grpc/bazel_build_java examples/io_grpc/bazel_build_kt; do
  echo "${INFO} Building ${example}..."
  if (cd "${REPO_ROOT}/${example}" && "${BAZEL}" build //... > /tmp/release_ex_build.log 2>&1); then
    check "${example}: build" "ok"
  else
    check "${example}: build" "failed — see /tmp/release_ex_build.log"
  fi
  if (cd "${REPO_ROOT}/${example}" && "${BAZEL}" test //... > /tmp/release_ex_test.log 2>&1); then
    check "${example}: test" "ok"
  else
    check "${example}: test" "failed — see /tmp/release_ex_test.log"
  fi
done

# ── Summary ───────────────────────────────────────────────────────────────────
echo ""
if [[ "${ERRORS}" -gt 0 ]]; then
  echo "❌  ${ERRORS} criterion/criteria failed. Fix above issues before releasing."
  exit 1
fi

echo "✅  All criteria passed for ${TAG}."
echo ""

if [[ "${DRY_RUN}" == "true" ]]; then
  echo "${INFO} --dry-run: stopping here. No tag or release created."
  exit 0
fi

# ── Create GitHub release (also creates the tag) ──────────────────────────────
echo "${INFO} Creating GitHub release ${TAG}..."
gh release create "${TAG}" \
  --repo geekinasuit/dagger-grpc \
  --title "dagger-grpc ${TAG}" \
  --generate-notes \
  --draft=false

echo ""
echo "✅  Release ${TAG} created."
echo "${INFO} The publish.yaml workflow will now open a draft BCR PR automatically."
echo "${INFO} Monitor: gh run list --repo geekinasuit/dagger-grpc --workflow publish.yaml"
echo "${INFO} BCR PR will appear at: https://github.com/geekinasuit/bazel-central-registry/pulls"
