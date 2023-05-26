#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

function run_test() {
    local test_build_type="$1"

    echo "#########################"
    echo "# Test: $test_build_type"
    echo "#########################"

    export BUILD_TYPE="$test_build_type"
    ./test.sh > /dev/null

    if [[ "$?" -eq 0 ]]
    then
        echo "Test successful"
    else
        echo "Test failed"
    fi
}

run_test "build"
run_test "copy"
run_test "download"