#!/bin/bash 

function showHelp () {
    echo "-------------------------------------" 
    echo "- SDC - SecHub developer command line"
    echo "-------------------------------------" 
    echo "Usage: Usage sdc"
    echo " Options: "
    echo "   -f,  --format-all                 : format all source code files"
    echo "   -i,  --integrationtest-all        : execute all integration tests"
    echo "   -ii, --integrationtest-integration: execute integration tests from sechub-integrationtest only"
    echo "   -is, --integrationtest-systemtest : execute integration tests from sechub-systemtest only"
    echo "   -r,  --report-combined-all        : create combined report for all"
    echo "   -c,  --clean-all                  : clean all"
    echo "   -ct, --clean-all-tests            : clean all test output"
    echo "   -cut,--clean-unit-tests           : clean all unit test output"
    echo "   -cit,--clean-integrationtests     : clean all integrationtest output"
    echo "   -h,  --help                       : show this help"
}

lastCommandHandled=""

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
    -cut|--clean-unit-tests)
    CLEAN_UNIT_TESTS="YES"
    shift # past argument
    ;;
    -cit|--clean-integrationtests)
    CLEAN_INTEGRATIONTEST="YES"
    shift # past argument
    ;;
    -b|--full-build)
    FULL_BUILD="YES"
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

CMD_EXEC_ALL_INTEGRATIONTESTS="./gradlew :sechub-integrationtest:startIntegrationTestInstances :sechub-systemtest:integrationtest :sechub-integrationtest:integrationtest :sechub-integrationtest:stopIntegrationTestInstances -Dsechub.build.stage=all --console=plain"
CMD_CREATE_COMBINED_REPORT="./gradlew createCombinedTestReport -Dsechub.build.stage=all"
# -----------------------
# Handle known commands
# -----------------------
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
    ./gradlew spotlessApply -Dsechub.build.stage=all
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
    ./gradlew :sechub-cli:buildGo :sechub-cli:testGo
    
    step "Build Server, DAUI and generate OpenAPI file"
    ./gradlew ensureLocalhostCertificate build generateOpenapi buildDeveloperAdminUI -x :sechub-integrationtest:test -x :sechub-cli:build
    
    step "Generate and build Java projects related to SecHub Java API"
    ./gradlew :sechub-api-java:build :sechub-systemtest:build :sechub-pds-tools:buildPDSToolsCLI -Dsechub.build.stage=api-necessary
    
    step "Integration test"
    eval "${CMD_EXEC_ALL_INTEGRATIONTESTS}"
    
    step "Create combined test report"
    eval "${CMD_CREATE_COMBINED_REPORT}"
    
    step "Create documentation"
    ./gradlew documentation
    
fi

if [[ "$REPORT_COMBINED" = "YES" ]]; then
    startJob "Create combined test report"
    eval "${CMD_CREATE_COMBINED_REPORT}"
fi



# -----------------------
# Handle unknown commands
# -----------------------
if [[ "${lastCommandHandled}" = "" ]]; then
    showHelp
    exit 1
fi

 