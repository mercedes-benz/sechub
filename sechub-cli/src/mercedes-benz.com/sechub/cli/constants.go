// SPDX-License-Identifier: MIT

package cli

import "time"

// CurrentAPIVersion - SecHub current api version
const CurrentAPIVersion = "1.0"

// DefaultSecHubConfigFile - name of the default SecHub configuration file when no other is set
const DefaultSecHubConfigFile = "sechub.json"

// DefaultReportFormat - Report format if not configured differently
const DefaultReportFormat = "json"

// DefaultTempDir - Create temporary files in current directory if not configured differently
const DefaultTempDir = "."

// DefaultWaitTime - Wait time in seconds.
// Will be used
// - for maximum pause between automatic status checks when action=scan
// - for pause between retries for failed HTTP calls
const DefaultWaitTime = 60

// MinimalWaitTimeSeconds - We don't allow intervals shorter than this to protect the SecHub server
const MinimalWaitTimeSeconds = 1

// initialWaitIntervalSeconds WaitIntervalIncreaseFactor - defines client's polling behaviour:
// 2s - 3s - 4.5s - 7s - 10s - 15s - 23s - 34s - 51s - 60s - 60s - 60s ...
const DefaultinitialWaitIntervalSeconds = 2
const InitialWaitIntervalNanoseconds = int64(DefaultinitialWaitIntervalSeconds * time.Second)
const WaitIntervalIncreaseFactor = 1.5

// MinimalInitialWaitIntervalSeconds - small value to enable quick integration tests. In real life: please stick with DefaultinitialWaitIntervalSeconds.
const MinimalInitialWaitIntervalSeconds = 0.1

// DefaultTimeoutInSeconds - Timeout for network communication in seconds
const DefaultTimeoutInSeconds = 120

// MinimalTimeoutInSeconds - Minimal allowed timeout setting
const MinimalTimeoutInSeconds = 10

// SizeOfJobList - Number of latest jobs to print
const SizeOfJobList = 20

// DefaultSourceCodeExcludeDirPatterns - Define directory patterns to exclude from zip file:
// - code in directories named "test" is not considered to end up in the binary
// - also ignore ".git" directory
// - ignore "node_modules" directories which may contain millions of lines of library code
var DefaultSourceCodeExcludeDirPatterns = []string{"**/test/**", "**/.git/**", "**/node_modules/**"}

// SupportedReportFormats - Supported output formats for SecHub reports
const ReportFormatJSON = "json"
const ReportFormatSPDXJSON = "spdx-json"
const ReportFormatHTML = "html"

var SupportedReportFormats = []string{ReportFormatJSON, ReportFormatHTML, ReportFormatSPDXJSON}

/* ---------------------------------- */
/* -------- Exit codes -------------- */
/* ---------------------------------- */

// ExitCodeOK means successful ended
const ExitCodeOK = 0

// ExitCodeFailed means common failure
const ExitCodeFailed = 1

// ExitCodeMissingParameter means a mandatory parameter was not set
const ExitCodeMissingParameter = 3

// ExitCodeMissingConfigFile means config file does not exist or is not valid
const ExitCodeMissingConfigFile = 4

// ExitCodeHTTPError and http error has occurred
const ExitCodeHTTPError = 5

// ExitCodeIllegalAction means action was illegal
const ExitCodeIllegalAction = 6

// ExitCodeMissingConfigParts means there were missing configuration parts
const ExitCodeMissingConfigParts = 7

// ExitCodeIOError and http error has occurred
const ExitCodeIOError = 8

// ExitCodeInvalidConfigFile means config file is not in expected format
const ExitCodeInvalidConfigFile = 9

// ExitCodeCanceled means that the scan job has been canceled on SecHub server
const ExitCodeCanceled = 10

/* ---------------------------------- */
/* -------- Actions ----------------- */
/* ---------------------------------- */

// scanAction - name of synchron scan action
const scanAction = "scan"

// scanAsynchronAction - name of asynchron scan action
const scanAsynchronAction = "scanAsync"

// getStatusAction - name of action to get status of Job
const getStatusAction = "getStatus"

// getReportAction - name of action to get report (json/html) of job
const getReportAction = "getReport"

// getFalsePositivesAction - name of action to download false-positives list (json) of project
const getFalsePositivesAction = "getFalsePositives"

// listJobsAction - name of action to list latest Jobs
const listJobsAction = "listJobs"

// markFalsePositivesAction - name of action to define false-positives of a project and upload it to SecHub server
const markFalsePositivesAction = "markFalsePositives"

// interactiveMarkFalsePositivesAction - name of action to interactively define false-positives of a project and upload it to SecHub server
const interactiveMarkFalsePositivesAction = "interactiveMarkFalsePositives"

// unmarkFalsePositivesAction - name of action to undefine false-positives of a project and upload it to SecHub server
const unmarkFalsePositivesAction = "unmarkFalsePositives"

// interactiveUnmarkFalsePositivesAction - name of action to interactively remove items from false-positives list of a project and upload it to SecHub server
const interactiveUnmarkFalsePositivesAction = "interactiveUnmarkFalsePositives"

// showHelpAction - name of action to display SecHub client help
const showHelpAction = "help"

// showVersionAction - name of action to display SecHub client version
const showVersionAction = "version"

/* --------------------------------------- */
/* -------- File Archive Constants ------- */
/* --------------------------------------- */

// archiveDataPrefix - Prefix in Zip or Tar archives for files from "data" section
const archiveDataPrefix = "__data__"

/* -------------------------------------- */
/* -------- Command line options -------- */
/* -------------------------------------- */

const apitokenOption = "apitoken"
const configfileOption = "configfile"
const fileOption = "file"
const helpOption = "help"
const jobUUIDOption = "jobUUID"
const labelOption = "label"
const outputOption = "output"
const projectOption = "project"
const quietOption = "quiet"
const reportformatOption = "reportformat"
const serverOption = "server"
const stopOnYellowOption = "stop-on-yellow"
const tempDirOption = "tempdir"
const timeoutOption = "timeout"
const userOption = "user"
const versionOption = "version"
const waitOption = "wait"

/* ----------------------------------------- */
/* -------- Environment variable names ----- */
/* ----------------------------------------- */

// SechubApitokenEnvVar - environment variable to set the SecHub api token
const SechubApitokenEnvVar = "SECHUB_APITOKEN"

// SechubDebugEnvVar - environment variable to enable debug output
const SechubDebugEnvVar = "SECHUB_DEBUG"

// SechubDebugHTTPEnvVar - environment variable to enable additional HTTP logging
const SechubDebugHTTPEnvVar = "SECHUB_DEBUG_HTTP"

// SechubIgnoreDefaultExcludesEnvVar - environment variable to make it possible to switch off default excludes (DefaultSourceCodeExcludeDirPatterns)
const SechubIgnoreDefaultExcludesEnvVar = "SECHUB_IGNORE_DEFAULT_EXCLUDES"

// SechubIgnoreDefaultExcludesEnvVar - environment variable to make it possible to switch off default excludes (DefaultSourceCodeExcludeDirPatterns)
const SechubIninitialWaitIntervalSecondsEnvVar = "SECHUB_INITIAL_WAIT_INTERVAL"

// SechubKeepTempfilesEnvVar - environment variable to keep temporary files
const SechubKeepTempfilesEnvVar = "SECHUB_KEEP_TEMPFILES"

// SechubLabelsEnvVar - environment variable to define labels. Comma separated if more than one.
// Example: "key1=value1,key2=value2"
const SechubLabelsEnvVar = "SECHUB_LABELS"

// SechubProjectEnvVar - environment variable to set the project ID
const SechubProjectEnvVar = "SECHUB_PROJECT"

// SechubQuietEnvVar - environment variable to set quiet mode
const SechubQuietEnvVar = "SECHUB_QUIET"

// SechubServerEnvVar - environment variable to set the SecHub server url
const SechubServerEnvVar = "SECHUB_SERVER"

// SechubTempDir - environment variable to set the directory for temporary files
const SechubTempDir = "SECHUB_TEMP_DIR"

// SechubTrustAllEnvVar - environment variable to disable ssl certificate checking
const SechubTrustAllEnvVar = "SECHUB_TRUSTALL"

// SechubUserIDEnvVar - environment variable to set the SecHub user ID
const SechubUserIDEnvVar = "SECHUB_USERID"

// SechubWaittimeDefaultEnvVar - environment variable to set poll interval for synchronous scans
const SechubWaittimeDefaultEnvVar = "SECHUB_WAITTIME_DEFAULT"

// SechubWhitelistAllEnvVar - environment variable to make it possible to switch off the default witelist for source code files.
//   Important: DefaultSourceCodeExcludeDirPatterns still remains active and can be turned off via SECHUB_IGNORE_DEFAULT_EXCLUDES environment variable.
const SechubWhitelistAllEnvVar = "SECHUB_WHITELIST_ALL"

/* ---------------------------------- */
/* -------- Status ------------------ */
/* ---------------------------------- */

// Job execution states
// as defined in sechub-commons-pds/src/main/java/com/mercedesbenz/sechub/commons/pds/data/PDSJobStatusState.java

// ExecutionStateCanceled - SecHub job has been canceled
const ExecutionStateCanceled = "CANCELED"

// ExecutionStateCanceled - SecHub job has been canceled
const ExecutionStateCancelRequested = "CANCEL_REQUESTED"

// ExecutionStateEnded - SecHub job has succesfully finished
const ExecutionStateEnded = "ENDED"

// JobStatusOkay - SecHub job has a report ready to download
const JobStatusOkay = "OK"

/* ---------------------------------- */
/* -------- Validation -------------- */
/* ---------------------------------- */

// MaximumBytesOfSecHubConfig - maximum byte length allowed for a SecHub config file
const MaximumBytesOfSecHubConfig = 20000

// MaximumNumberOfCMDLineArguments - maximum number of commandline args. os.Args will be capped if exceeded.
const MaximumNumberOfCMDLineArguments = 50

/* ---------------------------------- */
/* -------- Resilience -------------- */
/* ---------------------------------- */

// HTTPRetries - maximum number of retries for HTTP calls
const HTTPMaxRetries = 60
