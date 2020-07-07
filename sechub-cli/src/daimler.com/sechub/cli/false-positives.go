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

// FalsePositivesConfig - struct containing information for defining false-positives
type FalsePositivesConfig struct {
	APIVersion string                  `json:"apiVersion"`
	Type       string                  `json:"type"`
	JobData    []FalsePositivesJobData `json:"jobData"`
}

// FalsePositivesJobData - contains data related to a scan job for defining false-positives
type FalsePositivesJobData struct {
	JobUUID   string `json:"jobUUID"`
	FindingID int    `json:"findingId"`
	Comment   string `json:"comment"`
}

func (list *FalsePositivesList) save(context *Context) {
	filePath := list.createFilePath(false)
	LogDebug(context.config.debug, fmt.Sprintf("Saving to filepath %q", filePath))

	content := append(list.serverResult, []byte("\n")...) // add newline to the end

	WriteContentToFile(filePath, content, "json")

	fmt.Printf("- Project %q: false-positives list written to file %s\n", context.config.projectId, filePath)
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

	response := sendWithDefaultHeader("GET", buildFalsePositivesAPICall(context), context)

	data, err := ioutil.ReadAll(response.Body)
	HandleHTTPError(err)

	LogDebug(context.config.debug, fmt.Sprintf("SecHub false-positives list: %s", string(data)))
	return data
}

func uploadFalsePositivesFromFile(context *Context) {
	LogDebug(context.config.debug, fmt.Sprintf("Action %q: uploading file: %s\n", context.config.action, context.config.file))

	/* open file and check exists */
	jsonFile, err := os.Open(context.config.file)
	defer jsonFile.Close()
	failed := HandleIOError(err)

	context.unfilledByteValue, err = ioutil.ReadAll(jsonFile)
	fmt.Printf("content: %s\n", context.unfilledByteValue)
	failed = HandleIOError(err)

	if failed {
		showHelpHint()
		os.Exit(ExitCodeIOError)
	}

	// Send to SecHub server
	sendWithDefaultHeader("PUT", buildFalsePositivesAPICall(context), context)
}
