#!/bin/bash
# SPDX-License-Identifier: MIT

readonly BUILD_FOLDER="build"

function generate_empty_file() {
    touch "$BUILD_FOLDER/empty"
}

function generate_binary_file_no_license() {
    local binary_no_license="$BUILD_FOLDER/binary_no_license.bin"

    echo -n $'\x44\x40\x09\x07\x7f\x71\x01' > "$binary_no_license"
}

function generate_binary_with_mit_license() {
    local binary_file="$BUILD_FOLDER/binary_with_mit_license.bin"

    # write `NUL` into the file
    dd if=/dev/zero bs=1 count=10 > "$binary_file"

    for i in {1..400}
    do
        echo $'\x09\x07\x7f\x20\x40\x61\x69\x7C' >> "$binary_file"
        if test $i -eq 200
        then
            cat "MIT.txt" >> "$binary_file"
        fi
    done
}

mkdir $BUILD_FOLDER

generate_empty_file
generate_binary_file_no_license
generate_binary_with_mit_license