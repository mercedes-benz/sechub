#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

exit_code=0

# start container
nohup ./01-start-single-docker-compose.sh &

# Setup environment
export PDS_SERVER="https://localhost:8444"
export PDS_USERID="admin"
export PDS_APITOKEN="pds-apitoken"
export PDS_PRODUCT_IDENTFIER="PDS"
export RESULT_FILE="result.txt"

reference_file="docker/mocks/mock.sarif.json"

pds_api="../../sechub-developertools/scripts/pds-api.sh"

is_running="false"
retries=500

while [[ "$retries" -ge 0 && "$is_running" == "false" ]]
do
    echo "Waiting for $PDS_SERVER"
    check_alive=`$pds_api check_alive`

    if [[ -n "$check_alive" ]]
    then
        is_running="true"
    fi

    ((retries--))
    sleep 0.5s
done

if [[ "$is_running" == "false" && "$retries" -eq 0 ]]
then
    echo "Could not reach $PDS_SERVER"
    exit 3
fi

# Create ZIP
# Setup environment
test_file="test.txt"
test_zip="test.zip"
touch "$test_file"
zip "$test_zip" "$test_file"

# Start scan
"../shared/01-test.sh" "$test_zip"

# Compare result
diff --ignore-all-space "$reference_file" "$RESULT_FILE"

if [[ "$?" -ne 0 ]]
then
    echo "Error: files not equal"
    exit_code=1
fi

echo "Stoping PDS"
docker stop pds

echo "Cleaning up"
rm "$test_file" "$test_zip" "nohup.out" "$RESULT_FILE"

exit "$exit_code"