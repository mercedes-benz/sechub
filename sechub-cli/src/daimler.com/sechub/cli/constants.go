// SPDX-License-Identifier: MIT

package cli

// DefaultSecHubConfigFile represents the name of the sechub configuration file
// used per default when no other set
const DefaultSecHubConfigFile = "sechub.json"

// DefaultZipExcludeDirPatterns - Define directory patterns to exclude from zip file:
// code in directories named "test" is not considered to end up in the binary
// also ignore .git directory
var DefaultZipExcludeDirPatterns = []string{"**/test/**", "**/.git/**"}

// DefaultZipAllowedFilePatterns - Define file patterns to include in zip file.
// These patterns are considered as source code to be scanned.
var DefaultZipAllowedFilePatterns = []string{
	".apex", ".apexp", ".component", ".object", ".page", ".report", ".tgr", ".trigger", ".workflow", /* Apex */
	".asp",           /* ASP (Active Server Pages) */
	".ascx", ".aspx", /* ASP */
	".c", ".c++", ".cc", ".cpp", ".cxx", ".h", ".h++", ".hh", ".hpp", ".hxx", /* C/C++ */
	".bas", ".cls", ".cs", ".cshtml", ".csproj", ".ctl", ".dsr", ".frm", ".sln", ".vb", ".vbp", ".vbs", ".xaml", /* C#, VB.NET, Visual Basic, VB Script */
	".go",                            /* Go, Protobuf */
	".groovy", ".gsh", ".gvy", ".gy", /* Groovy */
	".htm", ".html", /* HTML */
	".hbs", ".java", ".javasln", ".jsp", ".jspf", ".project", ".properties", ".tag", ".tld", /* Java ecosystem */
	".js", ".json", ".jsx", /* JavaScript */
	".kt", ".kts", /* Kotlin */
	".m", ".swift", ".xib", /* Objective C, Swift */
	".pl", ".plx", ".pm", ".psgi", /* Perl */
	".ctp", ".php", ".php3", ".php4", ".php5", ".php5.6", ".phtm", ".phtml", ".tpl", ".twig", /* php */
	".pck", ".pkb", ".pkh", ".pks", ".pls", ".sql", /* PL/SQL */
	".py", ".py2", ".py3", ".pyi", ".python", /* Python, Django */
	".erb", ".rb", ".rhtml", ".rjs", ".rxml", /* Ruby */
	".conf", ".sc", ".scala", /* Scala */
	".ts", ".tsx", /* Typescript/Angular */
	".build", ".cgi", ".config", ".inc", ".xml"} /* Others */

// Definition as var because a constant needs a fix array size.

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

// ActionExecuteMarkFalsePositives name of action to interactively define false-positives of a project and upload it to SecHub server
const ActionExecuteMarkFalsePositives = "markFalsePositives"

/* ---------------------------------- */
/* -------- Status ------------------ */
/* ---------------------------------- */

// ExecutionStateEnded sechub job has succesfully finished
const ExecutionStateEnded = "ENDED"
