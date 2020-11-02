// SPDX-License-Identifier: MIT

package cli

// CurrentAPIVersion - SecHub current api version
const CurrentAPIVersion = "1.0"

// DefaultSecHubConfigFile represents the name of the sechub configuration file
// used per default when no other set
const DefaultSecHubConfigFile = "sechub.json"

// DefaultReportFormat - Report format if not configured differently
const DefaultReportFormat = "json"

// DefaultWaitTime - Wait time in seconds.
// Will be used for automatic status checks etc. when action=scan
const DefaultWaitTime = 60

// DefaultTimeoutInSeconds - Timeout for network communication in seconds
const DefaultTimeoutInSeconds = 120

// DefaultZipExcludeDirPatterns - Define directory patterns to exclude from zip file:
// code in directories named "test" is not considered to end up in the binary
// also ignore .git directory
var DefaultZipExcludeDirPatterns = []string{"**/test/**", "**/.git/**"}

/* ---------------------------------- */
/* -------- Exit codes -------------- */
/* ---------------------------------- */

// ExitCodeOK means all okay...
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

/* ---------------------------------- */
/* -------- Actions ----------------- */
/* ---------------------------------- */

// scanAction name of synchron scan action
const scanAction = "scan"

// scanAsynchronAction name of asynchron scan action
const scanAsynchronAction = "scanAsync"

// getStatusAction name of action to get status of Job
const getStatusAction = "getStatus"

// getReportAction name of action to get report (json/html) of job
const getReportAction = "getReport"

// getFalsePositivesAction name of action to download false-positives list (json) of project
const getFalsePositivesAction = "getFalsePositives"

// markFalsePositivesAction name of action to define false-positives of a project and upload it to SecHub server
const markFalsePositivesAction = "markFalsePositives"

// interactiveMarkFalsePositivesAction name of action to interactively define false-positives of a project and upload it to SecHub server
const interactiveMarkFalsePositivesAction = "interactiveMarkFalsePositives"

// unmarkFalsePositivesAction name of action to undefine false-positives of a project and upload it to SecHub server
const unmarkFalsePositivesAction = "unmarkFalsePositives"

// interactiveUnmarkFalsePositivesAction name of action to interactively remove items from false-positives list of a project and upload it to SecHub server
const interactiveUnmarkFalsePositivesAction = "interactiveUnmarkFalsePositives"

// showHelpAction - name of action to display SecHub client help
const showHelpAction = "help"

// showVersionAction - name of action to display SecHub client version
const showVersionAction = "version"

/* ---------------------------------- */
/* -------- Status ------------------ */
/* ---------------------------------- */

// ExecutionStateEnded sechub job has succesfully finished
const ExecutionStateEnded = "ENDED"

/* ---------------------------------- */
/* -------- Validation--------------- */
/* ---------------------------------- */

// MaximumBytesOfSecHubConfig maximum byte length allowed for a sechub config file
const MaximumBytesOfSecHubConfig = 20000
