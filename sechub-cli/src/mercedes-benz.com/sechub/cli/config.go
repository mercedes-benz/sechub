// SPDX-License-Identifier: MIT

package cli

import (
	"flag"
	"fmt"
	"os"
	"path/filepath"
	"reflect"
	"strconv"
	"strings"
	"time"

	sechubUtil "mercedes-benz.com/sechub/util"
)

// Config for internal CLI calls
type Config struct {
	action                string
	apiToken              string
	configFilePath        string
	configFileRead        bool
	debug                 bool
	debugHTTP             bool
	file                  string
	ignoreDefaultExcludes bool
	keepTempFiles         bool
	outputFileName        string
	outputFolder          string
	outputLocation        string
	projectID             string
	quiet                 bool
	reportFormat          string
	secHubJobUUID         string
	server                string
	stopOnYellow          bool
	tempDir               string
	timeOutNanoseconds    int64
	timeOutSeconds        int
	trustAll              bool
	user                  string
	waitNanoseconds       int64
	waitSeconds           int
	whitelistAll          bool
}

// Initialize Config with default values
var configFromInit Config = Config{
	configFilePath: DefaultSecHubConfigFile,
	reportFormat:   DefaultReportFormat,
	tempDir:        DefaultTempDir,
	timeOutSeconds: DefaultTimeoutInSeconds,
	waitSeconds:    DefaultWaitTime,
}

var flagHelp bool
var flagVersion bool

var missingFieldHelpTexts = map[string]string{
	"server":         "Server URL is missing. Can be defined with option '-server' or in environment variable " + SechubServerEnvVar + " or in config file.",
	"user":           "User id is missing. Can be defined with option '-user' or in environment variable " + SechubUserIDEnvVar + " or in config file.",
	"apiToken":       "API Token is missing. Can be defined in environment variable " + SechubApitokenEnvVar + ".",
	"projectID":      "Project id is missing. Can be defined with option '-project' or in environment variable " + SechubProjectEnvVar + " or in config file.",
	"configFileRead": "Unable to read config file (defaults to 'sechub.json'). Config file is mandatory for this action.",
	"file":           "Input file name is not provided which is mandatory for this action. Can be defined with option '-file'.",
}

// Global Go initialization (is called before main())
func init() {
	prepareOptionsFromCommandline(&configFromInit)
	parseConfigFromEnvironment(&configFromInit)
}

func prepareOptionsFromCommandline(config *Config) {
	flag.StringVar(&config.apiToken,
		apitokenOption, config.apiToken, "The api token - Mandatory. Please try to avoid '-apitoken' parameter for security reasons. Use environment variable "+SechubApitokenEnvVar+" instead!")
	flag.StringVar(&config.configFilePath,
		configfileOption, config.configFilePath, "Path to sechub config file")
	flag.StringVar(&config.file,
		fileOption, "", "Defines file to read from for actions '"+markFalsePositivesAction+"' or '"+interactiveMarkFalsePositivesAction+"' or '"+unmarkFalsePositivesAction+"'")
	flag.BoolVar(&flagHelp,
		helpOption, false, "Shows help and terminates")
	flag.StringVar(&config.secHubJobUUID,
		jobUUIDOption, "", "SecHub job uuid - Mandatory for actions '"+getStatusAction+"' or '"+getReportAction+"'")
	flag.StringVar(&config.outputLocation,
		outputOption, "", "Where to place reports, false-positive files etc. Can be a directory, a file name or a file path. (Defaults to current directory)")
	flag.StringVar(&config.projectID,
		projectOption, config.projectID, "SecHub project id - Mandatory, but can also be defined in environment variable "+SechubProjectEnvVar+" or in config file")
	flag.BoolVar(&config.quiet,
		quietOption, false, "Quiet mode - Suppress all informative output. Can also be defined in environment variable "+SechubQuietEnvVar+"=true")
	flag.StringVar(&config.reportFormat,
		reportformatOption, config.reportFormat, "Output format for reports, supported currently: "+fmt.Sprint(SupportedReportFormats)+".")
	flag.StringVar(&config.server,
		serverOption, config.server, "Server url of sechub server to use - e.g. 'https://sechub.example.com:8443'. Mandatory, but can also be defined in environment variable "+SechubServerEnvVar+" or in config file")
	flag.BoolVar(&config.stopOnYellow,
		stopOnYellowOption, config.stopOnYellow, "Makes a yellow traffic light in the scan also break the build")
	flag.StringVar(&config.tempDir,
		tempDirOption, config.tempDir, "Temporary directory - Temporary files will be placed here. Can also be defined in environment variable "+SechubTempDir)
	flag.IntVar(&config.timeOutSeconds,
		timeoutOption, config.timeOutSeconds, "Timeout for network communication in seconds.")
	flag.StringVar(&config.user,
		userOption, config.user, "User id - Mandatory, but can also be defined in environment variable "+SechubUserIDEnvVar+" or in config file")
	flag.BoolVar(&flagVersion,
		versionOption, false, "Shows version info and terminates")
	flag.IntVar(&config.waitSeconds,
		waitOption, config.waitSeconds, "Maximum wait time in seconds - For status checks of action='"+scanAction+"' and for retries of HTTP calls. Can also be defined in environment variable "+SechubWaittimeDefaultEnvVar)
}

func parseConfigFromEnvironment(config *Config) {
	apiTokenFromEnv :=
		os.Getenv(SechubApitokenEnvVar)
	config.debug =
		os.Getenv(SechubDebugEnvVar) == "true"
	config.debugHTTP =
		os.Getenv(SechubDebugHTTPEnvVar) == "true"
	config.ignoreDefaultExcludes =
		os.Getenv(SechubIgnoreDefaultExcludesEnvVar) == "true" // make it possible to switch off default excludes
	config.keepTempFiles =
		os.Getenv(SechubKeepTempfilesEnvVar) == "true"
	config.quiet =
		os.Getenv(SechubQuietEnvVar) == "true"
	config.trustAll =
		os.Getenv(SechubTrustAllEnvVar) == "true"
	projectFromEnv :=
		os.Getenv(SechubProjectEnvVar)
	serverFromEnv :=
		os.Getenv(SechubServerEnvVar)
	tempDirFromEnv :=
		os.Getenv(SechubTempDir)
	userFromEnv :=
		os.Getenv(SechubUserIDEnvVar)
	waittimeFromEnv :=
		os.Getenv(SechubWaittimeDefaultEnvVar)
	config.whitelistAll =
		os.Getenv(SechubWhitelistAllEnvVar) == "true"

	if apiTokenFromEnv != "" {
		config.apiToken = apiTokenFromEnv
	}
	if projectFromEnv != "" {
		config.projectID = projectFromEnv
	}
	if serverFromEnv != "" {
		config.server = serverFromEnv
	}
	if tempDirFromEnv != "" {
		config.tempDir = tempDirFromEnv
	}
	if userFromEnv != "" {
		config.user = userFromEnv
	}
	if waittimeFromEnv != "" {
		config.waitSeconds, _ = strconv.Atoi(waittimeFromEnv)
	}
}

// NewConfigByFlags parses commandline flags which override environment variable settings or defaults defined in init()
func NewConfigByFlags() *Config {
	// Normalize arguments from commandline
	os.Args = normalizeCMDLineArgs(os.Args)

	// Parse command line options
	flag.Parse()

	if flagHelp {
		configFromInit.action = showHelpAction
	} else if flagVersion {
		configFromInit.action = showVersionAction
	} else {
		if len(flag.Args()) == 0 {
			// Default: Show help if no action is given
			configFromInit.action = showHelpAction
		} else {
			// read `action` from commandline
			configFromInit.action = flag.Arg(0)
		}
	}

	return &configFromInit
}

func assertValidConfig(config *Config) {
	if config.trustAll {
		if !config.quiet {
			sechubUtil.LogWarning("Configured to trust all - means unknown service certificate is accepted. Don't use this in production!")
		}
	}

	// -------------------------------------
	//  Define mandatory fields for actions
	// -------------------------------------
	checklist := map[string][]string{
		scanAction:                            {"server", "user", "apiToken", "projectID", "configFileRead"},
		scanAsynchronAction:                   {"server", "user", "apiToken", "projectID", "configFileRead"},
		getStatusAction:                       {"server", "user", "apiToken", "projectID", "secHubJobUUID"},
		getReportAction:                       {"server", "user", "apiToken", "projectID", "secHubJobUUID"},
		getFalsePositivesAction:               {"server", "user", "apiToken", "projectID"},
		markFalsePositivesAction:              {"server", "user", "apiToken", "projectID", "file"},
		unmarkFalsePositivesAction:            {"server", "user", "apiToken", "projectID", "file"},
		interactiveMarkFalsePositivesAction:   {"server", "user", "apiToken", "projectID"},
		interactiveUnmarkFalsePositivesAction: {"server", "user", "apiToken", "projectID"},
		showHelpAction:                        {},
		showVersionAction:                     {},
	}

	/* --------------------------------------------------
	 * 					Validation
	 * --------------------------------------------------
	 */
	if config.action == "" {
		sechubUtil.LogError("sechub action not set")
		showHelpHint()
		os.Exit(ExitCodeMissingParameter)
	}

	errorsFound := false
	if mandatoryFields, ok := checklist[config.action]; ok {
		for _, fieldname := range mandatoryFields {
			if !isConfigFieldFilled(config, fieldname) {
				errorsFound = true
			}
		}
	} else {
		sechubUtil.LogError("Unknown action: '" + config.action + "'")
		errorsFound = true
	}

	if !validateRequestedReportFormat(config) {
		errorsFound = true
	}
	if !validateTempDir(config) {
		errorsFound = true
	}
	if !validateOutputLocation(config) {
		errorsFound = true
	}

	if config.action == interactiveMarkFalsePositivesAction && config.file == "" {
		// Let's try to find the latest report (default naming scheme) and take this as file
		config.file = sechubUtil.FindNewestMatchingFileInDir("sechub_report_"+config.projectID+"_.+\\.json$", ".", config.debug)
		if config.file == "" {
			sechubUtil.LogError("An input file is needed for action '" + interactiveMarkFalsePositivesAction + "'. Please define input file with -file option.")
			errorsFound = true
		} else {
			fmt.Printf("Using latest report file %q.\n", config.file)
		}
	}

	if errorsFound {
		showHelpHint()
		os.Exit(ExitCodeMissingParameter)
	}

	// For convenience: lowercase user id and project id if needed
	config.user = lowercaseOrNotice(config.user, "user id")
	config.projectID = lowercaseOrNotice(config.projectID, "project id")

	// Remove trailing slash from url if present
	config.server = strings.TrimSuffix(config.server, "/")

	config.waitSeconds = validateWaitTimeOrWarning(config.waitSeconds)
	config.waitNanoseconds = int64(config.waitSeconds) * int64(time.Second)

	config.timeOutSeconds = validateTimeoutOrWarning(config.timeOutSeconds)
	config.timeOutNanoseconds = int64(config.timeOutSeconds) * int64(time.Second)
}

// isConfigFieldFilled checks if field is not empty or is 'true' in case of boolean type
func isConfigFieldFilled(configPTR *Config, field string) bool {
	value := fmt.Sprintf("%v", reflect.ValueOf(configPTR).Elem().FieldByName(field))
	if value == "" || value == "false" {
		sechubUtil.LogError(missingFieldHelpTexts[field])
		return false
	}
	return true
}

// validateRequestedReportFormat issue warning in case of an unknown report format + lowercase if needed
func validateRequestedReportFormat(config *Config) bool {
	config.reportFormat = lowercaseOrNotice(config.reportFormat, "requested report format")

	if !sechubUtil.StringArrayContains(SupportedReportFormats, config.reportFormat) {
		sechubUtil.LogWarning("Unsupported report format '" + config.reportFormat + "'. Changing to 'json'.")
		config.reportFormat = "json"
	}
	return true
}

// normalizeCMDLineArgs - Make sure that the `action` is last in the argument list
//                        Otherwise flag.Parse() will not work properly.
func normalizeCMDLineArgs(args []string) []string {
	if len(args) == 1 {
		return args
	}

	action := ""
	pos := -1
	for i, arg := range args[1:] {
		if !strings.HasPrefix(arg, "-") {
			action = arg
			if i == 0 || (i == pos+1 && i != 1) {
				// exit loop if 1st argument or if two consecuting values appear
				pos = i
				break
			}
			pos = i
		}
	}

	var result []string
	if pos == -1 {
		result = args
	} else {
		result = args[:pos+1]
		if pos < len(args) {
			result = append(result, args[pos+2:]...)
		}
		result = append(result, action)
	}
	return result
}

// tempFile - creates a filepath depending on configured temp dir
func tempFile(context *Context, filename string) string {
	if context.config.tempDir == "." {
		return filename
	}
	return context.config.tempDir + "/" + filename
}

func validateTempDir(config *Config) bool {
	tempDirAbsolute, _ := filepath.Abs(config.tempDir)

	if !sechubUtil.VerifyDirectoryExists(tempDirAbsolute) {
		directoryLocation := tempDirAbsolute
		if config.tempDir != tempDirAbsolute {
			directoryLocation = fmt.Sprintf("%s (%s)", config.tempDir, tempDirAbsolute)
		}
		sechubUtil.LogError("Invalid value passed with '-" + tempDirOption + "': File does not exist or is not a directory: " + directoryLocation)
		return false
	}

	config.tempDir = tempDirAbsolute
	return true
}

// validateOutputLocation - Check given output location and fill config.outputFolder and config.outputFileName accordingly
func validateOutputLocation(config *Config) bool {
	if config.outputLocation == "" || config.outputLocation == "." {
		config.outputFolder = "."
	} else if !strings.Contains(config.outputLocation, "/") && !strings.Contains(config.outputLocation, string(os.PathSeparator)) {
		// Only a name is provided - can be an output file name or a directory
		if sechubUtil.VerifyDirectoryExists(config.outputLocation) {
			config.outputFolder, _ = filepath.Abs(config.outputLocation)
		} else {
			config.outputFolder = "."
			config.outputFileName = config.outputLocation
		}
	} else {
		dir := filepath.Dir(config.outputLocation)
		if !sechubUtil.VerifyDirectoryExists(dir) {
			sechubUtil.LogError("Invalid value passed with '-" + outputOption + "': Directory does not exist: " + dir)
			return false
		}
		if sechubUtil.VerifyDirectoryExists(config.outputLocation) {
			config.outputFolder, _ = filepath.Abs(config.outputLocation)
		} else {
			config.outputFolder, _ = filepath.Abs(dir)
			config.outputFileName = filepath.Base(config.outputLocation)
		}
	}
	return true
}

func validateWaitTimeOrWarning(waitTime int) int {
	// Verify wait time and ensure MinimalWaitTimeSeconds
	if waitTime < MinimalWaitTimeSeconds {
		sechubUtil.LogWarning(
			fmt.Sprintf("Desired wait intervall (%d s) is too small. Setting to %d seconds.", waitTime, MinimalWaitTimeSeconds))
		waitTime = MinimalWaitTimeSeconds
	}
	return waitTime
}

func validateTimeoutOrWarning(timeout int) int {
	// Verify timeout and ensure MinimalTimeoutInSeconds
	if timeout < MinimalTimeoutInSeconds {
		sechubUtil.LogWarning(
			fmt.Sprintf("Desired timeout (%d s) is too small. Setting to %d seconds.", timeout, MinimalTimeoutInSeconds))
		timeout = MinimalTimeoutInSeconds
	}
	return timeout
}
