#!/usr/bin/bash
# SPDX-License-Identifier: MIT

function check_env_var_is_set {
  local param="$1"
  if [ -z "${!param}" ] ; then
    echo "Mandatory environment variable $param is not set!"
    failed=true
  fi
}

# Check if mandatory environment variables are set
MANDATORY_ENV_VARS="EXTRACTCODE_ENABLED HELPER_FOLDER SCANCODE_LICENSE_SCORE SCANCODE_OUTPUT_FORMAT SCANCODE_PROCESSES SCANCODE_SCAN_COPYRIGHT SCANCODE_SCAN_DIAGNOSTICS SCANCODE_SCAN_EMAIL SCANCODE_SCAN_INFO SCANCODE_SCAN_LICENSE SCANCODE_SCAN_PACKAGE SCANCODE_SCAN_URL SCANCODE_TIMEOUT SPDX_TOOL_VERSION TOOL_FOLDER"
failed=false
for i in $MANDATORY_ENV_VARS ; do
  check_env_var_is_set $i
done
if $failed ; then
  echo "Please make sure that mandatory environment variables are passed to this script. Exiting."
  exit 1
fi

source "${HELPER_FOLDER}/message.sh"

function convert() {
  local spdx_file="$1"
  local spdx_json_file="$PDS_JOB_RESULT_FILE.spdx.json"

  echo "Converting to SPDX JSON"

  if [[ ! -f "$spdx_file" ]] ; then
    echo "Error file $spdx_file does not exist."
    return 1
  fi

  # use the SPDX tool converter to convert the SPDX tag-value to SPDX JSON
  java -jar "$TOOL_FOLDER/tools-java-${SPDX_TOOL_VERSION}-jar-with-dependencies.jar" Convert "$spdx_file" "$spdx_json_file" TAG JSON

  if [[ -f "$spdx_json_file" ]] ; then
    mv "$spdx_json_file" "$PDS_JOB_RESULT_FILE"
  else
    errorMessage "Product error. Unable to convert result to SPDX JSON."
  fi
}

# IMPORTANT: Keep the space in front and back of the list
output_formats=" json json-pp spdx-tv spdx-rdf spdx-json "
output_format="--spdx-tv"
convert_output_to_spdx_json=true
license_score="0"
scancode_processes="1"
file_scan_timeout="120"
diagnostics_file="diagnostics.json"

# make sure additional options is never empty
# otherwise scancode will have problems parsing the
# parameters and fail
options=""

echo ""
echo "------"
echo "System"
echo "------"
echo ""

# redirect from stderr to stdout. Extractcode writes its version number to stderr.
extractcode_version=$( extractcode --version 2>&1 )

# redirect from stderr to stdout. Python writes its version number to stderr.
python_version=$( python3 --version 2>&1 )

printf "%-26s %s\n" "Python:" "$python_version"
printf "%-26s %s\n" "PDS version:" "$PDS_VERSION"
printf "Scancode-Toolkit version:\n\n"
scancode --version
printf "\n\n%-26s %s\n" "Extractcode version:" "$extractcode_version"
printf "SPDX Tools version:\n\n"
java -jar "$TOOL_FOLDER/tools-java-$SPDX_TOOL_VERSION-jar-with-dependencies.jar" Version

echo ""
echo "---------"
echo "PDS Setup"
echo "---------"
echo ""

echo "SecHub Job UUID: $SECHUB_JOB_UUID"
echo "PDS Job UUID: $PDS_JOB_UUID"
echo ""

extracted_folder=""
if [[ "$PDS_JOB_HAS_EXTRACTED_SOURCES" == "true" ]] ; then
  echo "Extracted sources"
  extracted_folder="$PDS_JOB_EXTRACTED_SOURCES_FOLDER"
elif [[ "$PDS_JOB_HAS_EXTRACTED_BINARIES" == "true" ]] ; then
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
if [[ "$SCANCODE_PROCESSES" -ge 1 ]] ; then
  scancode_processes="$SCANCODE_PROCESSES"
  options="$options --processes $scancode_processes"
fi

echo "User provided license score: $SCANCODE_LICENSE_SCORE"
if [[ -n "$SCANCODE_LICENSE_SCORE" ]] ; then
  if [[ "$SCANCODE_LICENSE_SCORE" -le 100 && "$SCANCODE_LICENSE_SCORE" -ge 0 ]] ; then
    license_score="$SCANCODE_LICENSE_SCORE"
    options="$options --license-score $license_score"
  fi
fi

echo "User provided timeout: $SCANCODE_TIMEOUT"
if [[ -n "$SCANCODE_TIMEOUT" ]] ; then
  if [[ "$SCANCODE_TIMEOUT" -ge 1 ]] ; then
    file_scan_timeout="$SCANCODE_TIMEOUT"
    options="$options --timeout $file_scan_timeout"
  fi
fi

given_output_format="$(echo "$SCANCODE_OUTPUT_FORMAT" | tr '[:upper:]' '[:lower:]')"

echo "Possible output formats: $output_formats"
echo "User provided output format: $given_output_format"

# is given output format in output formats?
if [[ $output_formats =~ " $given_output_format " ]] ; then
  echo "User provided output format is in possible output formats."

  if [[ "$given_output_format" != "spdx-json" ]] ; then
    convert_output_to_spdx_json="false"
    output_format="--$given_output_format"
  fi
fi

if [[ "$SCANCODE_SCAN_COPYRIGHT" == "true" ]] ; then
  options="$options --copyright"
fi

if [[ "$SCANCODE_SCAN_DIAGNOSTICS" == "true" ]] ; then
  options="$options --json $diagnostics_file"
fi

if [[ "$SCANCODE_SCAN_EMAIL" == "true" ]] ; then
  options="$options --email"
fi

if [[ "$SCANCODE_SCAN_INFO" == "true" ]] ; then
  options="$options --info"
fi

if [[ "$SCANCODE_SCAN_LICENSE" == "true" ]] ; then
  options="$options --license"
fi

if [[ "$SCANCODE_SCAN_PACKAGE" == "true" ]] ; then
  options="$options --package"
fi

if [[ "$SCANCODE_SCAN_URL" == "true" ]] ; then
  options="$options --url"
fi

echo ""
echo "-------------"
echo "Starting scan"
echo "-------------"
echo ""

echo "Scancode processes: $scancode_processes"
echo "Minimum license score: $license_score"
echo "Timeout: $file_scan_timeout"
echo "Options: $options"
echo "Output format: $output_format"
echo ""

spdx_file="$PDS_JOB_RESULT_FILE.spdx"

extractcode_enabled=$( echo "$EXTRACTCODE_ENABLED" | tr '[:upper:]' '[:lower:]' )
if [[ "$extractcode_enabled" == "true" ]] ; then
  echo "Running extractcode"
  extractcode --verbose "$extracted_folder" 2>&1
fi

scancode $options --verbose --strip-root $output_format $spdx_file "$extracted_folder" 2>&1

echo ""
echo "----------------"
echo "Preparing result"
echo "----------------"
echo ""

if [[ "$SCANCODE_SCAN_DIAGNOSTICS" == "true" ]] ; then
  mv "$diagnostics_file" "${PDS_JOB_USER_MESSAGES_FOLDER}/INFO_message_$(date +%Y-%m-%d_%H.%M.%S_%N).txt"
fi

if $convert_output_to_spdx_json ; then
  convert "$spdx_file"
else
  echo "Moving SPDX file to $PDS_JOB_RESULT_FILE"
  mv "$PDS_JOB_RESULT_FILE.spdx" "$PDS_JOB_RESULT_FILE"
fi
