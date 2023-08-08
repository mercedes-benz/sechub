#!/bin/bash 

set -e
lastCommandHandled=""

function startJob (){
    echo "**************************************************************************************************"
    echo "* Start job: $1"
    echo "**************************************************************************************************"
    export lastCommandHandled=$1
}

function step (){
    echo "- ++++++++++++++++++++++++++++++++++"
    echo "- STEP: $1"
    echo "- ++++++++++++++++++++++++++++++++++"
}

function showHelp () {
    echo "-------------------------------------" 
    echo "- SDC - SecHub developer command line"
    echo "-------------------------------------" 
    echo "Usage: Usage sdc"
    echo " Options: "
    echo "   -f,  --format-all                     : format all source code files"
    echo "   -b,  --build-full                     : full build"
    echo "   -d,  --document-full                  : full document build"
    echo "   -u,  --unit-tests                     : execute all unit tests"
    echo "   -i,  --integrationtest-all            : execute all integration tests"
    echo "   -ii, --integrationtest-integration    : execute integration tests from sechub-integrationtest only"
    echo "   -is, --integrationtest-systemtest     : execute integration tests from sechub-systemtest only"
    echo "   -r,  --report-combined-all            : create combined report for all"
    echo "   -c,  --clean-all                      : clean all"
    echo "   -ct, --clean-all-tests                : clean all test output"
    echo "   -cu, --clean-unit-tests               : clean all unit test output"
    echo "   -ci, --clean-integrationtests         : clean all integrationtest output"
    echo "   -si, --stop-inttest-server            : stop running integration test servers (SecHub, PDS)"
    echo "   -gj, --generate-java-api              : generates parts for java api"
    echo "   -h,  --help                       : show this help"
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
        -b|--build-full)
        FULL_BUILD="YES"
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
    ./gradlew clean -Dsechub.build.stage=all --console=plain
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
fi

if [[ "$FORMAT_CODE_ALL" = "YES" ]]; then
    startJob "Format all sourcecode"
    openApiFilePath="$SECHUB_ROOT_DIR/sechub-doc/build/api-spec/openapi3.json"
    if [ -f "$openApiFilePath" ]; then
        echo ">>> Open API file exists"
    else
        echo ">>> Open API file DOES NOT exist - must be generated."
         # Problem detected: open api file must be generated to avoid problems with gradle configuration lifecycle for open api generator!
         ./gradlew generateOpenapi 
    fi        
    ./gradlew spotlessApply -Dsechub.build.stage=all
    
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
    startJob "Execute full build (simulate github actions workflow 'gradle' "
    
    # Simulate github workflow "gradle"
    step "Build Client"
    ./gradlew spotlessCheck :sechub-cli:buildGo :sechub-cli:testGo
    
    step "Build Server, DAUI and generate OpenAPI file"
    ./gradlew ensureLocalhostCertificate build generateOpenapi buildDeveloperAdminUI -x :sechub-cli:build
    
    step "Generate and build Java projects related to SecHub Java API"
    ./gradlew :sechub-api-java:build :sechub-systemtest:build :sechub-pds-tools:buildPDSToolsCLI -Dsechub.build.stage=api-necessary
    
    step "Integration test"
    eval "${CMD_EXEC_ALL_INTEGRATIONTESTS}"
    
    step "Create combined test report"
    eval "${CMD_CREATE_COMBINED_REPORT}"
    
    step "Create documentation"
    ./gradlew documentation -Dsechub.build.stage=all
    
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



# -----------------------
# Handle unknown commands
# -----------------------
if [[ "${lastCommandHandled}" = "" ]]; then
    showHelp
    exit 1
fi

 