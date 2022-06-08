#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

# IMPORTANT: Keep the space in front and back of the list
output_formats=" json json-pp spdx-tv spdx-rdf spdx-json "
output_format="--spdx-tv"
convert_output_to_spdx_json=true
license_score="0"
scancode_processes="1"

# make sure additional options is never empty
# otherwise scancode will have problems parsing the
# parameters and fail
additional_options=""

echo "-------------"
echo "SecHub Config"
echo "-------------"

echo "SecHub Job UUID: $SECHUB_JOB_UUID"

echo "-------"
echo " Setup "
echo "-------"

if [[ "$SCANCODE_PROCESSES" -ge 1 ]]
then
    scancode_processes="$SCANCODE_PROCESSES"
    additional_options="$additional_options --processes $scancode_processes"
fi

if [[ -n "$SCANCODE_LICENSE_SCORE" ]]
then
    if [[ "$SCANCODE_LICENSE_SCORE" -le 100 && "$SCANCODE_LICENSE_SCORE" -ge 0 ]]
    then
        echo "between 0 and 100"
        license_score="$SCANCODE_LICENSE_SCORE"
        additional_options="$additional_options --license-score $license_score"
    fi
fi

given_output_format="$(echo "$SCANCODE_OUTPUT_FORMAT" | tr '[:upper:]' '[:lower:]')"

echo "Given format: $given_output_format"

# is given output format in output formats?
if [[ $output_formats =~ " $given_output_format " ]]
then
    echo "Given format matches."

    if [[ "$given_output_format" == "spdx-json" ]]
    then
        echo "Format: --spdx-json"
    else
        convert_output_to_spdx_json=false
        output_format="--$given_output_format"
        echo "Format: $output_format"
    fi 
fi

extracted_folder=""
if $PDS_JOB_HAS_EXTRACTED_SOURCES
then
    echo "Extracted sources"
    extracted_folder="$PDS_JOB_EXTRACTED_SOURCES_FOLDER"
elif $PDS_JOB_HAS_EXTRACTED_BINARIES
then
    echo "Extracted binaries"
    extracted_folder="$PDS_JOB_EXTRACTED_BINARIES_FOLDER"
fi

echo "Extracted folder structure:"
echo ""
tree "$extracted_folder"

echo ""
echo "-------------"
echo "Starting scan"
echo "-------------"
echo ""

echo "Scancode processes: $scancode_processes"
echo "Minimum license score: $license_score"
echo "Additional options: $additional_options"
echo "Output format: $output_format"

spdx_file="$PDS_JOB_RESULT_FILE.spdx"

local extractcode_enabled=$( echo "$EXTRACTCODE_ENABLED" | tr '[:upper:]' '[:lower:]' )
if [[ "$extractcode_enabled" == "true" ]]
then
  echo "Running extractcode"
  "$TOOL_FOLDER/scancode-toolkit-$SCANCODE_VERSION/extractcode" $extracted_folder
fi

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
        java -jar "$TOOL_FOLDER/tools-java-$SPDX_TOOL_VERISON-jar-with-dependencies.jar" Convert "$spdx_file" "$spdx_json_file"
        mv "$spdx_json_file" "$PDS_JOB_RESULT_FILE"
    else
        echo "Moving file"
        mv "$PDS_JOB_RESULT_FILE.spdx" "$PDS_JOB_RESULT_FILE"
    fi
else
    echo "No findings"
fi