// SPDX-License-Identifier: MIT

package cli

import (
	"flag"
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"

	sechubUtil "daimler.com/sechub/util"
)

// Config for internal CLI calls
type Config struct {
	action             string
	apiToken           string
	configFilePath     string
	debug              bool
	file               string
	keepTempFiles      bool
	outputFolder       string
	projectID          string
	quiet              bool
	reportFormat       string
	secHubJobUUID      string
	server             string
	stopOnYellow       bool
	timeOutNanoseconds int64
	trustAll           bool
	user               string
	waitNanoseconds    int64
}

var apiTokenPtr *string
var configFilePath *string
var configFilePathPtr *string
var filePtr *string
var helpPtr *bool
var outputFolderPathPtr *string
var projectIDPtr *string
var reportFormatPtr *string
var secHubJobUUIDPtr *string
var serverPtr *string
var stopOnYellowPtr *bool
var timeOutSecondsPtr *int
var userPtr *string
var versionPtr *bool
var waitSecondsPtr *int

/* internal stuff - only necessary for development and testing*/
var debug = os.Getenv("SECHUB_DEBUG") == "true"
var ignoreDefaultExcludes = os.Getenv("SECHUB_IGNORE_DEFAULT_EXCLUDES") == "true" // make it possible to switch off default excludes
var keepTempFiles = os.Getenv("SECHUB_KEEP_TEMPFILES") == "true"
var quiet = os.Getenv("SECHUB_QUIET") == "true"
var trustAll = os.Getenv("SECHUB_TRUSTALL") == "true"

var defaultWaitTime = 60
var defaultTimeoutInSeconds = 120

var defaultWaitTimeEnv = os.Getenv("SECHUB_WAITTIME_DEFAULT")

func init() {
	if defaultWaitTimeEnv != "" {
		defaultWaitTime, _ = strconv.Atoi(defaultWaitTimeEnv)
	}

	apiTokenPtr = flag.String(
		"apitoken", "", "The api token. This is a mandatory option for every action. Can NOT be defined in config file")
	configFilePathPtr = flag.String(
		"configfile", DefaultSecHubConfigFile, "Path to sechub config file, if not defined './"+DefaultSecHubConfigFile+"' will be used")
	filePtr = flag.String(
		"file", "", "Defines file to read from for these actions: "+ActionExecuteAddFalsePositives+" "+ActionExecuteMarkFalsePositives+" "+ActionExecuteRemoveFalsePositives)
	helpPtr = flag.Bool(
		"help", false, "Shows help and terminates")
	secHubJobUUIDPtr = flag.String(
		"jobUUID", "", "SecHub job uuid (mandatory when using '"+ActionExecuteGetStatus+"' or '"+ActionExecuteGetReport+"')")
	outputFolderPathPtr = flag.String(
		"output", ".", "Output folder for reports etc. per default current dir")
	projectIDPtr = flag.String(
		"project", "", "SecHub project id - mandatory, but can also be defined in config file")
	reportFormatPtr = flag.String(
		"reportformat", "json", "Output format for reports, supported currently: [html,json]. If not a wellknown format json will always be the fallback.")
	serverPtr = flag.String(
		"server", "", "Server url of sechub server to use - e.g. https//example.com:8081. Mandatory, but can also be defined in config file")
	stopOnYellowPtr = flag.Bool(
		"stop-on-yellow", false, "When enabled a yellow traffic light will also break the build")
	timeOutSecondsPtr = flag.Int(
		"timeout", defaultTimeoutInSeconds, "Timeout for network communication in seconds.")
	userPtr = flag.String(
		"user", "", "User id - mandatory, but can also be defined in config file")
	versionPtr = flag.Bool(
		"version", false, "Shows version info and terminates")
	waitSecondsPtr = flag.Int(
		"wait", defaultWaitTime, "Wait time in seconds. Will be used for automatic status checks etc. when action='"+ActionExecuteSynchron+"'.")
}

// NewConfigByFlags creates a new configuration based on flag and environment variable settings
func NewConfigByFlags() *Config {
	flag.Parse()

	oneSecond := 1 * time.Second

	config := new(Config)

	config.apiToken = *apiTokenPtr
	if config.apiToken == "" { // read from environment variable if undefined on cmdline
		config.apiToken = os.Getenv("SECHUB_APITOKEN")
	} else {
		sechubUtil.LogWarning("Avoid '-apitoken' parameter for security reasons. Please use environment variable $SECHUB_APITOKEN instead!")
	}
	config.user = *userPtr
	if config.user == "" { // read from environment variable if undefined on cmdline
		config.user = os.Getenv("SECHUB_USERID")
	}
	config.server = *serverPtr
	if config.server == "" { // read from environment variable if undefined on cmdline
		config.server = os.Getenv("SECHUB_SERVER")
	}
	config.projectID = *projectIDPtr
	config.configFilePath = *configFilePathPtr
	config.file = *filePtr
	config.secHubJobUUID = *secHubJobUUIDPtr
	config.waitNanoseconds = int64(*waitSecondsPtr) * oneSecond.Nanoseconds()
	config.timeOutNanoseconds = int64(*timeOutSecondsPtr) * oneSecond.Nanoseconds()
	config.outputFolder = *outputFolderPathPtr
	config.reportFormat = *reportFormatPtr

	if *helpPtr == true {
		showHelpAndExit()
	}
	if *versionPtr == true {
		showVersionInfoAndExit()
	}

	config.trustAll = trustAll
	config.quiet = quiet
	config.debug = debug
	config.keepTempFiles = keepTempFiles
	config.stopOnYellow = *stopOnYellowPtr

	config.action = flag.Arg(0)

	return config
}

func assertValidConfig(configPtr *Config) {
	if configPtr.trustAll {
		if !configPtr.quiet {
			sechubUtil.LogWarning("Configured to trust all - means unknown service certificate is accepted. Don't use this in production!")
		}
	}

	/* --------------------------------------------------
	 * 					Validation
	 * --------------------------------------------------
	 */
	if configPtr.user == "" {
		fmt.Println("userid missing!")
		os.Exit(ExitCodeMissingParameter)
	}

	if configPtr.apiToken == "" {
		fmt.Println("api token missing!")
		os.Exit(ExitCodeMissingParameter)
	}

	if configPtr.projectID == "" {
		fmt.Println("project id missing!")
		os.Exit(ExitCodeMissingParameter)
	}

	if configPtr.server == "" {
		fmt.Println("sechub server not defined!")
		os.Exit(ExitCodeMissingParameter)
	} else {
		// remove trailing / from url if present
		configPtr.server = strings.TrimSuffix(configPtr.server, "/")
	}

	if configPtr.configFilePath == "" {
		fmt.Println("sechub config file not set")
		os.Exit(ExitCodeMissingParameter)
	}

	if configPtr.file == "" {
		if configPtr.action == ActionExecuteAddFalsePositives || configPtr.action == ActionExecuteRemoveFalsePositives {
			fmt.Printf("Input file is not set but is needed for action %q.\n", configPtr.action)
			fmt.Println("Please define input file with -file option.")
			os.Exit(ExitCodeMissingParameter)
		} else if configPtr.action == ActionExecuteMarkFalsePositives {
			// Let's try to find the latest report and take this as file
			configPtr.file = sechubUtil.FindNewestMatchingFileInDir("sechub_report_.+\\.json$", configPtr.outputFolder)
			if configPtr.file == "" {
				fmt.Printf("Input file is not set but is needed for action %q.\n", configPtr.action)
				fmt.Println("Please define input file with -file option.")
				os.Exit(ExitCodeMissingParameter)
			} else {
				fmt.Printf("Using latest report file %q.\n", configPtr.file)
			}
		}
	}

	if configPtr.secHubJobUUID == "" {
		if configPtr.action == ActionExecuteGetReport || configPtr.action == ActionExecuteGetStatus {
			fmt.Printf("SecHub job UUID is not set but is needed for action %q.\n", configPtr.action)
			fmt.Println("Please define job UUID with -jobUUID option.")
			os.Exit(ExitCodeMissingParameter)
		}
	}

	if configPtr.action == "" {
		fmt.Println("sechub action not set")
		showHelpHint()
		os.Exit(ExitCodeMissingParameter)
	}
}
