// SPDX-License-Identifier: MIT
package cli

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"

	. "daimler.com/sechub/util"
)

type Report struct {
	outputFileName string
	outputFolder   string
	serverResult   []byte
}

func (report *Report) save(context *Context) {
	filePath := report.createFilePath(true)
	LogDebug(context.config.debug, fmt.Sprintf("Saving to filepath %q", filePath))

	content := append(report.serverResult, []byte("\n")...) // add newline to the end

	WriteContentToFile(filePath, content, context.config.reportFormat)

	fmt.Printf("  SecHub report written to %s\n", filePath)
}

func (report *Report) createFilePath(forceDirectory bool) string {
	path := report.outputFolder

	if forceDirectory {
		if _, err := os.Stat(path); os.IsNotExist(err) {
			os.MkdirAll(path, 0755)
		}
	}

	return filepath.Join(path, report.outputFileName)
}

func getSecHubJobReport(context *Context) []byte {
	fmt.Printf("- Fetching result (format=%s) for job %s\n", context.config.reportFormat, context.config.secHubJobUUID)

	header := make(map[string]string)
	header["Content-Type"] = "application/json"

	if context.config.reportFormat == "html" {
		header["Accept"] = "text/html"
	} else {
		header["Accept"] = "application/json"
	}
	LogDebug(context.config.debug, fmt.Sprintf("getSecHubJobReport: header=%q", header))
	response := sendWithHeader("GET", buildGetSecHubJobReportAPICall(context), context, header)

	data, err := ioutil.ReadAll(response.Body)
	HandleHTTPError(err)

	LogDebug(context.config.debug, fmt.Sprintf("SecHub job report: %s", string(data)))
	return data
}
