// SPDX-License-Identifier: MIT

package cli

// CurrentAPIVersion - SecHub current api version
const CurrentAPIVersion = "1.0"

// DefaultSecHubConfigFile represents the name of the sechub configuration file
// used per default when no other set
const DefaultSecHubConfigFile = "sechub.json"

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

/* ---------------------------------- */
/* -------- Actions ----------------- */
/* ---------------------------------- */

// ActionExecuteSynchron name of synchron scan action
const ActionExecuteSynchron = "scan"

// ActionExecuteAsynchron name of asynchron scan action
const ActionExecuteAsynchron = "scanAsync"

// ActionExecuteGetStatus name of action to get status of Job
const ActionExecuteGetStatus = "getStatus"

// ActionExecuteGetReport name of action to get report (json/html) of job
const ActionExecuteGetReport = "getReport"

// ActionExecuteGetFalsePositives name of action to download false-positives list (json) of project
const ActionExecuteGetFalsePositives = "getFalsePositives"

// ActionExecuteAddFalsePositives name of action to define false-positives of a project and upload it to SecHub server
const ActionExecuteAddFalsePositives = "addFalsePositives"

// ActionExecuteMarkFalsePositives name of action to interactively define false-positives of a project and upload it to SecHub server
const ActionExecuteMarkFalsePositives = "markFalsePositives"

// ActionExecuteRemoveFalsePositives name of action to undefine false-positives of a project and upload it to SecHub server
const ActionExecuteRemoveFalsePositives = "removeFalsePositives"

// ActionExecuteUnmarkFalsePositives name of action to interactively remove items from false-positives list of a project and upload it to SecHub server
const ActionExecuteUnmarkFalsePositives = "unmarkFalsePositives"

/* ---------------------------------- */
/* -------- Status ------------------ */
/* ---------------------------------- */

// ExecutionStateEnded sechub job has succesfully finished
const ExecutionStateEnded = "ENDED"
