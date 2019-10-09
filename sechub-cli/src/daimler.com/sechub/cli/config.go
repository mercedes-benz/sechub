// SPDX-License-Identifier: MIT
package cli

import (
	"flag"
	"fmt"
	"os"
	"strconv"
	"time"
)

/**
* Config for internal CLI calls
 */
type Config struct {
	user               string
	apiToken           string
	projectId          string
	server             string
	configFilePath     string
	trustAll           bool
	debug              bool
	keepTempFiles      bool
	action             string
	secHubJobUUID      string
	waitNanoseconds    int64
	timeOutNanoseconds int64
	outputFolder       string
	quiet              bool
	reportFormat       string
	stopOnYellow       bool
}

var apiTokenPtr *string
var versionPtr *bool
var stopOnYellowPtr *bool
var helpPtr *bool
var userPtr *string
var projectIdPtr *string
var serverPtr *string
var configFilePath *string
var configFilePathPtr *string
var secHubJobUUIDPtr *string
var waitSecondsPtr *int
var timeOutSecondsPtr *int
var outputFolderPathPtr *string
var reportFormatPtr *string

/* internal stuff - only necessary for development and testing*/
var debug = os.Getenv("SECHUB_DEBUG") == "true"
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

	apiTokenPtr = flag.String("apitoken", "", "The api token. This is a mandatory option for every action. Can NOT be defined in config file")
	versionPtr = flag.Bool("version", false, "Shows version info and terminates")
	stopOnYellowPtr = flag.Bool("stop-on-yellow", false, "When enabled a yellow traffic light will also break the build")
	helpPtr = flag.Bool("help", false, "Shows help and terminates")
	userPtr = flag.String("user", "", "userid - mandatory, but can also be defined in config file")
	projectIdPtr = flag.String("project", "", "unique project id - mandatory, but can also be defined in config file")
	serverPtr = flag.String("server", "", "server url of sechub server to use - e.g. https//example.com:8081. Mandatory, but can also be defined in config file")
	configFilePathPtr = flag.String("configfile", "", "path to sechub config file, if not defined './"+DefaultSecHubConfigFile+"' will be used")

	secHubJobUUIDPtr = flag.String("jobUUID", "", "sechub job uuid (mandatory when using '"+ActionExecuteGetStatus+"' or '"+ActionExecuteGetReport+"')")
	waitSecondsPtr = flag.Int("wait", defaultWaitTime, "wait time in seconds. Will be used for automatic status checks etc. when action='"+ActionExecuteSynchron+"'.")
	timeOutSecondsPtr = flag.Int("timeout", defaultTimeoutInSeconds, "time out for network communication in seconds.")
	outputFolderPathPtr = flag.String("output", "", "output folder for reports etc. per default current dir")
	reportFormatPtr = flag.String("reportformat", "json", "output format for reports, supported currently: [html,json]. If not a wellknown format json will always be the fallback.")

}

// NewConfigByFlags creates a new configuration based on flag settings
func NewConfigByFlags() *Config {
	flag.Parse()

	oneSecond := 1 * time.Second

	config := new(Config)

	config.apiToken = *apiTokenPtr
	config.user = *userPtr
	config.projectId = *projectIdPtr
	config.configFilePath = *configFilePathPtr
	config.server = *serverPtr
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

	if config.configFilePath == "" {
		config.configFilePath = DefaultSecHubConfigFile
	}
	config.action = flag.Arg(0)

	return config

}

func assertValidConfig(configPtr *Config) {
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

	if configPtr.projectId == "" {
		fmt.Println("project id missing!")
		os.Exit(ExitCodeMissingParameter)
	}

	if configPtr.server == "" {
		fmt.Println("sechub server not defined!")
		os.Exit(ExitCodeMissingParameter)
	}

	if configPtr.configFilePath == "" {
		fmt.Println("sechub config file not set")
		os.Exit(ExitCodeMissingParameter)
	}

	if configPtr.action == "" {
		fmt.Println("sechub action not set")
		os.Exit(ExitCodeMissingParameter)
	}
}
