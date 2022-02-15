// SPDX-License-Identifier: MIT

package cli

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
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
	sechubUtil.Log("Creating new sechub job", context.config.quiet)
	response := sendWithDefaultHeader("POST", buildCreateNewSecHubJobAPICall(context), context)

	data, err := ioutil.ReadAll(response.Body)
	sechubUtil.HandleError(err, ExitCodeFailed)

	var result jobScheduleResult
	jsonErr := json.Unmarshal(data, &result)
	sechubUtil.HandleError(jsonErr, ExitCodeFailed)

	context.config.secHubJobUUID = result.JobID
}

// approveSecHubJob - Approve Job
func approveSecHubJob(context *Context) {
	sechubUtil.Log("Approve sechub job", context.config.quiet)
	response := sendWithDefaultHeader("PUT", buildApproveSecHubJobAPICall(context), context)

	_, err := ioutil.ReadAll(response.Body)
	sechubUtil.HandleError(err, ExitCodeFailed)
}

func waitForSecHubJobDone(context *Context) (status jobStatusResult) {
	newLine := true
	cursor := 0

	sechubUtil.Log(fmt.Sprintf("Waiting for job %s to be done", context.config.secHubJobUUID), context.config.quiet)

	for {
		getSecHubJobStatus(context)

		if context.jobStatus.State == ExecutionStateEnded {
			break
		}

		// PROGRESS bar ... 50 chars with dot, then next line...
		if newLine {
			sechubUtil.PrintIfNotSilent(strings.Repeat(" ", 29), context.config.quiet)
			newLine = false
		}
		sechubUtil.PrintIfNotSilent(".", context.config.quiet)
		if cursor++; cursor == 50 {
			sechubUtil.PrintIfNotSilent("\n", context.config.quiet)
			cursor = 0
			newLine = true
		}
		time.Sleep(time.Duration(context.config.waitNanoseconds))
	}
	sechubUtil.PrintIfNotSilent("\n", context.config.quiet)

	return status
}

func getSecHubJobStatus(context *Context) (jsonData string) {
	// request SecHub job state from server
	response := sendWithDefaultHeader("GET", buildGetSecHubJobStatusAPICall(context), context)

	data, err := ioutil.ReadAll(response.Body)
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
	/* Evaluate traffic light */
	switch context.jobStatus.TrafficLight {
	case "RED":
		fmt.Fprintln(os.Stderr, "  RED alert - security vulnerabilities identified (critical or high)")
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
	case "":
		sechubUtil.LogError("No traffic light available! Please check server logs.")
		os.Exit(ExitCodeFailed)
	default:
		sechubUtil.LogError(fmt.Sprintln("UNKNOWN traffic light:", context.jobStatus.TrafficLight, "- Expected one of: RED, YELLOW, GREEN."))
		os.Exit(ExitCodeFailed)
	}
}
