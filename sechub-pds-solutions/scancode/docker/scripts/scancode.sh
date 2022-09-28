#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

# IMPORTANT: Keep the space in front and back of the list
output_formats=" json json-pp spdx-tv spdx-rdf spdx-json "
output_format="--spdx-tv"
convert_output_to_spdx_json=true
license_score="0"
scancode_processes="1"
file_scan_timeout="120"

# make sure additional options is never empty
# otherwise scancode will have problems parsing the
# parameters and fail
additional_options=""

echo ""
echo "------"
echo "System"
echo "------"
echo ""

printf "%-26s %s\n" "PDS version:" "$PDS_VERSION"
printf "%-26s %s\n" "Scancode-Toolkit version:" "$SCANCODE_VERSION"

echo ""
echo "---------"
echo "PDS Setup"
echo "---------"
echo ""

echo "SecHub Job UUID: $SECHUB_JOB_UUID"
echo "PDS Job UUID: $PDS_JOB_UUID"
echo ""

extracted_folder=""
if [[ "$PDS_JOB_HAS_EXTRACTED_SOURCES" == "true" ]]
then
    echo "Extracted sources"
    extracted_folder="$PDS_JOB_EXTRACTED_SOURCES_FOLDER"
elif [[ "$PDS_JOB_HAS_EXTRACTED_BINARIES" == "true" ]]
then
    echo "Extracted binaries"
    extracted_folder="$PDS_JOB_EXTRACTED_BINARIES_FOLDER"
else
    echo ""
    echo "ERROR: Unrecognized file type. Neither binary nor source."
    echo ""
    echo "Workspace location structure:"
    echo ""
    tree "$PDS_JOB_WORKSPACE_LOCATION"
    exit 1
fi

echo "Extracted folder structure:"
echo ""
tree "$extracted_folder"
echo ""

echo "Size of the extracted folder on disk" 
du --summarize --human-readable "$extracted_folder"

echo ""
echo "--------------"
echo "Scancode Setup"
echo "--------------"
echo ""

echo "User provided number of scancode processes: $SCANCODE_PROCESSES"
if [[ "$SCANCODE_PROCESSES" -ge 1 ]]
then
    scancode_processes="$SCANCODE_PROCESSES"
    additional_options="$additional_options --processes $scancode_processes"
fi

echo "User provided license score: $SCANCODE_LICENSE_SCORE"
if [[ -n "$SCANCODE_LICENSE_SCORE" ]]
then
    if [[ "$SCANCODE_LICENSE_SCORE" -le 100 && "$SCANCODE_LICENSE_SCORE" -ge 0 ]]
    then
        license_score="$SCANCODE_LICENSE_SCORE"
        additional_options="$additional_options --license-score $license_score"
    fi
fi

echo "User provided timeout: $SCANCODE_TIMEOUT"
if [[ -n "$SCANCODE_TIMEOUT" ]]
then
    if [[ "$SCANCODE_TIMEOUT" -ge 1 ]]
    then
        file_scan_timeout="$SCANCODE_TIMEOUT"
        additional_options="$additional_options --timeout $file_scan_timeout"
    fi
fi

given_output_format="$(echo "$SCANCODE_OUTPUT_FORMAT" | tr '[:upper:]' '[:lower:]')"

echo "Possible output formats: $output_formats"
echo "User provided output format: $given_output_format"

# is given output format in output formats?
if [[ $output_formats =~ " $given_output_format " ]]
then
    echo "User provided output format is in possible output formats."

    if [[ "$given_output_format" != "spdx-json" ]]
    then
        convert_output_to_spdx_json=false
        output_format="--$given_output_format"
    fi 
fi

echo ""
echo "-------------"
echo "Starting scan"
echo "-------------"
echo ""

echo "Scancode processes: $scancode_processes"
echo "Minimum license score: $license_score"
echo "Timeout: $file_scan_timeout"
echo "Additional options: $additional_options"
echo "Output format: $output_format"
echo ""

spdx_file="$PDS_JOB_RESULT_FILE.spdx"

extractcode_enabled=$( echo "$EXTRACTCODE_ENABLED" | tr '[:upper:]' '[:lower:]' )
if [[ "$extractcode_enabled" == "true" ]]
then
  echo "Running extractcode"
  # `2>&1` -> redirect the verbose output from standard error to standard out
  "$TOOL_FOLDER/scancode-toolkit-$SCANCODE_VERSION/extractcode" --verbose $extracted_folder 2>&1
fi

# `2>&1` -> redirect the verbose output from standard error to standard out
# TODO: 
# debug mode -> write --json-pp INFO_message_xyz.txt to get the output as message or parts of it?
# write the error file to ERROR_messages_xyz.txt to get more information about errors
"$TOOL_FOLDER/scancode-toolkit-$SCANCODE_VERSION/scancode" $additional_options --verbose --strip-root --copyright --license --package --email --url --info $output_format $spdx_file $extracted_folder 2>&1

echo ""
echo "----------------"
echo "Preparing result"
echo "----------------"
echo ""

if [[ -f "$spdx_file" ]]
then
    if $convert_output_to_spdx_json
    then
        echo "Converting to SPDX JSON"
        spdx_json_file="$PDS_JOB_RESULT_FILE.spdx.json"

        # use the SPDX tool converter to convert the SPDX tag-value to SPDX JSON
        time java -jar "$TOOL_FOLDER/tools-java-$SPDX_TOOL_VERISON-jar-with-dependencies.jar" Convert "$spdx_file" "$spdx_json_file"
        mv "$spdx_json_file" "$PDS_JOB_RESULT_FILE"
    else
        echo "Moving file"
        mv "$PDS_JOB_RESULT_FILE.spdx" "$PDS_JOB_RESULT_FILE"
    fi
else
    echo "No findings"
fi
