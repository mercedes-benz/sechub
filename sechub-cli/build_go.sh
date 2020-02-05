#!/usr/bin/env bash

# SPDX-License-Identifier: MIT

projectDirOSspecific=$1
projectDirLinuxPath=$2 #converted part , necessary for mingw on windows...
package=$3
platforms=$4
globalBuildDir=$5

# echo ">>projectDirOsSpecific=$1"

if [[ -z "$package" ]]; then
    echo "usage: $0 <projectDirOsSpecific> <projectDirLinuxPath> <package-name> [ <platforms> <globalBuildDir>]"
    exit 1
fi

if [[ -z "$platforms" ]]; then
    platforms=("linux/386" "windows/amd64" "windows/386" )
fi


package_split=(${package//\// })
package_name=${package_split[-1]}

echo "Build go:Start building package '$package'"

export GOPATH="$projectDirOSspecific" # ignore former one, prevents differt of ; and : of pathes...

# echo ">>GOPATH=$GOPATH"

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
    env GOOS=$GOOS GOARCH=$GOARCH go build -ldflags="-s -w" -o $output_name $package
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