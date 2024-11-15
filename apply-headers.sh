#!/bin/bash
# SPDX-License-Identifier: MIT

SPDX_TEXT="SPDX-License-Identifier: MIT"

function isColoredTerminal(){
    # check if stdout is a terminal...
    if test -t 1; then

        # see if it supports colors...
        ncolors=$(tput colors)

        if test -n "$ncolors" && test $ncolors -ge 8; then
            return 0
        fi
    fi
    return 1
}

# apply spdx template to given file type
# param 1: file ending (e.g "yaml")
# param 2: SPDX License text to insert
# param 3: line where to insert the text (starting with 1)
function applySPDXline {
    local fileEnding="$1"
    local spdxMessage="$2"
    local line="$3"

    echo -e "  ${LIGHT_GREEN}Scanning '*.$fileEnding' files${NC}"
    # Loop over all files matching the pattern, but skip some patterns like generated files
    find . -type f -iname \*.$fileEnding \
    | grep -v '^./.git\|/build/\|/\.gradle/\|gradlew.bat\|sechub-cli/pkg/mod\|sechub-cli/src/mercedes-benz.com/sechub/pkg/mod/' \
    | while read file ; do
        if ! grep -q "$SPDX_TEXT" $file ; then
            sed -i "${line}i $spdxMessage" "$file"
            echo -e "${BROWN}$file${NC} - ${LIGHT_GREEN}copyright appended.${NC}"
        fi
    done
}

function applySPDXonFirstLine {
    applySPDXline "$1" "$2" 1
}

function applySPDXonSecondLine {
    applySPDXline "$1" "$2" 2
}

#####################################################
cd `dirname $0`

# define color variables when terminal and colors are enabled - otherwise we do not set variables so empty
if isColoredTerminal ; then
    RED='\033[0;31m'
    LIGHT_RED='\033[1;31m'
    LIGHT_GREEN='\033[1;32m'
    BROWN='\033[0;33m'
    NC='\033[0m' # No Color
fi

echo -e "*******************************"
echo -e "* Apply copyright information *"
echo -e "*******************************"
echo -e
echo -e "${LIGHT_GREEN}Ignored parts:${NC}"
echo -e "  ${BROWN}- json files must be ignored${NC}"
echo -e "  Reason?"
echo -e "    ${BROWN}Comments are not part of official syntax${NC}, see https://www.json.org/json-en.html"
echo -e "    So many tools and libraries often have problems with javascript comments"
echo -e "    inside JSON. Having declared MIT license also everybody is allowed to remove"
echo -e "    an SPDX enry without licence conflict ... so we decided to add no spdx"
echo -e "    entries in json files."
echo -e
echo -e "${LIGHT_GREEN}Automated parts:${NC}"
echo -e "  --------------------------------------------"
echo -e "  Start applying missing copyright information"
echo -e "  --------------------------------------------"

##########################################################
# Apply SPDX license headers:
applySPDXonFirstLine "adoc" "// $SPDX_TEXT"
applySPDXonFirstLine "puml" "' $SPDX_TEXT"
applySPDXonFirstLine "plantuml" "' $SPDX_TEXT"
applySPDXonFirstLine "bat" ":: $SPDX_TEXT"
applySPDXonFirstLine "c" "// $SPDX_TEXT"
applySPDXonFirstLine "dockerfile" "# $SPDX_TEXT"
applySPDXonFirstLine "go" "// $SPDX_TEXT"
applySPDXonFirstLine "groovy" "// $SPDX_TEXT"
applySPDXonFirstLine "gradle" "// $SPDX_TEXT"
applySPDXonFirstLine "jenkins" "// $SPDX_TEXT"
applySPDXonFirstLine "java" "// $SPDX_TEXT"
applySPDXonFirstLine "md" "<!-- $SPDX_TEXT --->"
applySPDXonFirstLine "properties" "# $SPDX_TEXT"
applySPDXonSecondLine "py" "# $SPDX_TEXT"
applySPDXonFirstLine "rb" "# $SPDX_TEXT"
applySPDXonSecondLine "sh" "# $SPDX_TEXT"
applySPDXonFirstLine "sql" "-- $SPDX_TEXT"
applySPDXonFirstLine "yaml" "# $SPDX_TEXT"
applySPDXonFirstLine "yml" "# $SPDX_TEXT"
applySPDXonFirstLine "ts" "// $SPDX_TEXT"
applySPDXonFirstLine "d.ts" "// $SPDX_TEXT"
applySPDXonFirstLine "vue" "<!-- $SPDX_TEXT -->"


# for plantuml we do no longer apply automatically, because a comment before
# a @startUml is problematic

##########################################################

exit 0
