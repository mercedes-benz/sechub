// SPDX-License-Identifier: MIT

package cli

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"strings"
	"time"

	sechubUtil "daimler.com/sechub/util"
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

func waitForSecHubJobDoneAndFailOnTrafficLight(context *Context) string {
	return getSecHubJobState(context, false, true, true)
}

func getSecHubJobState(context *Context, checkOnlyOnce bool, checkTrafficLight bool, downloadReport bool) string {
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

	if checkOnlyOnce {
		done = true
	} else {
		sechubUtil.Log(fmt.Sprintf("Waiting for job %s to be done", context.config.secHubJobUUID), context.config.quiet)
	}

	for {
		// request SecHub job state from server
		response := sendWithDefaultHeader("GET", buildGetSecHubJobStatusAPICall(context), context)

		data, err := ioutil.ReadAll(response.Body)
		sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)
		if context.config.debug {
			sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("get job status :%s", string(data)))
		}

		/* transform text to json */
		err = json.Unmarshal(data, &status)
		sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)

		if status.State == ExecutionStateEnded {
			done = true
		}
		if done {
			if !checkTrafficLight {
				return string(data)
			}
			break
		} else {
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
	}
	sechubUtil.PrintIfNotSilent("\n", context.config.quiet)

	if downloadReport {
		downloadSechubReport(context)
	}

	/* Evaluate traffic light */
	switch status.TrafficLight {
	case "RED":
		fmt.Fprintln(os.Stderr, "  RED alert - security vulnerabilities identified (critical or high)")
		os.Exit(ExitCodeFailed)
	case "YELLOW":
		yellowMessage := "  YELLOW alert - security vulnerabilities identified (but not critical or high)"
		if context.config.stopOnYellow == true {
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
		sechubUtil.LogError(fmt.Sprintln("UNKNOWN traffic light:", status.TrafficLight, "- Expected one of: RED, YELLOW, GREEN."))
		os.Exit(ExitCodeFailed)
	}
	return ""
}
