// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"

	sechubUtil "mercedes-benz.com/sechub/util"
)

type jobStatusResult struct {
	State        string `json:"state"`
	Result       string `json:"result"`
	TrafficLight string `json:"trafficLight"`
}

type jobScheduleResult struct {
	JobID string `json:"jobId"`
}

// Execute starts sechub client
func Execute() {

	context := InitializeContext()

	printLogoWithVersion(context)

	switch context.config.action {
	case scanAction:
		prepareCreateApproveJob(context)
		waitForSecHubJobDone(context)
		downloadSechubReport(context)
		printSecHubJobSummaryAndFailOnTrafficLight(context)
	case scanAsynchronAction:
		prepareCreateApproveJob(context)
		fmt.Println(context.config.secHubJobUUID)
	case getStatusAction:
		jsonData := getSecHubJobStatus(context)
		fmt.Println(jsonData)
	case getReportAction:
		getSecHubJobStatus(context)
		downloadSechubReport(context)
	case getFalsePositivesAction:
		downloadFalsePositivesList(context)
	case markFalsePositivesAction:
		uploadFalsePositivesFromFile(context)
	case interactiveMarkFalsePositivesAction:
		interactiveMarkFalsePositives(context)
	case unmarkFalsePositivesAction:
		unmarkFalsePositivesFromFile(context)
	case interactiveUnmarkFalsePositivesAction:
		interactiveUnmarkFalsePositives(context)
	case showHelpAction:
		PrintUsage(os.Stdout)
	case showVersionAction:
		if context.config.quiet {
			// Print ONLY in quiet mode, because otherwise the version is already printed along with the banner
			fmt.Println(Version())
		}
		// We show the version every time - so nothing more to do here
	default:
		fmt.Printf("Unknown action '%s'\n", context.config.action)
		showHelpHint()
		os.Exit(ExitCodeIllegalAction)
	}
	os.Exit(ExitCodeOK)
}

/* --------------------------------------------------
 * 		Common way until approve: create job, handle
 *      code scan parts, do approve
 * --------------------------------------------------*/
func prepareCreateApproveJob(context *Context) {
	prepareCodeScan(context)
	createNewSecHubJob(context)
	handleCodeScanUpload(context)
	approveSecHubJob(context)
}

/* --------------------------------------------------
 * 		Handle code scan parts
 * --------------------------------------------------
 */
func handleCodeScanUpload(context *Context) {
	if !context.isUploadingSourceZip() {
		return
	}
	sechubUtil.Log("Uploading source zip file", context.config.quiet)
	uploadSourceZipFile(context)
}

func prepareCodeScan(context *Context) {
	/* currently we only provide filesystem - means zipping etc. */
	json := context.sechubConfig

	// build regexp list for source code file patterns
	json.CodeScan.SourceCodePatterns = append(json.CodeScan.SourceCodePatterns, DefaultZipAllowedFilePatterns...)

	// add default exclude patterns to exclude list
	if !context.config.ignoreDefaultExcludes {
		json.CodeScan.Excludes = append(json.CodeScan.Excludes, DefaultZipExcludeDirPatterns...)
	}

	amountOfFolders := len(json.CodeScan.FileSystem.Folders)
	var debug = context.config.debug
	if debug {
		sechubUtil.LogDebug(debug, fmt.Sprintf("prepareCodeScan - folders=%s", json.CodeScan.FileSystem.Folders))
		sechubUtil.LogDebug(debug, fmt.Sprintf("prepareCodeScan - excludes=%s", json.CodeScan.Excludes))
		sechubUtil.LogDebug(debug, fmt.Sprintf("prepareCodeScan - SourceCodePatterns=%s", json.CodeScan.SourceCodePatterns))
		sechubUtil.LogDebug(debug, fmt.Sprintf("prepareCodeScan - amount of folders found: %d", amountOfFolders))
	}
	if amountOfFolders == 0 {
		/* nothing set, so no upload */
		return
	}
	context.sourceZipFileName = fmt.Sprintf("sourcecode-%s.zip", context.config.secHubJobUUID)

	/* compress all folders to one single zip file*/
	config := sechubUtil.ZipConfig{
		Folders:            json.CodeScan.FileSystem.Folders,
		Excludes:           json.CodeScan.Excludes,
		SourceCodePatterns: json.CodeScan.SourceCodePatterns,
		Debug:              context.config.debug} // pass through debug flag
	err := sechubUtil.ZipFolders(context.sourceZipFileName, &config, context.config.quiet)
	if err != nil {
		sechubUtil.LogError(fmt.Sprintf("%s\nExiting due to fatal error...\n", err))
		os.Remove(context.sourceZipFileName) // cleanup zip file
		os.Exit(ExitCodeFailed)
	}

	/* calculate checksum for zip file */
	context.sourceZipFileChecksum = sechubUtil.CreateChecksum(context.sourceZipFileName)
}

func downloadSechubReport(context *Context) {
	if context.jobStatus.Result != JobStatusOkay {
		sechubUtil.LogError("Job " + context.config.secHubJobUUID + " failed on server. Cannot download report.")
		os.Exit(ExitCodeFailed)
	}

	fileEnding := "." + context.config.reportFormat // e.g. .json, .html
	fileName := "sechub_report_" + context.config.projectID + "_" + context.config.secHubJobUUID + fileEnding

	report := ReportDownload{serverResult: getSecHubJobReport(context), outputFolder: context.config.outputFolder, outputFileName: fileName}
	report.save(context)
}

func downloadFalsePositivesList(context *Context) {
	fileName := "sechub-false-positives-" + context.config.projectID + ".json"

	list := FalsePositivesList{serverResult: getFalsePositivesList(context), outputFolder: context.config.outputFolder, outputFileName: fileName}
	list.save(context)
}
