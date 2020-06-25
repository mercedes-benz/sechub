// SPDX-License-Identifier: MIT
package cli

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"time"

	. "daimler.com/sechub/util"
)

type jobStatusResult struct {
	State        string `json:"state"`
	Result       string `json:"result"`
	TrafficLight string `json:"trafficLight"`
}

type jobScheduleResult struct {
	//    {
	//    "jobId": "a52e0695-5789-4902-9643-72d2ce138942"
	//}
	JobId string `json:"jobId"`
}

// Execute starts sechub client
func Execute() {
	initHelp()
	context := InitializeContext()

	printLogoWithVersion(os.Stdout)

	if context.config.trustAll {
		if !context.config.quiet {
			fmt.Println("WARNING: Configured to trust all - means unknown service certificate is accepted. Don't use this in production!")
		}
	}

	switch context.config.action {
	case ActionExecuteSynchron:
		{
			commonWayToApprove(context)
			waitForSecHubJobDoneAndFailOnTrafficLight(context)
			os.Exit(ExitCodeOK)
		}
	case ActionExecuteAsynchron:
		{
			commonWayToApprove(context)
			fmt.Println(context.config.secHubJobUUID)
			os.Exit(ExitCodeOK)
		}
	case ActionExecuteGetStatus:
		{
			state := getSecHubJobState(context, true, false, false)
			fmt.Println(state)
			os.Exit(ExitCodeOK)
		}
	case ActionExecuteGetReport:
		{
			downloadSechubReport(context)
			os.Exit(ExitCodeOK)
		}
	default:
		{
			fmt.Printf("Unknown action '%s'\n", context.config.action)
			os.Exit(ExitCodeIllegalAction)
		}
	}
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
 * 		Create Job, updates config with job id
 * --------------------------------------------------
 */
func createNewSecHubJob(context *Context) {
	fmt.Printf("- Creating new sechub job\n")
	response := sendWithDefaultHeader("POST", buildCreateNewSecHubJobAPICall(context), context)

	data, err := ioutil.ReadAll(response.Body)
	HandleError(err)

	var result jobScheduleResult
	jsonErr := json.Unmarshal(data, &result)
	HandleError(jsonErr)

	context.config.secHubJobUUID = result.JobId
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
	fmt.Printf("- Uploading source zip file\n")
	uploadSourceZipFile(context)
}

func handleCodeScan(context *Context) {
	/* currently we only provide filesystem - means zipping etc. */
	json := context.sechubConfig

	// build regexp list for source code file patterns
	json.CodeScan.SourceCodePatterns = append(json.CodeScan.SourceCodePatterns, DefaultZipAllowedFilePatterns...)

	// add default exclude patterns to exclude list
	if !ignoreDefaultExcludes {
		json.CodeScan.Excludes = append(json.CodeScan.Excludes, DefaultZipExcludeDirPatterns...)
	}

	amountOfFolders := len(json.CodeScan.FileSystem.Folders)
	LogDebug(context, fmt.Sprintf("handleCodeScan - folders=%s", json.CodeScan.FileSystem.Folders))
	LogDebug(context, fmt.Sprintf("handleCodeScan - excludes=%s", json.CodeScan.Excludes))
	LogDebug(context, fmt.Sprintf("handleCodeScan - SourceCodePatterns=%s", json.CodeScan.SourceCodePatterns))
	LogDebug(context, fmt.Sprintf("handleCodeScan - amount of folders found: %d", amountOfFolders))
	if amountOfFolders == 0 {
		/* nothing set, so no upload */
		return
	}
	context.sourceZipFileName = fmt.Sprintf("sourcecode-%s.zip", context.config.secHubJobUUID)

	/* compress all folders to one single zip file*/
	config := ZipConfig{
		Folders:            json.CodeScan.FileSystem.Folders,
		Excludes:           json.CodeScan.Excludes,
		SourceCodePatterns: json.CodeScan.SourceCodePatterns,
		Debug:              context.config.debug} // pass through debug flag
	err := ZipFolders(context.sourceZipFileName, &config)
	if err != nil {
		fmt.Printf("%s\n", err)
		fmt.Print("Exiting due to fatal error...\n")
		os.Remove(context.sourceZipFileName) // cleanup zip file
		os.Exit(ExitCodeFailed)
	}

	/* calculate checksum for zip file */
	context.sourceZipFileChecksum = CreateChecksum(context.sourceZipFileName)
}

/* --------------------------------------------------
 * 		Approve Job
 * --------------------------------------------------
 */
func approveSecHubJob(context *Context) {
	fmt.Printf("- Approve sechub job\n")
	response := sendWithDefaultHeader("PUT", buildApproveSecHubJobAPICall(context), context)

	_, err := ioutil.ReadAll(response.Body)
	HandleError(err)
}

func waitForSecHubJobDoneAndFailOnTrafficLight(context *Context) string {
	return getSecHubJobState(context, false, true, true)
}

func getSecHubJobState(context *Context, checkOnlyOnce bool, checkTrafficLight bool, downloadReport bool) string {
	fmt.Printf("- Waiting for job %s to be done", context.config.secHubJobUUID)
	//    {
	//    "jobUUID": "e21b13fc-591e-4abd-b119-755d473c5625",
	//    "owner": "developer",
	//    "created": "2018-03-06T12:59:59.691",
	//    "started": "2018-03-06T13:00:00.007",
	//    "ended": "2018-03-06T13:00:04.562",
	//    "state": "ENDED",
	//    "result": "OK",
	//    "trafficLight": "GREEN"
	//    }

	//	{
	//    "jobUUID": "a52e0695-5789-4902-9643-72d2ce138942",
	//    "owner": "developer",
	//    "created": "2018-03-08T23:08:54.014",
	//    "started": "2018-03-08T23:08:55.013",
	//    "ended": "2018-03-08T23:08:57.324",
	//    "state": "ENDED",
	//    "result": "FAILED",
	//    "trafficLight": ""
	//}

	done := false
	var status jobStatusResult

	newLine := true
	cursor := 0
	/* PROGRESS bar ... 80 chars with dot, then next line... */
	for {
		if newLine {
			fmt.Print("\n  ")
			newLine = false
		}
		done = checkOnlyOnce
		fmt.Print(".")
		cursor++
		if cursor == 80 {
			cursor = 0
			newLine = true
		}
		response := sendWithDefaultHeader("GET", buildGetSecHubJobStatusAPICall(context), context)

		data, err := ioutil.ReadAll(response.Body)
		HandleHTTPError(err)
		if context.config.debug {
			LogDebug(context, fmt.Sprintf("get job status :%s", string(data)))
		}

		/* transform text to json */
		err = json.Unmarshal(data, &status)
		HandleHTTPError(err)

		if status.State == ExecutionStateEnded {
			done = true
		}
		if done {
			if !checkTrafficLight {
				return string(data)
			}
			break
		} else {
			time.Sleep(time.Duration(context.config.waitNanoseconds))
		}
	}
	fmt.Print("\n")

	if downloadReport {
		downloadSechubReport(context)
	}

	/* FAIL mode */
	if status.TrafficLight == "" {
		fmt.Println("  No traffic light available! Seems job has been broken.")
		os.Exit(ExitCodeFailed)
	}
	if status.TrafficLight == "RED" {
		fmt.Println("  RED alert - security vulnerabilities identified (critical or high)")
		os.Exit(ExitCodeFailed)
	}
	if status.TrafficLight == "YELLOW" {
		fmt.Println("  YELLOW alert - security vulnerabilities identified (but not critical or high)")
		if context.config.stopOnYellow == true {
			os.Exit(ExitCodeFailed)
		} else {
			return ""
		}
	}
	if status.TrafficLight == "GREEN" {
		fmt.Println("  GREEN - no security vulnerabilities identified")
		return ""
	}
	fmt.Printf("UNKNOWN traffic light:%s\n", status.TrafficLight)
	os.Exit(ExitCodeFailed)
	return "" // dummy - will never happen

}

func downloadSechubReport(context *Context) string {
	fileEnding := ".json"
	if context.config.reportFormat == "html" {
		fileEnding = ".html"
	}
	fileName := "sechub_report_" + context.config.secHubJobUUID + fileEnding

	report := Report{serverResult: getSecHubJobReport(context), outputFolder: context.config.outputFolder, outputFileName: fileName}
	report.save(context)

	return "" // Dummy (Error handling is done in report.save method)
}

func getSecHubJobReport(context *Context) string {
	fmt.Printf("- Fetching result (format=%s) for job %s\n", context.config.reportFormat, context.config.secHubJobUUID)

	header := make(map[string]string)
	header["Content-Type"] = "application/json"

	if context.config.reportFormat == "html" {
		header["Accept"] = "text/html"
	} else {
		header["Accept"] = "application/json"
	}
	LogDebug(context, fmt.Sprintf("getSecHubJobReport: header=%s\n", header))
	response := sendWithHeader("GET", buildGetSecHubJobReportAPICall(context), context, header)

	data, err := ioutil.ReadAll(response.Body)
	HandleHTTPError(err)

	jsonString := string(data)
	if context.config.debug {
		LogDebug(context, fmt.Sprintf("get job report :%s", jsonString))
	}
	return jsonString
}
