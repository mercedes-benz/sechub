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
	var cursor uint8 = 0
	waitNanoseconds := context.config.initialWaitIntervalNanoseconds

	sechubUtil.Log(fmt.Sprintf("Waiting for job %s to be done", context.config.secHubJobUUID), context.config.quiet)

	for {
		getSecHubJobStatus(context)

		if context.jobStatus.State == ExecutionStateEnded {
			break
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
	case "OFF":
		fmt.Println("  SCAN FAILED - detection of security vulnerabilities failed on server or product")
		os.Exit(ExitCodeFailed)
	case "":
		sechubUtil.LogError("No traffic light available! Please check messages and server logs.")
		os.Exit(ExitCodeFailed)
	default:
		sechubUtil.LogError(fmt.Sprintln("UNKNOWN traffic light:", context.jobStatus.TrafficLight, "- Expected one of: RED, YELLOW, GREEN."))
		os.Exit(ExitCodeFailed)
	}
}
