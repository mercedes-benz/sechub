#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

function run_test() {
    local test_build_type="$1"

    local space_line="#########################"

    echo "$space_line"
    echo "# Test: $test_build_type"
    echo "$space_line"

    export BUILD_TYPE="build"
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