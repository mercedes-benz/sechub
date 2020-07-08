// SPDX-License-Identifier: MIT
package cli

import (
	"encoding/json"
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

	// we don't want to send content here
	context.unfilledByteValue = []byte(``)

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
	failed = HandleIOError(err)

	if failed {
		showHelpHint()
		os.Exit(ExitCodeIOError)
	}

	// Send to SecHub server
	sendWithDefaultHeader("PUT", buildFalsePositivesAPICall(context), context)
	fmt.Printf("Successfully uploaded SecHub false-positives list for project %q to server.\n", context.config.projectId)
}

func removeFalsePositivesFromFile(context *Context) {
	LogDebug(context.config.debug, fmt.Sprintf("Action %q: remove false positives - read from file: %s", context.config.action, context.config.file))

	/* open file and check exists */
	jsonFile, err := os.Open(context.config.file)
	defer jsonFile.Close()
	failed := HandleIOError(err)

	context.unfilledByteValue, err = ioutil.ReadAll(jsonFile)
	failed = HandleIOError(err)

	if failed {
		showHelpHint()
		os.Exit(ExitCodeIOError)
	}

	// read json into go struct
	removeFalsePositivesList := newFalsePositivesListFromBytes(context.unfilledByteValue)
	LogDebug(context.config.debug, fmt.Sprintf("False positives to be removed: %+v", removeFalsePositivesList))

	fmt.Printf("Applying false-positives to be removed for project %q:\n", context.config.projectId)
	// Loop over list and push to SecHub server
	// Url scheme: curl 'https://sechub.example.com/api/project/project1/false-positive/f1d02a9d-5e1b-4f52-99e5-401854ccf936/42' -i -X DELETE
	urlPrefix := buildFalsePositiveAPICall(context)
	// we don't want to send content here
	context.unfilledByteValue = []byte(``)

	for _, element := range removeFalsePositivesList.JobData {
		fmt.Printf("- JobUUID %s: finding #%d\n", element.JobUUID, element.FindingID)
		sendWithDefaultHeader("DELETE", fmt.Sprintf("%s/%s/%d", urlPrefix, element.JobUUID, element.FindingID), context)
		//fmt.Println(fmt.Sprintf("%s/%s/%d", urlPrefix, element.JobUUID, element.FindingID))
	}
	fmt.Println("Transfer completed")
}

func newFalsePositivesListFromBytes(bytes []byte) FalsePositivesConfig {
	var list FalsePositivesConfig

	/* transform text to json */
	err := json.Unmarshal(bytes, &list)
	if err != nil {
		fmt.Println("sechub confiuration json is not valid json")
		showHelpHint()
		HandleError(err)
	}
	return list
}
