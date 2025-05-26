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
	action                         string
	addSCMHistory                  bool
	apiToken                       string
	configFilePath                 string
	configFileRead                 bool
	debug                          bool
	debugHTTP                      bool
	failOnRed                      bool
	failOnYellow                   bool
	file                           string
	ignoreDefaultExcludes          bool
	initialWaitIntervalNanoseconds int64
	keepTempFiles                  bool
	labels                         map[string]string
	outputFileName                 string
	outputFolder                   string
	outputLocation                 string
	projectID                      string
	quiet                          bool
	reportFormat                   string
	secHubJobUUID                  string
	server                         string
	tempDir                        string
	timeOutNanoseconds             int64
	timeOutSeconds                 int
	trustAll                       bool
	user                           string
	waitNanoseconds                int64
	waitSeconds                    int
	whitelistAll                   bool
}

// Initialize Config with default values
var configFromInit Config = Config{
	configFilePath:                 DefaultSecHubConfigFile,
	initialWaitIntervalNanoseconds: int64(DefaultinitialWaitIntervalSeconds * time.Second),
	labels:                         map[string]string{},
	reportFormat:                   DefaultReportFormat,
	tempDir:                        DefaultTempDir,
	timeOutSeconds:                 DefaultTimeoutInSeconds,
	waitSeconds:                    DefaultWaitTime,
}

var missingFieldHelpTexts = map[string]string{
	"server":         "Server URL is missing. Can be defined with option '-" + serverOption + "' or in environment variable " + SechubServerEnvVar + " or in config file.",
	"user":           "User id is missing. Can be defined with option '-" + userOption + "' or in environment variable " + SechubUserIDEnvVar + " or in config file.",
	"apiToken":       "API Token is missing. Can be defined in environment variable " + SechubApitokenEnvVar + ".",
	"projectID":      "Project id is missing. Can be defined with option '-" + projectOption + "' or in environment variable " + SechubProjectEnvVar + " or in config file.",
	"configFileRead": "Unable to read config file (defaults to 'sechub.json'). Config file is mandatory for this action.",
	"file":           "Input file name is not provided which is mandatory for this action. Can be defined with option '-" + fileOption + "'.",
	"secHubJobUUID":  "SecHub job-UUID is missing. Please use option '-" + jobUUIDOption + "'.",
}

// Global Go initialization (is called before main())
func init() {
	prepareOptionsFromCommandline(&configFromInit)
	parseConfigFromEnvironment(&configFromInit)
}

func prepareOptionsFromCommandline(config *Config) {
	flag.BoolVar(&config.addSCMHistory,
		addSCMHistoryOption, false, "Secrets scan only: Upload SCM directories like .git for scanning. Can also be defined in environment variable "+SechubAddSCMHistoryEnvVar+"=true")
	flag.StringVar(&config.apiToken,
		apitokenOption, config.apiToken, "The api token - Mandatory. Please try to avoid '-apitoken' parameter for security reasons. Use environment variable "+SechubApitokenEnvVar+" instead!")
	flag.StringVar(&config.configFilePath,
		configfileOption, config.configFilePath, "Path to SecHub config file")
	flag.StringVar(&config.file,
		fileOption, "", "Defines file to read from for actions '"+defineFalsePositivesAction+"', '"+markFalsePositivesAction+"', '"+interactiveMarkFalsePositivesAction+"', '"+unmarkFalsePositivesAction+"'")
	flag.StringVar(&config.secHubJobUUID,
		jobUUIDOption, "", "SecHub job uuid - Optional for actions '"+getStatusAction+"' or '"+getReportAction+"'")
	flag.Func(labelOption, "Define a `SecHub label` for scan or filtering. (Example: \"key1=value1\") Repeat to define multiple labels.", func(s string) error {
		var err error
		config.labels, err = addLabelToList(config.labels, s, true)
		if err != nil {
			return err
		}
		return nil
	})
	flag.StringVar(&config.outputLocation,
		outputOption, "", "Where to place reports, false-positive files etc. Can be a directory, a file name or a file path. (Defaults to current directory)")
	flag.StringVar(&config.projectID,
		projectOption, config.projectID, "SecHub project id - Mandatory, but can also be defined in environment variable "+SechubProjectEnvVar+" or in config file")
	flag.BoolVar(&config.quiet,
		quietOption, false, "Quiet mode - Suppress all informative output. Can also be defined in environment variable "+SechubQuietEnvVar+"=true")
	flag.StringVar(&config.reportFormat,
		reportformatOption, config.reportFormat, "Output format for reports, supported currently: "+fmt.Sprint(SupportedReportFormats)+".")
	flag.StringVar(&config.server,
		serverOption, config.server, "Server url of SecHub server to use - e.g. 'https://sechub.example.com:8443'.\nMandatory, but can also be defined in environment variable "+SechubServerEnvVar+" or in config file")
	flag.BoolVar(&config.failOnYellow,
		failOnYellowOption, config.failOnYellow, "Makes a yellow traffic light in the scan also break the build")
	flag.StringVar(&config.tempDir,
		tempDirOption, config.tempDir, "Temporary directory - Temporary files will be placed here. Can also be defined in environment variable "+SechubTempDir)
	flag.IntVar(&config.timeOutSeconds,
		timeoutOption, config.timeOutSeconds, "Timeout for network communication in seconds.")
	flag.StringVar(&config.user,
		userOption, config.user, "User id - Mandatory, but can also be defined in environment variable "+SechubUserIDEnvVar+" or in config file")
	flag.IntVar(&config.waitSeconds,
		waitOption, config.waitSeconds, "Maximum wait time in seconds - For status checks of action='"+scanAction+"' and for retries of HTTP calls.\nCan also be defined in environment variable "+SechubWaittimeDefaultEnvVar)
}

func parseConfigFromEnvironment(config *Config) {
	var err error
	config.addSCMHistory =
		os.Getenv(SechubAddSCMHistoryEnvVar) == "true"
	apiTokenFromEnv :=
		os.Getenv(SechubApitokenEnvVar)
	config.debug =
		os.Getenv(SechubDebugEnvVar) == "true"
	config.debugHTTP =
		os.Getenv(SechubDebugHTTPEnvVar) == "true"
	if os.Getenv(SechubFailOnRedEnvVar) == "false" {
		config.failOnRed = false
	} else {
		config.failOnRed = true  // default behavior
	}
	config.ignoreDefaultExcludes =
		os.Getenv(SechubIgnoreDefaultExcludesEnvVar) == "true" // make it possible to switch off default excludes
	config.keepTempFiles =
		os.Getenv(SechubKeepTempfilesEnvVar) == "true"
	labelsRawDataFromEnv :=
		os.Getenv(SechubLabelsEnvVar)
	config.quiet =
		os.Getenv(SechubQuietEnvVar) == "true"
	config.trustAll =
		os.Getenv(SechubTrustAllEnvVar) == "true"
	initialWaitIntervalFromEnv :=
		os.Getenv(SechubIninitialWaitIntervalSecondsEnvVar)
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
	if initialWaitIntervalFromEnv != "" {
		initialWaitInterval, err := strconv.ParseFloat(initialWaitIntervalFromEnv, 64)
		if err == nil {
			config.initialWaitIntervalNanoseconds = int64(initialWaitInterval * float64(time.Second))
		} else {
			sechubUtil.LogWarning(fmt.Sprintf("Could not parse '%v' as number (read from $%s)", initialWaitIntervalFromEnv, SechubIninitialWaitIntervalSecondsEnvVar))
		}
	}
	if labelsRawDataFromEnv != "" {
		labelsFromEnv := strings.Split(labelsRawDataFromEnv, ",")
		for _, labelDefinition := range labelsFromEnv {
			config.labels, err = addLabelToList(config.labels, labelDefinition, false)
			if err != nil {
				sechubUtil.LogWarning(fmt.Sprintf("Parse error of environment variable '%s' as labels: %s)", SechubLabelsEnvVar, err))
			}
		}
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
	validateMaximumNumberOfCMDLineArgumentsOrCapAndWarning()

	// Normalize arguments from commandline
	os.Args = normalizeCMDLineArgs(os.Args)

	// Parse command line options
	flag.Parse()

	if len(flag.Args()) == 0 {
		// Default: Show help if no action is given
		configFromInit.action = showHelpAction
	} else {
		// read `action` from commandline
		configFromInit.action = flag.Arg(0)
	}

	return &configFromInit
}

func assertValidConfig(context *Context) {
	// Nothing to check if help output is requested
	if context.config.action == showHelpAction {
		return
	}

	if context.config.trustAll {
		if !context.config.quiet {
			sechubUtil.LogWarning("Configured to trust all - means unknown service certificate is accepted. Don't use this in production!")
		}
	}

	// -------------------------------------
	//  Define mandatory fields for actions
	// -------------------------------------
	checklist := map[string][]string{
		scanAction:                            {"server", "user", "apiToken", "projectID", "configFileRead"},
		scanAsynchronAction:                   {"server", "user", "apiToken", "projectID", "configFileRead"},
		cancelAction:                          {"secHubJobUUID"},
		getStatusAction:                       {"server", "user", "apiToken", "projectID", "secHubJobUUID"},
		getReportAction:                       {"server", "user", "apiToken", "projectID", "secHubJobUUID"},
		getFalsePositivesAction:               {"server", "user", "apiToken", "projectID"},
		listJobsAction:                        {"server", "user", "apiToken", "projectID"},
		defineFalsePositivesAction:            {"server", "user", "apiToken", "projectID"},
		markFalsePositivesAction:              {"server", "user", "apiToken", "projectID", "file"},
		unmarkFalsePositivesAction:            {"server", "user", "apiToken", "projectID", "file"},
		interactiveMarkFalsePositivesAction:   {"server", "user", "apiToken", "projectID"},
		interactiveUnmarkFalsePositivesAction: {"server", "user", "apiToken", "projectID"},
		showHelpAction:                        {},
		showVersionAction:                     {},
	}

	// --------------------------------------------------
	//  Validation
	// --------------------------------------------------

	// Check if one valid action is specified
	var detectedActions []string
	for action, _ := range checklist {
		for _, arg := range os.Args {
			if arg == action {
				detectedActions = append(detectedActions, action)
			}
		}
	}
	if len(detectedActions) == 0 {
		sechubUtil.LogError("SecHub action not set or unknown action.")
	} else if len(detectedActions) > 1 {
		sechubUtil.LogError(fmt.Sprint("Multiple actions set: ", detectedActions, ". Only one action is possible."))
	}
	if len(detectedActions) != 1 {
		showHelpHint()
		os.Exit(ExitCodeMissingParameter)
	}

	// For convenience: lowercase user id and project id if needed
	context.config.user = lowercaseOrNotice(context.config.user, "user id", false)
	context.config.projectID = lowercaseOrNotice(context.config.projectID, "project id", false)

	// Check mandatory fields for the requested action
	errorsFound := false
	if mandatoryFields, ok := checklist[context.config.action]; ok {
		for _, fieldname := range mandatoryFields {
			// Try to get latest secHubJobUUID from server if not provided
			if fieldname == "secHubJobUUID" && context.config.secHubJobUUID == "" {
				switch context.config.action {
				case cancelAction:
					// Do NOT fetch the latest job UUID automatically in case of "cancel" action
					// So we do nothing here :-)
				case getReportAction:
					// Get job UUID from latest ended job
					context.config.secHubJobUUID = getLatestSecHubJobUUID(context, ExecutionStateEnded)
					sechubUtil.Log("Using latest finished job: "+context.config.secHubJobUUID, context.config.quiet)
				default:
					// Get job UUID from latest job (any state)
					context.config.secHubJobUUID = getLatestSecHubJobUUID(context)
				}
			}

			if !isConfigFieldFilled(context.config, fieldname) {
				errorsFound = true
			}
		}
	}

	if !validateRequestedReportFormat(context.config) {
		errorsFound = true
	}
	if !validateTempDir(context.config) {
		errorsFound = true
	}
	if !validateOutputLocation(context.config) {
		errorsFound = true
	}
	if context.config.addSCMHistory {
		validateAddScmHistory(context)
	}

	if context.config.action == interactiveMarkFalsePositivesAction && context.config.file == "" {
		// Let's try to find the latest report (default naming scheme) and take this as file
		context.config.file = sechubUtil.FindNewestMatchingFileInDir("sechub_report_"+context.config.projectID+"_.+\\.json$", ".", context.config.debug)
		if context.config.file == "" {
			sechubUtil.LogError("An input file is needed for action '" + interactiveMarkFalsePositivesAction + "'. Please define input file with -file option.")
			errorsFound = true
		} else {
			fmt.Printf("Using latest report file %q.\n", context.config.file)
		}
	}

	if errorsFound {
		showHelpHint()
		os.Exit(ExitCodeMissingParameter)
	}

	// Remove trailing slash from url if present
	context.config.server = strings.TrimSuffix(context.config.server, "/")

	context.config.initialWaitIntervalNanoseconds = validateInitialWaitIntervalOrWarning(context.config.initialWaitIntervalNanoseconds)

	context.config.waitSeconds = validateWaitTimeOrWarning(context.config.waitSeconds)
	context.config.waitNanoseconds = int64(context.config.waitSeconds) * int64(time.Second)

	context.config.timeOutSeconds = validateTimeoutOrWarning(context.config.timeOutSeconds)
	context.config.timeOutNanoseconds = int64(context.config.timeOutSeconds) * int64(time.Second)
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
	config.reportFormat = lowercaseOrNotice(config.reportFormat, "requested report format", false)

	if !sechubUtil.StringArrayContains(SupportedReportFormats, config.reportFormat) {
		sechubUtil.LogWarning("Unsupported report format '" + config.reportFormat + "'. Changing to '" + ReportFormatJSON + "'.")
		config.reportFormat = ReportFormatJSON
	}
	return true
}

func actionSpellCorrection(action string) string {
	actionLowercase := strings.ToLower(action)
	for _, clientAction := range actionlist {
		if strings.ToLower(clientAction) == actionLowercase {
			return clientAction
		}
	}
	return action
}

// flagSpellCorrection - returns arg in correct case (if a cmdline option/flag matches)
func flagSpellCorrection(arg string) string {
	argLowercase := strings.ToLower(arg)
	for _, flag := range flaglist {
		if strings.ToLower(flag) == argLowercase {
			return flag
		}
	}
	return arg
}

// normalizeCMDLineArgs
// - Make sure that the `action` is last in the argument list. Otherwise flag.Parse() will not work properly.
// - Do a "spell correction" if the upper/lowercase spelling is not correct (action and args)
func normalizeCMDLineArgs(args []string) []string {
	numberOfArgs := len(args)
	if numberOfArgs == 1 {
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

	// Spell correction (upper/lowercase)
	for i, arg := range result[1:] {
		index := i + 1
		argname, found := strings.CutPrefix(arg, "-")
		if found {
			result[index] = "-" + flagSpellCorrection(argname)
		}
		// Last argument is the Client action
		if index == (numberOfArgs - 1) {
			result[index] = actionSpellCorrection(arg)
		}
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
	} else if !strings.Contains(config.outputLocation, "/") && !strings.Contains(config.outputLocation, PathSeparator) {
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
			fmt.Sprintf("Desired wait interval (%d s) is too short. Setting it to %d seconds.", waitTime, MinimalWaitTimeSeconds))
		waitTime = MinimalWaitTimeSeconds
	}
	return waitTime
}

func validateInitialWaitIntervalOrWarning(intervalNanoseconds int64) int64 {
	minimalInitialWaitIntervalNanoseconds := int64(MinimalInitialWaitIntervalSeconds * float64(time.Second))
	// Verify wait time and ensure minimalInitialWaitIntervalNanoseconds
	if intervalNanoseconds < minimalInitialWaitIntervalNanoseconds {
		sechubUtil.LogWarning(
			fmt.Sprintf("Desired initial wait interval (%v s) is too short. Setting it to %v s.",
				float64(intervalNanoseconds)/float64(time.Second),
				float64(minimalInitialWaitIntervalNanoseconds)/float64(time.Second)))
		intervalNanoseconds = minimalInitialWaitIntervalNanoseconds
	}
	return intervalNanoseconds
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

func validateMaximumNumberOfCMDLineArgumentsOrCapAndWarning() {
	if len(os.Args) > MaximumNumberOfCMDLineArguments {
		os.Args = os.Args[0:MaximumNumberOfCMDLineArguments]
		sechubUtil.LogWarning(fmt.Sprintf("Too many commandline arguments. Capping to %d.", MaximumNumberOfCMDLineArguments))
	}
}

func validateAddScmHistory(context *Context) {
	if context.config.addSCMHistory && len(context.sechubConfig.SecretScan.Use) == 0 {
		sechubUtil.LogWarning("You chose to append the SCM history but have configured no secretScan. The SCM history is not uploaded to SecHub.")
	}
}
