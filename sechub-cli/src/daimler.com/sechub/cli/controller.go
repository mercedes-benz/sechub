// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"

	sechubUtil "daimler.com/sechub/util"
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
		commonWayToApprove(context)
		waitForSecHubJobDoneAndFailOnTrafficLight(context)
	case scanAsynchronAction:
		commonWayToApprove(context)
		fmt.Println(context.config.secHubJobUUID)
	case getStatusAction:
		fmt.Println(getSecHubJobState(context, true, false, false))
	case getReportAction:
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
func commonWayToApprove(context *Context) {
	createNewSecHubJob(context)
	handleCodeScanParts(context)
	approveSecHubJob(context)
}

/* --------------------------------------------------
 * 		Handle code scan parts
 * --------------------------------------------------
 */
func handleCodeScanParts(context *Context) {
	handleCodeScan(context)
	if !context.isUploadingSourceZip() {
		return
	}
	sechubUtil.Log("Uploading source zip file", context.config.quiet)
	uploadSourceZipFile(context)
}

func handleCodeScan(context *Context) {
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
		sechubUtil.LogDebug(debug, fmt.Sprintf("handleCodeScan - folders=%s", json.CodeScan.FileSystem.Folders))
		sechubUtil.LogDebug(debug, fmt.Sprintf("handleCodeScan - excludes=%s", json.CodeScan.Excludes))
		sechubUtil.LogDebug(debug, fmt.Sprintf("handleCodeScan - SourceCodePatterns=%s", json.CodeScan.SourceCodePatterns))
		sechubUtil.LogDebug(debug, fmt.Sprintf("handleCodeScan - amount of folders found: %d", amountOfFolders))
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

func downloadSechubReport(context *Context) string {
	fileEnding := ".json"
	if context.config.reportFormat == "html" {
		fileEnding = ".html"
	}
	fileName := "sechub_report_" + context.config.projectID + "_" + context.config.secHubJobUUID + fileEnding

	report := ReportDownload{serverResult: getSecHubJobReport(context), outputFolder: context.config.outputFolder, outputFileName: fileName}
	report.save(context)

	return "" // Dummy (Error handling is done in report.save method)
}

func downloadFalsePositivesList(context *Context) {
	fileName := "sechub-false-positives-" + context.config.projectID + ".json"

	list := FalsePositivesList{serverResult: getFalsePositivesList(context), outputFolder: context.config.outputFolder, outputFileName: fileName}
	list.save(context)
}
