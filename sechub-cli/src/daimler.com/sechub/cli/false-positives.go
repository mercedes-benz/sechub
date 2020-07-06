// SPDX-License-Identifier: MIT
package cli

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"

	. "daimler.com/sechub/util"
)

// FalsePositivesList - structure for handling download of false-positive lists
type FalsePositivesList struct {
	serverResult   []byte
	outputFolder   string
	outputFileName string
}

func (list *FalsePositivesList) save(context *Context) {
	filePath := list.createFilePath(false)
	LogDebug(context.config.debug, fmt.Sprintf("Saving to filepath %q", filePath))

	content := append(list.serverResult, []byte("\n")...) // add newline to the end

	WriteContentToFile(filePath, content, "json")

	fmt.Printf("  SecHub false-positives list of project %q written to %s\n", context.config.projectId, filePath)
}

func (list *FalsePositivesList) createFilePath(forceDirectory bool) string {
	path := list.outputFolder

	if forceDirectory {
		if _, err := os.Stat(path); os.IsNotExist(err) {
			os.MkdirAll(path, 0755)
		}
	}

	return filepath.Join(path, list.outputFileName)
}

func getFalsePositivesList(context *Context) []byte {
	fmt.Printf("- Fetching false-positives list for project %q.\n", context.config.projectId)

	header := make(map[string]string)
	header["Content-Type"] = "application/json"
	header["Accept"] = "application/json"

	LogDebug(context.config.debug, fmt.Sprintf("getFalsePositivesList: header=%q", header))
	response := sendWithHeader("GET", buildGetFalsePositivesListAPICall(context), context, header)

	data, err := ioutil.ReadAll(response.Body)
	HandleHTTPError(err)

	LogDebug(context.config.debug, fmt.Sprintf("SecHub false-positives list: %s", string(data)))
	return data
}
