#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

projectDirOSspecific="$1"
projectDirLinuxPath="$2" #converted part , necessary for mingw on windows...
package="$3"
platforms="$4"

MOD_BASENAME="mercedes-benz.com/sechub"
SUBMODULES="cli testutil util"  # Space separated list
SRC_PATH="$projectDirLinuxPath/src/$MOD_BASENAME"

if [[ -z "$package" ]]; then
    echo "usage: $0 <projectDirOsSpecific> <projectDirLinuxPath> <package-name> [ <platforms> ]"
    exit 1
fi

if [[ -z "$platforms" ]]; then
    platforms=("linux/386" "linux/amd64" "linux/arm" "linux/arm64" "darwin/amd64" "darwin/arm64" "windows/amd64" "windows/386")
fi

function init_go_modules() {
    # We build the mod.go files ourselves - so it works with any go version installed
    pushd "$SRC_PATH" >/dev/null
    # remove previously generated go.mod files
    rm -f go.mod */go.mod

    echo "# Initialize go modules"
    cd main/
    go mod init $MOD_BASENAME
    for i in $SUBMODULES ; do
        pushd ../$i >/dev/null
        go mod init $MOD_BASENAME/$i
        popd >/dev/null
        go mod edit -replace $MOD_BASENAME/$i=../$i
    done
    go mod tidy  # Make the defined modules usable (changes go.mod file)

    echo "# Declare other submodules to satisfy dependencies when testing"
    for i in $SUBMODULES ; do
        pushd ../$i
        for j in $SUBMODULES ; do
            if [ $j != $i ] ; then
                go mod edit -replace $MOD_BASENAME/$j=../$j
            fi
        done
        go mod tidy
        popd >/dev/null
    done

    popd >/dev/null
}

package_split=(${package//\// })
package_name=${package_split[*]: -1}

echo "Build Go: Building package '$package'"

export GOPATH="$SRC_PATH" # ignore former one, prevents differt of ; and : of pathes...
echo "GOPATH=$GOPATH"

init_go_modules

cd "$SRC_PATH/main"

export CGO_ENABLED=0  # This forces statically linked binaries
GO_LD_FLAGS="-s -w"   # strip (reduce size): disable debug symbol table / disable DWARF generation

for platform in "${platforms[@]}" ; do
    platform_split=(${platform//\// })
    export GOOS=${platform_split[0]}
    export GOARCH=${platform_split[1]}

    # Create subfolder for platform
    targetSubFolder="platform/$GOOS-$GOARCH"
    buildDir="$projectDirLinuxPath/build/go/$targetSubFolder"
    mkdir -p "$buildDir"

    output_name="$package_name"
    if [ $GOOS = "windows" ]; then
        output_name+='.exe'
    fi

    echo "> building $targetSubFolder"
    go build -ldflags="$GO_LD_FLAGS" -o "$buildDir/$output_name" .
    if [ $? -ne 0 ]; then
        echo 'Go build failed because of an error'
        exit 1
    fi

    # create sha256 checksum and use only first part (checksum)
    checksumHash=($(sha256sum "$buildDir/$output_name"))
    echo "$checksumHash" > "$buildDir/$output_name.sha256"
done
