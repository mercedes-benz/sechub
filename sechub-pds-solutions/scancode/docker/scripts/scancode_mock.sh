#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

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
echo "Running Scancode mock"
echo "Parameters:"
echo "- scancode.timeout: $SCANCODE_TIMEOUT"
echo "- scancode.license.score: $SCANCODE_LICENSE_SCORE"
echo "- scancode.processes: $SCANCODE_PROCESSES"
echo "- extractcode.enabled: $EXTRACTCODE_ENABLED"
echo "- scancode.output.format: $SCANCODE_OUTPUT_FORMAT"
echo ""
echo "Given output format: $given_output_format"
echo "Mock file: $mock_file"
cp "$mock_file" "$PDS_JOB_RESULT_FILE"