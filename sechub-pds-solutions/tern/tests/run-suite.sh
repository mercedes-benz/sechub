#!/usr/bin/bash
# SPDX-License-Identifier: MIT

source test-tern.sh

# start tern docker
# wait for pds to be alive

# for image in images
#   build image
#   package image -> pds cli tool
#   upload to tern
#   compare results -> remember result
#
# stop tern docker

declare -A test_cases
test_cases[minimal]="minimal/Minimal.dockerfile" 
test_cases[alpine]="alpine/Alpine.dockerfile" 
test_cases[debian]="debian/Debian.dockerfile" 
test_cases[fedora]="fedora/Fedora.dockerfile"
test_cases[opensuse]="opensuse/OpenSUSE-Leap.dockerfile"
test_cases[rockylinux]="rockylinux/RockyLinux.dockerfile"

start_tern_docker
setup

if [[ $( wait_for_tern_to_be_alive ) == "yes" ]]
then
    log_step "Waiting successful. PDS Tern is alive"


    for test_case in "${!test_cases[@]}"
    do
        create_build_folder
        run_test "sechub-test-$test_case" "${test_cases[$test_case]}"
        # Intentionally commented out. Otherwhise the reports are deleted as well
        #remove_build_folder 
    done
else
    log_error "Unable to connect to PDS Tern."
fi

stop_tern_docker
tear_down