// SPDX-License-Identifier: MIT

package cli

import (
	"archive/zip"
	"fmt"
	"os"
	"path/filepath"

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
	prepareScan(context)
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

func prepareScan(context *Context) {

	//////////////////////////////
	// Creating sources ZIP file
	context.sourceZipFileName = tempFile(context, fmt.Sprintf("sourcecode-%s.zip", context.config.projectID))

	// Set source code patterns in
	// - data.sources
	// - codeScan
	// depending on
	// - DefaultSourceCodeAllowedFilePatterns
	// - context.config.whitelistAll (deactivates all filters)
	adjustSourceCodePatterns(context)

	err := createSouceCodeZipFile(context)
	if err != nil {
		sechubUtil.LogError(fmt.Sprintf("%s\nExiting due to fatal error while creating sources zip file...\n", err))
		os.Remove(context.sourceZipFileName) // cleanup zip file
		os.Exit(ExitCodeFailed)
	}

	// calculate checksum for zip file
	context.sourceZipFileChecksum = sechubUtil.CreateChecksum(context.sourceZipFileName)

	//////////////////////////////
	// Creating binaries TAR file

	// ToDo
}

func downloadSechubReport(context *Context) {
	if context.jobStatus.Result != JobStatusOkay {
		sechubUtil.LogError("Job " + context.config.secHubJobUUID + " failed on server. Cannot download report.")
		os.Exit(ExitCodeFailed)
	}

	fileName := context.config.outputFileName
	if fileName == "" {
		// Example:  sechub_report_myproject_cdde8927-2df4-461c-b775-2dec9497e8b1.json
		fileName = "sechub_report_" + context.config.projectID + "_" + context.config.secHubJobUUID + "." + context.config.reportFormat
	}

	report := ReportDownload{serverResult: getSecHubJobReport(context), outputFolder: context.config.outputFolder, outputFileName: fileName}
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

// createSouceCodeZipFile - compress all defined sources into one single zip file
func createSouceCodeZipFile(context *Context) error {
	zipFile, _ := filepath.Abs(context.sourceZipFileName)

	// create zip file
	newZipFile, err := os.Create(zipFile)
	if err != nil {
		return err
	}
	defer newZipFile.Close()

	// create zip writer
	zipWriter := zip.NewWriter(newZipFile)
	defer zipWriter.Close()

	// Support legacy definition:
	if len(context.sechubConfig.CodeScan.FileSystem.Folders) > 0 {
		namedCodeScanConfig := NamedCodeScanConfig{
			Name:               "",
			FileSystem:         context.sechubConfig.CodeScan.FileSystem,
			Excludes:           context.sechubConfig.CodeScan.Excludes,
			SourceCodePatterns: context.sechubConfig.CodeScan.SourceCodePatterns,
		}
		err = appendToSourceCodeZipFile(zipFile, zipWriter, namedCodeScanConfig, context.config.quiet, context.config.debug)
		if err != nil {
			return err
		}
	}

	// ToDo: Support data section

	return nil
}

func appendToSourceCodeZipFile(zipFile string, zipWriter *zip.Writer, config NamedCodeScanConfig, quiet bool, debug bool) error {
	prefix := ""
	if config.Name != "" {
		prefix = fmt.Sprintf("__data__/%s/", config.Name)
	}
	zipConfig := sechubUtil.ZipConfig{
		ZipFileName:        zipFile,
		ZipWriter:          zipWriter,
		PrefixInZip:        prefix,
		Files:              config.FileSystem.Files,
		Folders:            config.FileSystem.Folders,
		Excludes:           config.Excludes,
		SourceCodePatterns: config.SourceCodePatterns,
		Quiet:              quiet,
		Debug:              debug,
	}

	amountOfFolders := len(config.FileSystem.Folders)

	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToSourceCodeZipFile - %d folders defined: %+v", amountOfFolders, config.FileSystem.Folders))
	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToSourceCodeZipFile - Excludes: %+v", config.Excludes))
	sechubUtil.LogDebug(debug, fmt.Sprintf("appendToSourceCodeZipFile - SourceCodePatterns: %+v", config.SourceCodePatterns))

	if amountOfFolders == 0 { // nothing defined, so nothing to do
		return nil
	}

	return sechubUtil.Zip(&zipConfig)
}
