#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

# IMPORTANT: Keep the space in front and back of the list
output_formats=" json json-pp spdx-tv spdx-rdf spdx-json "
output_format="--spdx-tv"
convert_output_to_spdx_json=true

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

spdx_file="$PDS_JOB_RESULT_FILE.spdx"

"$TOOL_FOLDER/scancode-toolkit-$SCANCODE_VERSION/scancode" --verbose --copyright --license --package --email --url --info --processes 2 "$output_format" "$spdx_file" "$PDS_JOB_SOURCECODE_UNZIPPED_FOLDER/"

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