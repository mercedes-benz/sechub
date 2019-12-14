#!/bin/bash

# SPDX-License-Identifier: MIT

RED='\033[0;31m'
LIGHT_RED='\033[1;31m'
LIGHT_GREEN='\033[1;32m'
BROWN='\033[0;33m'
NC='\033[0m' # No Color


#
# apply spdx template to given file type
# param 1: fileending (e.g "yaml")
# param 2: template filename, will use templates inside sechub-other/spdx/template/$filename
function applySPDXonFirstLine {
    fileEnding=$1
    spxTemplate=$2

    echo -e "${LIGHT_GREEN}$Scanning '*.$fileEnding' files${NC}"
    find -iname \*.$fileEnding | while read file ; do
        if [[ -d $file ]]; then
            echo -e "${BROWN}$file${NC} - ${LIGHT_GREEN}ignored because directory.${NC}"
        elif ! grep -q SPDX-License $file
        then
            echo -e "${BROWN}$file${NC} - ${LIGHT_GREEN}appending copyright.${NC}"
            cat sechub-other/spdx/template/$spxTemplate $file >$file.new && mv $file.new $file
        fi

    done
}

function infoAboutManualParts {
    echo -e "${LIGHT_GREEN}Manual parts:${NC}"
    echo -e "${BROWN}- Bash files must be handled manual${NC}"
    echo "Reason?"
    echo "        This must be done in second line because of the #! string"
    echo "        Because apply-copyright-info.sh itself is a bash script and"
    echo "        also having only a small amount of bash scripts, we do not"
    echo "        automate this, so developers must add spdx info manually."
    echo "Why second line?"
    echo "        This is the exact way done by linux kernel project and so a good "
    echo "        approach, see https://lwn.net/Articles/739183/ :"
    echo -e "${BROWN}        \"... For kernel source files, the decision was made that the SPDX tag"
    echo -e "         should appear as the first line in the file (or the second line for"
    echo -e "         scripts where the first line must be the #! string)...\"${NC}"

}

function infoAboutIgnoredParts {
    echo -e "${LIGHT_GREEN}Ignored parts:${NC}"
    echo -e "${BROWN}- json files must be ignored${NC}"
    echo "Reason?"
    echo -e "       ${LIGHT_RED}Comments are not part of official syntax${NC}, see https://www.json.org/json-en.html"
    echo "       So many tools and libraries often have problems with javascript comments"
    echo "       inside JSON. Having declared MIT license also everybody is allowed to remove"
    echo "       an SPDX enry without licence conflict ... so we decided to add no spdx"
    echo "       entries in json files."

}

function startAutoApply {

    applySPDXonFirstLine "java" "spdx_template_doubleslash.txt"
    applySPDXonFirstLine "groovy" "spdx_template_doubleslash.txt"
    applySPDXonFirstLine "gradle" "spdx_template_doubleslash.txt"
    applySPDXonFirstLine "go" "spdx_template_doubleslash.txt"
    applySPDXonFirstLine "adoc" "spdx_template_doubleslash.txt"

    # for plantuml we do no longer apply automatically, because a comment before
    # a @startUml is problematic

    applySPDXonFirstLine "properties" "spdx_template_hash.txt"

    applySPDXonFirstLine "yaml" "spdx_template_hash.txt"
    applySPDXonFirstLine "yml" "spdx_template_hash.txt"

    applySPDXonFirstLine "md" "spdx_template_md.txt"
}

echo "*******************************"
echo "* Apply copyright information *"
echo "*******************************"
echo
infoAboutManualParts
infoAboutIgnoredParts
echo -e "${LIGHT_GREEN}Automated parts:${NC}"
echo "When you continue next step the automation will start:"

read -n 1 -p "Continue ?(y/n):" continueSelect

echo
echo "--------------------------------------------"
echo "Start applying missing copyright information"
echo "--------------------------------------------"
echo
if [ "$continueSelect" == "y" ]; then
    startAutoApply
else
    echo "Canceled"
fi

