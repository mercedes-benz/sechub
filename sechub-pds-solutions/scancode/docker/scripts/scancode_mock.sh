#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

source "${HELPER_FOLDER}/message.sh"

given_output_format="$(echo "$SCANCODE_OUTPUT_FORMAT" | tr '[:upper:]' '[:lower:]')"
mock_file="$MOCK_FOLDER/scancode_mock.spdx.json"

case "$given_output_format" in
    json)
        mock_file="$MOCK_FOLDER/scancode_mock.json"
    ;;
    json-pp)
        mock_file="$MOCK_FOLDER/scancode_mock.pretty_printed.json"
    ;;
    spdx-tv)
        mock_file="$MOCK_FOLDER/scancode_mock.spdx"
    ;;
    spdx-rdf)
        mock_file="$MOCK_FOLDER/scancode_mock.spdx.rdf"
esac

# Mock is scan output of the `samples` folder in: https://github.com/nexB/scancode-toolkit
infoMessage "Running Scancode mock"

echo "Parameters:"
echo "- scancode.timeout: $SCANCODE_TIMEOUT"
echo "- scancode.license.score: $SCANCODE_LICENSE_SCORE"
echo "- scancode.processes: $SCANCODE_PROCESSES"
echo "- extractcode.enabled: $EXTRACTCODE_ENABLED"
echo "- scancode.output.format: $SCANCODE_OUTPUT_FORMAT"
echo "- scancode.scan.copyright: $SCANCODE_SCAN_COPYRIGHT"
echo "- scancode.scan.license: $SCANCODE_SCAN_LICENSE"
echo "- scancode.scan.package: $SCANCODE_SCAN_PACKAGE"
echo "- scancode.scan.email: $SCANCODE_SCAN_EMAIL"
echo "- scancode.scan.url: $SCANCODE_SCAN_URL"
echo "- scancode.scan.info: $SCANCODE_SCAN_INFO"
echo "- scancode.scan.diagnostics: $SCANCODE_SCAN_DIAGNOSTICS"
echo "Given output format: $given_output_format"
echo "Mock file: $mock_file"
cp "$mock_file" "$PDS_JOB_RESULT_FILE"

if [[ "$SCANCODE_SCAN_DIAGNOSTICS" == "true" ]]
then
    cat "$MOCK_FOLDER/scancode_mock.json" > "${PDS_JOB_USER_MESSAGES_FOLDER}/INFO_message_$(date +%Y-%m-%d_%H.%M.%S_%N).txt"
fi
