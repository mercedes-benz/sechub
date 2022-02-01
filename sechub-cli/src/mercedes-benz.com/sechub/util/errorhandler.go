// SPDX-License-Identifier: MIT

package util

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
)

// HandleError - handler method for common errors
func HandleError(err error, exitCode int) {
	if err != nil {
		LogError(fmt.Sprintf("Error: %s\n", err))
		os.Exit(exitCode)
	}
}

// HandleHTTPError - handler method for http errors
func HandleHTTPError(err error, exitCode int) {
	if err != nil {
		LogError(fmt.Sprintf("The HTTP request failed with error '%s'", err))
		os.Exit(exitCode)
	}
}

// HandleHTTPResponse - handler method for http response. when not 200 an error log entry will be created and sechub client does exit
func HandleHTTPResponse(response *http.Response, exitCode int) {
	if response.StatusCode >= 400 { // StatusCode is 4xx or 5xx
		b, _ := ioutil.ReadAll(response.Body)
		LogError(fmt.Sprintf("The HTTP request failed with status code '%s'\nbody=%s\n", response.Status, string(b)))
		os.Exit(exitCode)
	}
}
