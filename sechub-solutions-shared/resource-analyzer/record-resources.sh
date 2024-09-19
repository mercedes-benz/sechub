#!/bin/bash
# SPDX-License-Identifier: MIT

result_file=""

function usage() {  
    cat - <<EOF
Usage: basename $0 <container-id> <result-file>
EOF
}

function print_error_message() {
    local error_message="$1"

    printf "\nERROR: $error_message\n"
}

function quit() {
    echo "Do you want to quit? (y/n)"
    read answer
    if [[ "$answer" == "y" ]]
    then
        echo "Stopped recording."
        exit 0
    fi
}

trap quit SIGINT SIGTERM

container_id="$1"
result_file="$2"

if [[ -z "$container_id" ]]
then
    print_error_message "Container Id is missing."
    usage
    exit 1
fi

if [[ -z "$result_file" ]]
then
    print_error_message "The path to the result file is missing."
    usage
    exit 1
fi

touch "$result_file"

# write csv header
echo "CPUPercent,MemoryUsageInBytes" > "$result_file"

# necessary for numfmt
export LC_ALL=en_US.utf8

# the `stats` command is available on Podman as well
# see: https://docs.podman.io/en/latest/markdown/podman-stats.1.html#example
while true
do
    # get the stats
    json_row=$(docker stats --no-stream --format="{{json .}}")

    # get the CPU percentage
    cpu_percent=$(echo $json_row | jq --raw-output '.CPUPerc' |  cut -d '%' -f1)

    # get the memory usage
    memory_usage_in_bytes=$(echo $json_row | jq --raw-output '.MemUsage' | cut -d '/' -f1 | tr -d "B" | numfmt --from=auto)

    # write csv row
    echo "$cpu_percent,$memory_usage_in_bytes" >> "$result_file"

    echo "Press CTRL+C to stop the recordig"
    sleep 0.2s
done