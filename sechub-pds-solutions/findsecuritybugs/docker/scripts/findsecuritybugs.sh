#!/usr/bin/env bash
# SPDX-License-Identifier: MIT

source "${HELPER_FOLDER}/message.sh"

function to_upper() {
    local mixedCase="$1"

    echo "$mixedCase" | tr '[:lower:]' '[:upper:]'
}

function to_lower() {
    local mixedCase="$1"

    echo "$mixedCase" | tr '[:upper:]' '[:lower:]'
}

function build_xml_bug_patterns() {
    local bug_patterns="$1"
    local is_exclude=$2
    
    local content=""

    # trim whitespaces
    bug_patterns=$(echo "$bug_patterns" | tr --delimiter=' ')

    IFS=,
    read line <<<$bug_patterns
    bug_pattern_list=( $line )

    for bug_pattern in "${bug_pattern_list[@]}"
    do
        content+="<Match><Bug pattern=\"$bug_pattern\" /></Match>"
    done

    echo "$content"
}

function build_xml_include() {
    local bug_patterns="$1"
    local is_exclude=$2

    local start_xml='<?xml version="1.0" encoding="UTF-8"?><FindBugsFilter>'
    local end_xml='</FindBugsFilter>'
    local middle_xml=""

    if [[ -n "$bug_patterns" ]]
    then 
        middle_xml=$(build_xml_bug_patterns "$bug_patterns" $is_exclude )
    fi

    content="$start_xml$middle_xml$end_xml"

    echo "$content"
}

echo "------------------"
echo "Find Security Bugs"
echo "------------------"
echo ""
echo "SecHub Job UUID: $SECHUB_JOB_UUID"

effort_levels=" min less default more max "
include_file="$TOOL_FOLDER/include.xml"
additional_options=""

if [[ "$PDS_JOB_HAS_EXTRACTED_BINARIES" == "true" ]]
then
	# The binaries are in a tar file (not compressed), therefore it is possible to check the file size
	# by checking the directory size
	binaries_size=$( du --summarize --bytes "$PDS_JOB_EXTRACTED_BINARIES_FOLDER/" | tr '[:space:]' ',' | cut --delimiter=',' --fields=1 )
	echo "Binaries size: $binaries_size"
	
	if [[ "$binaries_size" -gt "$PDS_MAX_FILE_UPLOAD_BYTES" ]]
	then
		errorMessage "File limit exceeded. File larger than $PDS_MAX_FILE_UPLOAD_BYTES bytes. Exiting."
		exit 1
	fi
	
    echo "Extracted folder structure:"
    echo ""
    tree "$PDS_JOB_EXTRACTED_BINARIES_FOLDER/"
    echo ""
else
    errorMessage "No files to analyze found. Exiting."
    exit 1
fi

# -experimental                            report of any confidence level including experimental bug patterns
# -low                                     report warnings of any confidence level
# -medium                                  report only medium and high confidence warnings [default]
# -high                                    report only high confidence warnings
if [[ -n "$FINDSECURITYBUGS_SEVERITY" ]]
then
    severity=$(to_upper "$FINDSECURITYBUGS_SEVERITY")

    case "$severity" in
        "HIGH")
            additional_options+=" -high"
            echo "Severity level: HIGH"
            ;;
        "MEDIUM")
            additional_options+=" -medium"
            echo "Severity level: MEDIUM"
            ;;
        "LOW")
            additional_options+=" -low"
            echo "Severity level: LOW"
            ;;
        "EXPERIMENTAL")
            additional_options+=" -experimental"
            echo "Severity level: EXPERIMENTAL"
            ;;
        *)
            echo "WARNING: Unknown Severity level: $severity"
            ;;
    esac
fi

# -effort[:min|less|default|more|max]      set analysis effort level
if [[ -n "$FINDSECURITYBUGS_EFFORT" ]]
then
    effort_level=$(to_lower "$FINDSECURITYBUGS_EFFORT")

    if [[ $effort_levels =~ " $effort_level " ]]
    then
        echo "Effort level: $effort_level"
        additional_options+=" -effort:$effort_level"
    fi
fi

if [[ -n "$FINDSECURITYBUGS_INCLUDE_BUGPATTERNS" ]]
then
    echo "Only include the following bug patterns in the report: $FINDSECURITYBUGS_INCLUDE_BUGPATTERNS"
    include_xml=$(build_xml_include "$FINDSECURITYBUGS_INCLUDE_BUGPATTERNS" false)
    bug_patterns_include_file="$PDS_JOB_WORKSPACE_LOCATION/bug_patterns_include.xml"
    echo "$include_xml" | xmllint --format - > "$bug_patterns_include_file"
    include_file="$bug_patterns_include_file"
fi

if [[ -n "$FINDSECURITYBUGS_EXCLUDE_BUGPATTERNS" ]]
then
    echo "Exclude the following bug patterns in the report: $FINDSECURITYBUGS_EXCLUDE_BUGPATTERNS"
    exclude_xml=$(build_xml_include "$FINDSECURITYBUGS_EXCLUDE_BUGPATTERNS" true)
    bug_patterns_exclude_file="$PDS_JOB_WORKSPACE_LOCATION/bug_patterns_exclude.xml"
    echo "$exclude_xml" | xmllint --format - > "$bug_patterns_exclude_file"

    echo "Exclude file: $bug_patterns_exclude_file"
    cat "$bug_patterns_exclude_file"
    echo ""

    additional_options+=" -exclude $bug_patterns_exclude_file"
fi

echo "Include file: $include_file"
cat "$include_file"
echo ""
echo "Additional options: $additional_options"
echo ""
echo "# Start analyzing"
"$TOOL_FOLDER/findsecbugs_sechub.sh" -sarif -progress "$additional_options" -include "$include_file" -output "$PDS_JOB_RESULT_FILE" "$PDS_JOB_EXTRACTED_BINARIES_FOLDER/"