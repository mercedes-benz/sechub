// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"

	sechubUtil "mercedes-benz.com/sechub/util"
)

type jobStatusResult struct {
	State        string             `json:"state"`
	Result       string             `json:"result"`
	TrafficLight string             `json:"trafficLight"`
	Messages     []JobStatusMessage `json:"messages"`
}

type JobStatusMessage struct {
	Type string `json:"type"`
	Text string `json:"text"`
}

type jobListResult struct {
	List []JobListEntry `json:"content"`
}

type JobListEntry struct {
	JobUUID         string `json:"jobUUID"`
	ExecutedBy      string `json:"executedBy"`
	Created         string `json:"created"`
	Started         string `json:"started"`
	Ended           string `json:"ended"`
	ExecutionState  string `json:"executionState"`
	ExecutionResult string `json:"executionResult"`
	TrafficLight    string `json:"trafficLight"`
}

type jobScheduleResult struct {
	JobID string `json:"jobId"`
}

// Execute starts sechub client
func Execute() {

	context := InitializeContext()

	initSignalHandler(context)

	switch context.config.action {
	case scanAction:
		prepareCreateApproveJob(context)
		waitForSecHubJobDone(context)
		downloadSechubReport(context)
		printSecHubJobSummaryAndFailOnTrafficLight(context)
	case scanAsynchronAction:
		prepareCreateApproveJob(context)
		fmt.Println(context.config.secHubJobUUID)
	case cancelAction:
		cancelSecHubJob(context)
	case getStatusAction:
		jsonData := getSecHubJobStatus(context)
		fmt.Println(jsonData)
	case getReportAction:
		getSecHubJobStatus(context)
		downloadSechubReport(context)
	case listJobsAction:
		printLatestJobsOfProject(context)
	case getFalsePositivesAction:
		downloadFalsePositivesList(context)
	case defineFalsePositivesAction:
		defineFalsePositivesFromFile(context)
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
	if ! verifySecHubConfig(context) {
		os.Exit(ExitCodeInvalidConfigFile)
	}
	prepareScan(context)
	createNewSecHubJob(context)
	handleUploads(context)
	approveSecHubJob(context)
}

func verifySecHubConfig(context *Context) (result bool) {
	result = true

	// Check all data.sources entries
	for _, data_source := range context.sechubConfig.Data.Sources {
		for _, forbidden_name := range forbiddenArchiveDataSectionNames {
			if data_source.Name == forbidden_name {
				sechubUtil.LogError("SecHub config file contains reserved name '" + forbidden_name + "'. Please choose another!")
				result = false
			}
		}
	}

	// Check all data.binaries entries
	for _, data_binary := range context.sechubConfig.Data.Binaries {
		for _, forbidden_name := range forbiddenArchiveDataSectionNames {
			if data_binary.Name == forbidden_name {
				sechubUtil.LogError("SecHub config file contains reserved name '" + forbidden_name + "'. Please choose another!")
				result = false
			}
		}
	}

	return result
}

/* --------------------------------------------------
 * 		Handle code scan parts
 * --------------------------------------------------
 */
func handleUploads(context *Context) {
	if context.sourceZipUploadNeeded {
		sechubUtil.Log("Uploading sources zip file", context.config.quiet)
		uploadSourceZipFile(context)
	}
	if context.binariesTarUploadNeeded {
		sechubUtil.Log("Uploading binaries tar archive", context.config.quiet)
		uploadBinariesTarFile(context)
	}
}

func prepareScan(context *Context) {

	if len(context.sechubConfig.Data.Sources) > 0 || len(context.sechubConfig.CodeScan.FileSystem.Folders) > 0 {
		//////////////////////////////
		// Creating sources ZIP file
		context.sourceZipFileName = tempFile(context, fmt.Sprintf("sourcecode-%s.zip", context.config.projectID))

		// Set sources filter patterns in
		// - data.sources
		// - codeScan
		// depending on
		// - scan type
		//   - codeScan -> DefaultSourceCodeAllowedFilePatterns
		//   - secretScan -> everything but blacklisted
		// - context.config.whitelistAll (deactivates all filters)
		adjustSourceFilterPatterns(context)

		err := createSouceCodeZipFile(context)
		if err != nil {
			sechubUtil.LogError(fmt.Sprintf("%s\nExiting due to fatal error while creating sources zip file...\n", err))
			os.Remove(context.sourceZipFileName) // cleanup zip file
			os.Exit(ExitCodeFailed)
		} else if context.sourceZipUploadNeeded {
			// calculate checksum for zip file
			context.sourceZipFileChecksum = sechubUtil.CreateChecksum(context.sourceZipFileName)
		}
	} else {
		context.sourceZipUploadNeeded = false
	}

	if len(context.sechubConfig.Data.Binaries) > 0 {
		//////////////////////////////
		// Creating binaries TAR file
		context.binariesTarFileName = tempFile(context, fmt.Sprintf("binaries-%s.tar", context.config.projectID))

		err := createBinariesTarFile(context)
		if err != nil {
			sechubUtil.LogError(fmt.Sprintf("%s\nExiting due to fatal error while creating binaries tar file...\n", err))
			os.Remove(context.binariesTarFileName) // cleanup tar file
			os.Exit(ExitCodeFailed)
		} else if context.binariesTarUploadNeeded {
			// calculate checksum for tar file
			context.binariesTarFileChecksum = sechubUtil.CreateChecksum(context.binariesTarFileName)
		}
	} else {
		context.binariesTarUploadNeeded = false
	}
}

func downloadSechubReport(context *Context) {
	if context.jobStatus.Result != JobStatusOkay {
		sechubUtil.LogError("Job " + context.config.secHubJobUUID + " failed on server. Cannot download report.")
		os.Exit(ExitCodeFailed)
	}

	fileName := context.config.outputFileName
	if fileName == "" {
		// Use default report file name if not yet defined
		fileExtension := ""
		switch context.config.reportFormat {
		case ReportFormatHTML:
			fileExtension = ".html"
		case ReportFormatJSON:
			fileExtension = ".json"
		case ReportFormatSPDXJSON:
			fileExtension = ".spdx.json"
		}
		// Example:  sechub_report_myproject_cdde8927-2df4-461c-b775-2dec9497e8b1.json
		fileName = "sechub_report_" + context.config.projectID + "_" + context.config.secHubJobUUID + fileExtension
	}

	report := ReportDownload{
		serverResult:   getSecHubJobReport(context),
		outputFolder:   context.config.outputFolder,
		outputFileName: fileName,
	}
	report.save(context)
}

func downloadFalsePositivesList(context *Context) {
	fileName := context.config.outputFileName
	if fileName == "" {
		// Example: sechub-false-positives-myproject.json
		fileName = "sechub-false-positives-" + context.config.projectID + ".json"
	}

	list := FalsePositivesList{serverResult: getFalsePositivesList(context), outputFolder: context.config.outputFolder, outputFileName: fileName}
	list.save(context)
}
