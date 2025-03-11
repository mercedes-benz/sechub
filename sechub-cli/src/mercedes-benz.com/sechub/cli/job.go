// SPDX-License-Identifier: MIT

package cli

import (
	"encoding/json"
	"fmt"
	"io"
	"os"
	"strings"
	"time"

	sechubUtil "mercedes-benz.com/sechub/util"
)

/* --------------------------------------------------
 * 		Create Job, updates config with job id
 * --------------------------------------------------
 */
func createNewSecHubJob(context *Context) {
	response := sendWithDefaultHeader("POST", buildCreateNewSecHubJobAPICall(context), context)

	data, err := io.ReadAll(response.Body)
	sechubUtil.HandleError(err, ExitCodeFailed)

	var result jobScheduleResult
	jsonErr := json.Unmarshal(data, &result)
	sechubUtil.HandleError(jsonErr, ExitCodeFailed)

	context.config.secHubJobUUID = result.JobID
	sechubUtil.Log("Creating new SecHub job: "+context.config.secHubJobUUID, context.config.quiet)
}

// approveSecHubJob - Approve Job
func approveSecHubJob(context *Context) {
	sechubUtil.Log("Approve sechub job", context.config.quiet)
	response := sendWithDefaultHeader("PUT", buildApproveSecHubJobAPICall(context), context)

	_, err := io.ReadAll(response.Body)
	sechubUtil.HandleError(err, ExitCodeFailed)
}

func waitForSecHubJobDone(context *Context) (status jobStatusResult) {
	var cursor uint8 = 0
	waitNanoseconds := context.config.initialWaitIntervalNanoseconds

	sechubUtil.Log(fmt.Sprintf("Waiting for job %s to be done", context.config.secHubJobUUID), context.config.quiet)

	for {
		getSecHubJobStatus(context)

		// Check if job has finished
		if context.jobStatus.State == ExecutionStateEnded {
			break
		}
		// Exit if job has been canceled on SecHub server
		if context.jobStatus.State == ExecutionStateCanceled || context.jobStatus.State == ExecutionStateCancelRequested {
			sechubUtil.PrintIfNotSilent("\n", context.config.quiet)
			sechubUtil.LogError("Job " + context.config.secHubJobUUID + " has been canceled on SecHub server.")
			os.Exit(ExitCodeCanceled)
		}

		if !context.config.quiet {
			// Progress dots
			cursor = printProgressDot(cursor)
		}

		time.Sleep(time.Duration(waitNanoseconds))
		waitNanoseconds = computeNextWaitInterval(waitNanoseconds, context.config.waitNanoseconds)
	}
	sechubUtil.PrintIfNotSilent("\n", context.config.quiet)

	return status
}

// Print progress dots ... 50 dots, then next line...
func printProgressDot(cursor uint8) uint8 {
	if cursor == 0 {
		// initial identation
		fmt.Print(strings.Repeat(" ", 29))
	}

	fmt.Print(".")

	cursor++
	if cursor == 50 {
		fmt.Print("\n")
		cursor = 0
	}

	return cursor
}

func computeNextWaitInterval(current int64, max int64) int64 {
	next := int64(float64(current) * WaitIntervalIncreaseFactor)
	if next > max {
		return max
	}
	return next
}

func getSecHubJobStatus(context *Context) (jsonData string) {
	// request SecHub job state from server
	response := sendWithDefaultHeader("GET", buildGetSecHubJobStatusAPICall(context), context)

	data, err := io.ReadAll(response.Body)
	sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)
	if context.config.debug {
		sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Get job status :%s", string(data)))
	}

	/* transform text to json */
	err = json.Unmarshal(data, context.jobStatus)
	sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)

	return string(data)
}

func printSecHubJobSummaryAndFailOnTrafficLight(context *Context) {
	// Handle messages from server
	errorInJobMessage := false
	numberOfMessages := len(context.jobStatus.Messages)

	if numberOfMessages > 0 {
		fmt.Print("Message")
		if numberOfMessages > 1 {
			fmt.Print("s")
		}
		fmt.Println(" from SecHub server:")
		for _, message := range context.jobStatus.Messages {
			fmt.Printf("  -> %s: %s\n", message.Type, message.Text)
			if message.Type == "ERROR" {
				errorInJobMessage = true
			}
		}
	}

	// Evaluate traffic light
	switch context.jobStatus.TrafficLight {
	case "RED":
		if errorInJobMessage {
			fmt.Fprintln(os.Stderr, "  RED alert - server error while job execution")
		} else {
			fmt.Fprintln(os.Stderr, "  RED alert - security vulnerabilities identified (critical or high)")
		}
		os.Exit(ExitCodeFailed)
	case "YELLOW":
		yellowMessage := "  YELLOW alert - security vulnerabilities identified (but not critical or high)"
		if context.config.stopOnYellow {
			fmt.Fprintln(os.Stderr, yellowMessage)
			os.Exit(ExitCodeFailed)
		} else {
			fmt.Println(yellowMessage)
		}
	case "GREEN":
		fmt.Println("  GREEN - no severe security vulnerabilities identified")
	case "OFF", "":
		sechubUtil.LogError("No traffic light available! Please check messages and server logs.")
		os.Exit(ExitCodeFailed)
	default:
		sechubUtil.LogError(fmt.Sprintln("UNKNOWN traffic light:", context.jobStatus.TrafficLight, "- Expected one of: RED, YELLOW, GREEN."))
		os.Exit(ExitCodeFailed)
	}
}

func getSecHubJobList(context *Context, size int) {
	// Print filtering labels if defined
	for key, value := range context.config.labels {
		sechubUtil.LogNotice("Filtered by label "+key+"="+value)
	}	

	// Request SecHub job list from server
	response := sendWithDefaultHeader("GET", buildGetSecHubJobListAPICall(context, size), context)

	data, err := io.ReadAll(response.Body)
	sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)
	if context.config.debug {
		sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Get job list: %s", string(data)))
	}

	/* transform json result into context.jobList */
	err = json.Unmarshal(data, context.jobList)
	sechubUtil.HandleError(err, ExitCodeFailed)
}

func latestUUIDFromJobList(context *Context, filter string) (uuid string) {
	if filter == "" {
		uuid = context.jobList.List[0].JobUUID
	} else {
		for _, item := range context.jobList.List {
			if item.ExecutionState == filter {
				uuid = item.JobUUID
				break
			}
		}
	}
	return uuid
}

func getLatestSecHubJobUUID(context *Context, expectedState ...string) string {
	// get latest 5 entries into context.jobList
	getSecHubJobList(context, 5)

	if len(context.jobList.List) == 0 {
		sechubUtil.LogWarning("No SecHub jobs found for "+context.config.projectID+". Have you started a scan?")
		// Return 0 because we do not regard this as an error
		os.Exit(ExitCodeOK)
	}

	// If not provided: accept any job state
	stateFilter := ""
	if len(expectedState) > 0 {
		stateFilter = expectedState[0]
	}

	return latestUUIDFromJobList(context, stateFilter)
}

func printLatestJobsOfProject(context *Context) {
	// get latest jobs into context.jobList
	getSecHubJobList(context, SizeOfJobList)

	// Print result table
	printFormat := "%-36s | %-6s | %-8s | %-6s | %-19s | %-19s\n"
	fmt.Printf(printFormat, "SecHub JobUUID", "Status", "Stage", "Result", "Created", "Ended")
	fmt.Println("-------------------------------------+--------+----------+--------+---------------------+--------------------")
	for _, item := range context.jobList.List {
		// Create reasonable job status strings
		jobStatus := item.ExecutionState
		if strings.HasPrefix(item.ExecutionState, "READY") {
			jobStatus = "WAITING"
		} else if strings.HasPrefix(item.ExecutionState, "CANCEL") {
			jobStatus = "CANCELED"
		}

		// Cut some of the results to keep the table's format (needs some padding with blanks in case the strings are smaller)
		fmt.Printf(printFormat,
			item.JobUUID,
			item.TrafficLight,
			(jobStatus + "        ")[0:8],
			item.ExecutionResult,
			(item.Created + "                   ")[0:19],
			(item.Ended + "                   ")[0:19])
	}
}

func cancelSecHubJob(context *Context) {
	// request cancel of scan job on SecHub server
	response := sendWithDefaultHeader("POST", buildCancelSecHubJobAPICall(context), context)

	data, err := io.ReadAll(response.Body)
	sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)
	if context.config.debug {
		sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Cancel job status :%s", string(data)))
	}
}