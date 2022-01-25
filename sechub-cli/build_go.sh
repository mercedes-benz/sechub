#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

projectDirOSspecific="$1"
projectDirLinuxPath="$2" #converted part , necessary for mingw on windows...
package="$3"
platforms="$4"
globalBuildDir="$5"

MOD_BASENAME="daimler.com/sechub"
SUBMODULES="cli testutil util"  # Space separated list
SRC_PATH="$projectDirLinuxPath/src/$MOD_BASENAME"

if [[ -z "$package" ]]; then
    echo "usage: $0 <projectDirOsSpecific> <projectDirLinuxPath> <package-name> [ <platforms> <globalBuildDir>]"
    exit 1
fi

if [[ -z "$platforms" ]]; then
    platforms=("linux/386" "linux/amd64" "linux/arm" "linux/arm64" "darwin/amd64" "windows/amd64" "windows/386")
fi

function init_go_modules() {
    # We build the mod.go files ourselves - so it works with any go version installed
    pushd "$SRC_PATH"
    # remove previously generated go.mod files
    rm -f go.mod */go.mod

    echo "# Initialize go modules"
    cd main/
    go mod init $MOD_BASENAME
    for i in $SUBMODULES ; do
        pushd ../$i
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
package_name=${package_split[-1]}

echo "Build Go: Building package '$package':"

export GOPATH="$SRC_PATH" # ignore former one, prevents differt of ; and : of pathes...

echo ">>GOPATH=$GOPATH"

init_go_modules

cd $SRC_PATH/main
for platform in "${platforms[@]}"
do
    platform_split=(${platform//\// })
    GOOS=${platform_split[0]}
    GOARCH=${platform_split[1]}
    output_name=$package_name'-'$GOOS'-'$GOARCH
    targetSubFolder='platform/'$GOOS'-'$GOARCH
    if [ $GOOS = "windows" ]; then
        output_name+='.exe'
    fi
    echo ">building:$targetSubFolder"
    env GOOS=$GOOS GOARCH=$GOARCH go build -ldflags="-s -w" -o $output_name .
    if [ $? -ne 0 ]; then
        echo 'Go build failed because of an error'
        exit 1
    fi

    if [[ -z "$globalBuildDir" ]]; then
        buildDir="$projectDirLinuxPath/build/go/$targetSubFolder"
    else
        buildDir="$globalBuildDir"
    fi
    finalOutputName=$package_name
    if [ $GOOS = "windows" ]; then
        finalOutputName+='.exe'
    fi
    mkdir -p "$buildDir"
    mv "$output_name" "$buildDir/$finalOutputName"
    # create sha25 checksum and use only first part (checksum)
    checksumHash=($(sha256sum "$buildDir/$finalOutputName"))
    echo "$checksumHash" > "$buildDir/$finalOutputName.sha256"
done