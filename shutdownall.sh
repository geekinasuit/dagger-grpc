#!/usr/bin/env bash

bazel shutdown

examples=$(find examples -name MODULE.bazel -exec dirname {} \;)
dir=$(pwd)
for ex in $examples ; do
  echo "Shutting down bazel in $ex"
  cd "$dir/$ex"
  bazel shutdown
done