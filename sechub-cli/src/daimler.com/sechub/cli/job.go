// SPDX-License-Identifier: MIT

package cli

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"time"

	sechubUtil "daimler.com/sechub/util"
)

/* --------------------------------------------------
 * 		Create Job, updates config with job id
 * --------------------------------------------------
 */
func createNewSecHubJob(context *Context) {
	sechubUtil.Log("Creating new sechub job")
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
	sechubUtil.Log("Approve sechub job")
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
		sechubUtil.Log(fmt.Sprintf("Waiting for job %s to be done", context.config.secHubJobUUID))
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
				fmt.Print("                             ")
				newLine = false
			}
			fmt.Print(".")
			if cursor++; cursor == 50 {
				fmt.Println()
				cursor = 0
				newLine = true
			}
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
