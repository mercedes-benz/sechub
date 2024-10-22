#!/bin/bash 
# SPDX-License-Identifier: MIT

set -e
trap handleExitCodes EXIT

lastCommandHandled=""

RED='\033[0;31m'
LIGHT_RED='\033[1;31m'
LIGHT_GREEN='\033[1;32m'
BROWN='\033[0;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'

NC='\033[0m' # No Color

# Define global variables for GitHub action apts
gha_sechub_server_version=1.7.0
gha_sechub_server_port=8443
gha_pds_version=1.4.0
gha_pds_port=8444


function startJob (){
    echo -e "${BROWN}---------------------------------------------------${NC}"
    echo -e "${BROWN} Start job: $1"
    echo -e "${BROWN}---------------------------------------------------${NC}"
    export lastCommandHandled=$1
}

function startTask (){
    echo -e "${BLUE}[ $1 ]${NC}"
}

function specialTask (){
    echo -e "${PURPLE}[ $1 ]${NC}"
}


function step (){
    echo "- ++++++++++++++++++++++++++++++++++"
    echo "- STEP: $1"
    echo "- ++++++++++++++++++++++++++++++++++"
}

function showHelp () {
    echo -e "${BROWN}---------------------------------------------------${NC}"
    echo -e "${BROWN}- SDC - SecHub developer command line${NC}"
    echo -e "${BROWN}---------------------------------------------------${NC}" 
    echo "Usage: Usage sdc"
    echo " Option s: "                                  
    echo "  -f,   --format-all                          : format all source code files"
    echo "  -sd,  --show-directory-to-project-root      : shows directory to sechub project root directory"
    echo ""                           
    echo "  -b,   --build-full                          : full build"
    echo "  -bpt, --build-pds-tools                     : build pds tools"
    echo "  -d,   --document-full                       : full document build"
    echo "  -gj,  --generate-java-api                   : generates parts for java api"
    echo ""                           
    echo "  -u,   --unit-tests                          : execute all unit tests"
    echo "  -i,   --integrationtest-all                 : execute all integration tests (java)"
    echo "  -ii,  --integrationtest-integration         : execute integration tests from sechub-integrationtest only"
    echo "  -is,  --integrationtest-systemtest          : execute integration tests from sechub-systemtest only"
    echo "  -r,   --report-combined-all                 : create combined report for all"
    echo ""                           
    echo "  -c,   --clean-all                           : clean all"
    echo "  -ct,  --clean-all-tests                     : clean all test output"
    echo "  -cu,  --clean-unit-tests                    : clean all unit test output"
    echo "  -ci,  --clean-integrationtests              : clean all integrationtest output"
    echo "  -cr,  --clean-reports                       : clean all reports"
    echo ""                           
    echo "  -si,  --stop-inttest-server                 : stop running integration test servers (SecHub, PDS)"
    echo ""                           
    echo "  -pigh,--prepare-integrationtest-gh-action   : prepare integration test data for github actions ((re)start SecHub, PDS and init data)"                  
    echo "  -igh, --integrationtest-github-action       : execute integration tests for github action only (nodejs, -pigh initial necessary)"
    echo "  -bgh, --build-github-action                 : full build of github action with integration tests (prepare etc. is all done automatically)" 
    echo "  -agh  --audit-github-action                 : start audit for github action - checks for newer dependencies/vulnerabilities etc."                                            
    echo ""                                            
    echo "  -syg, --start-systemtest-sanity-check-gosec : start systemtest 'sanity-check' for gosec with local build pds tools (0.0.0)" 
    echo ""                                            
    echo "  -h,   --help                                : show this help"

}

function prepareGitHubActionIntegrationTest(){
    # Set working directory (to default)
    cd $SECHUB_ROOT_DIR
    cd ./github-actions/scan

    startTask "Setup integration test data"
    echo "Version and port variables are globally defined"

    # Set working directory
    cd ./__test__/integrationtest/
    
    # next lines only for our local build: we stop always former running integration test servers (to call it multiple times)
    specialTask "Stop former running test servers"
    ./05-stop.sh $gha_sechub_server_port $gha_pds_port

    # Start integration test servers
    startTask "Start integration test servers"
    ./01-start.sh $gha_sechub_server_version $gha_sechub_server_port $gha_pds_version $gha_pds_port
    
    # Init integration test data
    startTask "Init integration test data"
    ./03-init_sechub_data.sh $gha_sechub_server_port $gha_pds_port
}

function runGitHubActionIntegrationTests(){
    # Set working directory (to default)
    cd $SECHUB_ROOT_DIR
    cd ./github-actions/scan

    # Run integration tests
    startTask "Run integration tests"
    npm run integration-test
}

function handleExitCodes(){
    lastExitCode=$?
    
    if [[ "$lastExitCode" = "0" ]]; then
        echo -e "${LIGHT_GREEN}SUCCESSFUL${NC}"
    else
        echo -e "${RED}FAILED${NC} - exit code: $lastExitCode"
    fi
}

SCRIPT_DIR="$(dirname -- "$0")"
cd ${SCRIPT_DIR}
cd ..
cd ..

# At this point we are inside sechub root folder as current working directory
SECHUB_ROOT_DIR=$(pwd)

if [[ "$SECHUB_DEBUG" = "true" ]]; then
    echo "PWD=$(pwd)"
    echo "SCRIPT_DIR=$SCRIPT_DIR"
    echo "SECHUB_ROOT_DIR=$SECHUB_ROOT_DIR"
fi
    
POSITIONAL=()
while [[ $# -gt 0 ]]
do
    key="$1"
    
    
    case $key in
        -f|--format-all)
        FORMAT_CODE_ALL="YES"
        shift # past argument
        ;;
        -sd|--show-directory-to-project-root)
        SHOW_DIRECTORY_TO_PROJECT_ROOT="YES"
        shift # past argument
        ;;
        -r|--report-combined)
        REPORT_COMBINED="YES"
        shift # past argument
        ;;
        -i|--integrationtest-all)
        INTEGRATIONTEST_ALL="YES"
        shift # past argument
        ;;
        -ii|--integrationtest-integratio)
        INTEGRATIONTEST_INTEGRATION="YES"
        shift # past argument
        ;;
        -is|--integrationtest-systemtest)
        INTEGRATIONTEST_SYSTEMTEST="YES"
        shift # past argument
        ;;
        -c|--clean-all)
        CLEAN_ALL="YES"
        shift # past argument
        ;;
        -ct|--clean-all-tests)
        CLEAN_ALL_TESTS="YES"
        shift # past argument
        ;;
        -cu|--clean-unit-tests)
        CLEAN_UNIT_TESTS="YES"
        shift # past argument
        ;;
        -ci|--clean-integrationtests)
        CLEAN_INTEGRATIONTEST="YES"
        shift # past argument
        ;;
        -cr|--clean-reports)
        CLEAN_REPORTS="YES"
        shift # past argument
        ;;
        -b|--build-full)
        FULL_BUILD="YES"
        shift # past argument
        ;;
        -bpt|--build-pds-tools)
        PDS_TOOLS_BUILD="YES"
        shift # past argument
        ;;
        -bgh|--build-github-action)
        GITHUB_ACTION_BUILD="YES"
        shift # past argument
        ;;
        -pigh|--prepare-integrationtest-gh-action)
        GITHUB_ACTION_PREPARE_INTEGRATIONTEST="YES"
        shift # past argument
        ;;
        -igh|--integrationtest-github-action)
        GITHUB_ACTION_START_INTEGRATIONTEST="YES"
        shift # past argument
        ;;
        -agh|--audit-github-action)
        GITHUB_ACTION_START_AUDIT="YES"
        shift # past argument
        ;;
        -d|--document-full)
        DOCUMENT_FULL="YES"
        shift # past argument
        ;;
        -gj|--generate-java-api)
        GENERATE_JAVA_API="YES"
        shift # past argument
        ;;
        -u|--unit-tests)
        UNIT_TESTS="YES"
        shift # past argument
        ;;
        -si|--stop-inttest-server)
        STOP_SERVERS="YES"
        shift # past argument
        ;;
        -syg|--start-systemtest-sanitycheck-gosec)
        START_SYSTEMTEST_SANITYCHECK_GOSEC="YES"
        shift # past argument
        ;;
        -x|--xsearchpath)
        SEARCHPATH="$2"
        shift # past argument
        shift # past value
        ;;
        -h|--help)
        HELP=YES
        shift # past argument
        ;;
        *)    # unknown option
        POSITIONAL+=("$1") # save it in an array for later
        shift # past argument
        ;;
    esac
done

set -- "${POSITIONAL[@]}" # restore positional parameters

#
# Help - will always just print out help and exit with 0
#
if [[ "$HELP" = "YES" ]]; then
    showHelp
    exit 0
fi

function cleanOldReportData() {
  if [ -f ./sechub_report_*.json ]; then
    rm ./sechub_report_*.json -f
  fi
}


CMD_EXEC_ALL_INTEGRATIONTESTS="./gradlew :sechub-integrationtest:startIntegrationTestInstances :sechub-systemtest:integrationtest :sechub-integrationtest:integrationtest :sechub-integrationtest:stopIntegrationTestInstances -Dsechub.build.stage=all --console=plain"
CMD_CREATE_COMBINED_REPORT="./gradlew createCombinedTestReport -Dsechub.build.stage=all"
# -----------------------
# Handle known commands
# -----------------------
if [[ "$STOP_SERVERS" = "YES" ]]; then
    startJob "Stop running servers"
    ./gradlew stopIntegrationTestInstances
fi
if [[ "$CLEAN_ALL" = "YES" ]]; then
    startJob "Clean all"
    step "Clean all gradle parts"
    ./gradlew clean -Dsechub.build.stage=all --console=plain
    
    step "Clean old report data"
    cleanOldReportData
fi
if [[ "$CLEAN_ALL_TESTS" = "YES" ]]; then
    startJob "Clean all tests"
    ./gradlew cleanTest cleanIntegrationtest -Dsechub.build.stage=all --console=plain
fi
if [[ "$CLEAN_UNIT_TESTS" = "YES" ]]; then
    startJob "Clean all unit tests"
    ./gradlew cleanTest -Dsechub.build.stage=all --console=plain
fi
if [[ "$CLEAN_INTEGRATIONTEST" = "YES" ]]; then
    startJob "Clean all integration tests"
    ./gradlew cleanIntegrationtest -Dsechub.build.stage=all --console=plain
    
    step "Clean old report data"
    cleanOldReportData
fi

if [[ "$CLEAN_REPORTS" = "YES" ]]; then
    startJob "Clean all reports"
    cleanOldReportData
fi
if [[ "$SHOW_DIRECTORY_TO_PROJECT_ROOT" = "YES" ]]; then
    startJob "Show directory of SecHub project root"
    echo "$SECHUB_ROOT_DIR"
    exit 0
fi

if [[ "$FORMAT_CODE_ALL" = "YES" ]]; then
    startJob "Format all sourcecode"
    ./gradlew spotlessApply
    
fi

if [[ "$UNIT_TESTS" = "YES" ]]; then
    startJob "Execute all unit tests"
    ./gradlew test -Dsechub.build.stage=all --console=plain
fi

if [[ "$INTEGRATIONTEST_ALL" = "YES" ]]; then
    startJob "Execute all integration tests"
    eval "${CMD_EXEC_ALL_INTEGRATIONTESTS}"
fi

if [[ "$INTEGRATIONTEST_INTEGRATION" = "YES" ]]; then
    startJob "Execute integration tests (sechub-integrationtest only)"
    ./gradlew integrationtest --console=plain
fi
if [[ "$INTEGRATIONTEST_SYSTEMTEST" = "YES" ]]; then
    startJob "Execute integration tests (sechub-systemtest only)"
    ./gradlew :sechub-integrationtest:startIntegrationTestInstances :sechub-systemtest:integrationtest :sechub-integrationtest:stopIntegrationTestInstances -Dsechub.build.stage=all --console=plain
fi

if [[ "$FULL_BUILD" = "YES" ]]; then
    startJob "Execute full build (simulate github actions workflow 'gradle' )"
    
    # Simulate github workflow "gradle"
    step "Build Client"
    ./gradlew spotlessCheck :sechub-cli:buildGo :sechub-cli:testGo
    
    step "Build Server, DAUI and generate OpenAPI file"
    ./gradlew ensureLocalhostCertificate build generateOpenapi buildDeveloperAdminUI -x :sechub-cli:build
    
    step "Generate and build Java projects related to SecHub Java API"
    ./gradlew :sechub-api-java:build :sechub-systemtest:build :sechub-pds-tools:buildPDSToolsCLI :sechub-webui:build -Dsechub.build.stage=api-necessary
    
    step "Integration test"
    eval "${CMD_EXEC_ALL_INTEGRATIONTESTS}"
    
    step "Create combined test report"
    eval "${CMD_CREATE_COMBINED_REPORT}"
    
    step "Create documentation"
    ./gradlew documentation -Dsechub.build.stage=all
    
fi

if [[ "$PDS_TOOLS_BUILD" = "YES" ]]; then
     startJob "Execute build pds tools"
     step "Generate and build Java projects related to SecHub Java API"
    ./gradlew :sechub-api-java:build :sechub-systemtest:build :sechub-pds-tools:buildPDSToolsCLI -Dsechub.build.stage=api-necessary
  
fi

if [[ "$DOCUMENT_FULL" = "YES" ]]; then
    startJob "Create documentation"
    ./gradlew documentation -Dsechub.build.stage=all
fi

if [[ "$REPORT_COMBINED" = "YES" ]]; then
    startJob "Create combined test report"
    eval "${CMD_CREATE_COMBINED_REPORT}"
fi

if [[ "$GENERATE_JAVA_API" = "YES" ]]; then
    startJob "Regenerate open api class files"
    cd $SECHUB_ROOT_DIR
    cd sechub-api-java
    ./fullRegenerateOpenAPIClassFiles.sh
    cd $SECHUB_ROOT_DIR 
fi

if [[ "$START_SYSTEMTEST_SANITYCHECK_GOSEC" = "YES" ]]; then
    startJob "Start systemtest 'sanity-check' for GoSec"
    cd $SECHUB_ROOT_DIR
    cd sechub-pds-solutions/gosec/tests
    #java -Djdk.httpclient.HttpClient.log=requests,headers,errors -jar $SECHUB_ROOT_DIR/sechub-pds-tools/build/libs/sechub-pds-tools-cli-0.0.0.jar systemtest --file systemtest_local.json --pds-solutions-rootfolder ../../ --sechub-solution-rootfolder ../../../sechub-solution --run-tests sanity-check
    java -jar $SECHUB_ROOT_DIR/sechub-pds-tools/build/libs/sechub-pds-tools-cli-0.0.0.jar systemtest --file systemtest_local.json --pds-solutions-rootfolder ../../ --sechub-solution-rootfolder ../../../sechub-solution --run-tests sanity-check
    cd $SECHUB_ROOT_DIR 
fi

if [[ "$GITHUB_ACTION_PREPARE_INTEGRATIONTEST" = "YES" ]]; then
    startJob "Prepare integration test situation for GitHub action"
    
    prepareGitHubActionIntegrationTest

fi

if [[ "$GITHUB_ACTION_START_AUDIT" = "YES" ]]; then
    startJob "Start github action audit"
    cd $SECHUB_ROOT_DIR
    cd ./github-actions/scan
    
    npm audit fix
fi

if [[ "$GITHUB_ACTION_START_INTEGRATIONTEST" = "YES" ]]; then
    startJob "Start integration tests for GitHub action"
    
    runGitHubActionIntegrationTests

fi

# Builds github action ("scan") like done in workflow, means the integration tests are executed as well)
# We do here the same steps as done in 'github-action-scan.yml'
if [[ "$GITHUB_ACTION_BUILD" = "YES" ]]; then
    ### --------------------------
    ### Build GitHub action "scan"
    ### --------------------------
    startJob "Build GitHub action 'scan'"
    
    prepareGitHubActionIntegrationTest
    
    cd $SECHUB_ROOT_DIR
    cd ./github-actions/scan

    specialTask "Use Node.js"
    echo "Not done locally. Please setup this on your machine by nvm. For example: 'nvm use node' (sets to latest)"

    startTask "Clean install"
    npm ci

    startTask "Build"
    npm run build --if-present

    startTask "Run unit tests"
    npm test
    
    runGitHubActionIntegrationTests

    # Cleanup integration tests
    startTask "Cleanup integration tests"
    cd $SECHUB_ROOT_DIR
    cd ./github-actions/scan
    cd ./__test__/integrationtest/
    ./05-stop.sh $gha_sechub_server_port $gha_pds_port
fi


# -----------------------
# Handle unknown commands
# -----------------------
if [[ "${lastCommandHandled}" = "" ]]; then
    showHelp
    exit 1
fi

 
