// SPDX-License-Identifier: MIT

package util

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
)

// HandleError - handler method for common errors
func HandleError(err error, exitcode int) {
	if err != nil {
		LogError(fmt.Sprintf("Error: %s\n", err))
		os.Exit(exitcode)
	}
}

// HandleHTTPError - handler method for http errors
func HandleHTTPError(err error, exitcode int) {
	if err != nil {
		LogError(fmt.Sprintf("The HTTP request failed with error '%s'", err))
		os.Exit(exitcode)
	}
}

// HandleHTTPResponse - handler method for http response. when not 200 an error log entry will be created and sechub client does exit
func HandleHTTPResponse(response *http.Response, exitcode int) {
	if response.StatusCode != 200 {
		b, _ := ioutil.ReadAll(response.Body)
		LogError(fmt.Sprintf("The HTTP request failed with status code '%s'\nbody=%s\n", response.Status, string(b)))
		os.Exit(exitcode)
	}
}
