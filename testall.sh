#!/usr/bin/env bash

bazel test //...

examples=$(find examples -name MODULE.bazel -exec dirname {} \;)
dir=$(pwd)
for ex in $examples ; do
  echo "Executing tests in $ex"
  cd "$dir/$ex"
  bazel test //...
done