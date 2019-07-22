// SPDX-License-Identifier: MIT
package cli

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
)

func HandleHTTPError(err error) {
	if err != nil {
		LogError(fmt.Sprintf("The HTTP request failed with error %s\n", err))
		os.Exit(EXIT_CODE_HTTP_ERROR)
	}
}

func HandleError(err error) {
	if err != nil {
		LogError(fmt.Sprintf("Error: %s\n", err))
		os.Exit(EXIT_CODE_HTTP_ERROR)
	}
}
func HandleHTTPResponse(res *http.Response) {
	if res.StatusCode != 200 {
		b, _ := ioutil.ReadAll(res.Body)
		LogError(fmt.Sprintf("The HTTP request failed with error %s\nbody=%s\n", res.Status, string(b)))
		os.Exit(EXIT_CODE_HTTP_ERROR)
	}
}

func HandleHTTPErrorAndResponse(res *http.Response, err error) {
	HandleHTTPError(err)
	HandleHTTPResponse(res)
}
