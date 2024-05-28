// SPDX-License-Identifier: MIT

package cli

import (
	"encoding/json"
	"fmt"
	"io"
	"os"
	"path/filepath"

	sechubUtil "mercedes-benz.com/sechub/util"
)

// Keyword for false-posisitves json file
const falsePositivesListType = "falsePositiveJobDataList"

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

// FalsePositivesDefinition - the struct that comes from SecHub server with getFalsePositives
type FalsePositivesDefinition struct {
	Items []FalsePositiveDefinition `json:"falsePositives"`
}

// FalsePositiveDefinition - a single false-positive definition from server
type FalsePositiveDefinition struct {
	JobData  FalsePositivesJobData           `json:"jobData"`
	Author   string                          `json:"author"`
	MetaData FalsePositiveDefinitionMetaData `json:"metaData"`
	Created  string                          `json:"created"`
}

// FalsePositiveDefinitionMetaData - metadata part of FalsePositiveDefinition
type FalsePositiveDefinitionMetaData struct {
	ScanType string                              `json:"scanType"`
	Name     string                              `json:"name"`
	Severity string                              `json:"severity"`
	Code     FalsePositiveDefinitionCodeMetaData `json:"code"`
	CweID    int                                 `json:"cweId"`
}

// FalsePositiveDefinitionCodeMetaData - location in code
type FalsePositiveDefinitionCodeMetaData struct {
	Start FalsePositiveDefinitionCodeLocation `json:"start"`
	End   FalsePositiveDefinitionCodeLocation `json:"end"`
}

// FalsePositiveDefinitionCodeLocation - code location definition
type FalsePositiveDefinitionCodeLocation struct {
	Location     string `json:"location"`
	RelevantPart string `json:"relevantPart"`
	SourceCode   string `json:"sourceCode"`
}

func (list *FalsePositivesList) save(context *Context) {
	filePath := list.createFilePath(false)
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Saving to filepath %q", filePath))

	content := append(list.serverResult, []byte("\n")...) // add newline to the end

	sechubUtil.WriteContentToFile(filePath, content, "json")

	if context.config.quiet {
		fmt.Println(filePath)
	} else {
		sechubUtil.Log(fmt.Sprintf("Project %q: false-positives list written to file %s", context.config.projectID, filePath), context.config.quiet)
	}
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
	sechubUtil.Log(fmt.Sprintf("Fetching false-positives list for project %q from server.", context.config.projectID), context.config.quiet)

	// we don't want to send content here
	context.inputForContentProcessing = []byte(``)
	processContent(context)

	response := sendWithDefaultHeader("GET", buildFalsePositivesAPICall(context), context)

	data, err := io.ReadAll(response.Body)
	sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)

	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("SecHub false-positives list: %s", string(data)))
	return data
}

/* at the moment this does just set data to both byte value holder - if it's necessary in future, we can provide template mechanism inside this */
func processContent(context *Context) {
	context.contentToSend = context.inputForContentProcessing // content data used for TLS encrypted data (currently we do not provide templating for false positive data, so just same)
}

// readFileIntoContext
//
//	  reads a file referenced by context.config.file into context.inputForContentProcessing as byte array
//		If context.config.file is empty then use fallbackFile
func readFileIntoContext(context *Context, fallbackFile string) {
	var file string
	if context.config.file == "" {
		file = fallbackFile
	} else {
		file = context.config.file
	}
	sechubUtil.Log(fmt.Sprintf("Reading file %q", file), context.config.quiet)

	inputFile, err := os.Open(file)
	if sechubUtil.HandleIOError(err) {
		showHelpHint()
		os.Exit(ExitCodeIOError)
	}
	defer inputFile.Close()

	// read file's content into context.inputForContentProcessing
	context.inputForContentProcessing, err = io.ReadAll(inputFile)
	if sechubUtil.HandleIOError(err) {
		os.Exit(ExitCodeIOError)
	}
}

func defineFalsePositivesFromFile(context *Context) {
	readFileIntoContext(context, DefaultSecHubFalsePositivesJSONFile)

	// read json into go struct
	falsePositivesDefinitionList := newFalsePositivesListFromBytes(context.inputForContentProcessing)
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("False positives to be defined: %+v", falsePositivesDefinitionList))

	// download false-positives list for project from SecHub server
	jsonFPBlob := FalsePositivesList{serverResult: getFalsePositivesList(context), outputFolder: "", outputFileName: ""}
	var falsePositivesServerList FalsePositivesDefinition
	err := json.Unmarshal(jsonFPBlob.serverResult, &falsePositivesServerList)
	sechubUtil.HandleError(err, ExitCodeFailed)

	// Compute the FPs to add and those to remove
	sechubUtil.Log("Computing differences", context.config.quiet)
	falsePositivesToAdd, falsePositivesToRemove := defineFalsePositives(falsePositivesDefinitionList, falsePositivesServerList.Items)
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("False positives to be added: %+v\n", falsePositivesToAdd))
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("False positives to be removed: %+v\n", falsePositivesToRemove))

	// Apply changes to SecHub server
	markFalsePositives(context, &falsePositivesToAdd)
	unmarkFalsePositives(context, &falsePositivesToRemove)
}

func defineFalsePositives(newFalsePositives FalsePositivesConfig, currentFalsePositives []FalsePositiveDefinition) (
	falsePositivesToAdd FalsePositivesConfig,
	falsePositivesToRemove FalsePositivesConfig) {
	// Initialize structures
	falsePositivesToAdd.APIVersion = CurrentAPIVersion
	falsePositivesToAdd.Type = falsePositivesListType
	falsePositivesToRemove.APIVersion = CurrentAPIVersion
	falsePositivesToRemove.Type = falsePositivesListType

	// Loop through definition list and figure out, what to add and what to remove
	for _, newFalsePositive := range newFalsePositives.JobData {
		matched := false
		for i, falsePositive := range currentFalsePositives {
			if newFalsePositive.JobUUID == falsePositive.JobData.JobUUID && newFalsePositive.FindingID == falsePositive.JobData.FindingID {
				matched = true
				// False positive is already defined. Remove item from list
				currentFalsePositives[i] = currentFalsePositives[len(currentFalsePositives)-1] // Copy last item to current position
				currentFalsePositives = currentFalsePositives[:len(currentFalsePositives)-1]   // Truncate slice
				break
			}
		}
		if !matched {
			// False positive is to be added
			falsePositivesToAdd.JobData = append(falsePositivesToAdd.JobData, newFalsePositive)
		}
	}

	// currentFalsePositives now contains all false positives to remove
	for _, falsePositiveToBeRemoved := range currentFalsePositives {
		var fp FalsePositivesJobData
		fp.JobUUID = falsePositiveToBeRemoved.JobData.JobUUID
		fp.FindingID = falsePositiveToBeRemoved.JobData.FindingID
		falsePositivesToRemove.JobData = append(falsePositivesToRemove.JobData, fp)
	}

	return falsePositivesToAdd, falsePositivesToRemove
}

func uploadFalsePositivesFromFile(context *Context) {
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Action %q: uploading file: %s\n", context.config.action, context.config.file))

	readFileIntoContext(context, DefaultSecHubFalsePositivesJSONFile)
	processContent(context)

	uploadFalsePositives(context)

	sechubUtil.Log("Transfer completed", context.config.quiet)
}

func uploadFalsePositives(context *Context) {
	// Send context.contentToSend to SecHub server
	sendWithDefaultHeader("PUT", buildFalsePositivesAPICall(context), context)
}

func unmarkFalsePositivesFromFile(context *Context) {
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Action %q: remove false positives - read from file: %s", context.config.action, context.config.file))

	readFileIntoContext(context, "")

	// read json into go struct
	removeFalsePositivesList := newFalsePositivesListFromBytes(context.inputForContentProcessing)
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("False positives to be removed: %+v", removeFalsePositivesList))

	unmarkFalsePositives(context, &removeFalsePositivesList)
}

func unmarkFalsePositives(context *Context, list *FalsePositivesConfig) {
	if len(list.JobData) == 0 {
		sechubUtil.Log("0 false-positives removed from project \""+context.config.projectID+"\"", context.config.quiet)
		return
	}

	sechubUtil.Log("Removing as false-positives from project \""+context.config.projectID+"\":", context.config.quiet)
	// Loop over list and push to SecHub server
	// Url scheme: curl 'https://sechub.example.com/api/project/project1/false-positive/f1d02a9d-5e1b-4f52-99e5-401854ccf936/42' -i -X DELETE
	urlPrefix := buildFalsePositiveAPICall(context)

	// we don't want to send content here
	context.inputForContentProcessing = []byte(``)
	processContent(context)

	for _, element := range list.JobData {
		sechubUtil.Log(fmt.Sprintf("- JobUUID %s: Finding #%d", element.JobUUID, element.FindingID), context.config.quiet)
		sendWithDefaultHeader("DELETE", fmt.Sprintf("%s/%s/%d", urlPrefix, element.JobUUID, element.FindingID), context)
	}
	sechubUtil.Log("Transfer completed", context.config.quiet)
}

func newFalsePositivesListFromBytes(bytes []byte) FalsePositivesConfig {
	var list FalsePositivesConfig

	/* transform text to json */
	err := json.Unmarshal(bytes, &list)
	if err != nil {
		sechubUtil.LogError("Encoding is no valid json")
		sechubUtil.HandleError(err, ExitCodeFailed)
	}
	return list
}

func interactiveMarkFalsePositives(context *Context) {
	FalsePositivesList := newFalsePositivesListFromConsole(context)
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("False-positives list for upload:\n%+v", FalsePositivesList))

	if len(FalsePositivesList.JobData) == 0 {
		return
	}

	// ToDo: Are you sure?

	// upload to server
	markFalsePositives(context, &FalsePositivesList)
}

func newFalsePositivesListFromConsole(context *Context) (list FalsePositivesConfig) {
	list.APIVersion = CurrentAPIVersion
	list.Type = falsePositivesListType

	report := newSecHubReportFromFile(context)

	// ToDo: sort report by severity,finding id

	var ExpectedInputs = []sechubUtil.ConsoleInputItem{
		{Input: "y", ShortDescription: "Yes"},
		{Input: "n", ShortDescription: "No"},
		{Input: "s", ShortDescription: "Skip following findings"},
		{Input: "c", ShortDescription: "Cancel"},
	}

	// iterate over entries and ask which to mark
	for _, finding := range report.Result.Findings {
		printFinding(&finding)

		input, err := sechubUtil.ReadAllowedItemFromConsole("Add this as false positive?", ExpectedInputs)
		sechubUtil.HandleError(err, ExitCodeFailed)
		if input == "y" {
			// append finding to list
			fmt.Println("Please add a single line comment:")
			comment, _ := sechubUtil.ReadFromConsole()
			var listEntry = FalsePositivesJobData{report.JobUUID, finding.ID, comment}
			list.JobData = append(list.JobData, listEntry)
		} else if input == "c" {
			os.Exit(ExitCodeOK)
		} else if input == "s" {
			break
		}
	}

	return list
}

func markFalsePositives(context *Context, list *FalsePositivesConfig) {
	if len(list.JobData) == 0 {
		sechubUtil.Log("0 false-positives added to project \""+context.config.projectID+"\"", context.config.quiet)
		return
	}

	sechubUtil.Log("Adding as false-positives to project \""+context.config.projectID+"\":", context.config.quiet)
	for _, element := range list.JobData {
		sechubUtil.Log(fmt.Sprintf("- JobUUID %s: Finding #%d, Comment: %s", element.JobUUID, element.FindingID, element.Comment), context.config.quiet)
	}

	// upload to server
	jsonBlob, err := json.Marshal(list)
	sechubUtil.HandleError(err, ExitCodeFailed)
	context.inputForContentProcessing = jsonBlob
	processContent(context)
	uploadFalsePositives(context)

	sechubUtil.Log("Transfer completed", context.config.quiet)
}

func printFinding(finding *SecHubReportFindings) {
	// Example output:
	// ---------------------------------------------------------------------------
	// 1: Absolute Path Traversal, severity: MEDIUM
	// java/com/mercedes-benz/sechub/docgen/AsciidocGenerator.java, line:28, column:35:
	//       public static void main(String[] args) throws Exception {
	// ---------------------------------------------------------------------------
	sechubUtil.PrintDashedLine()
	fmt.Printf("%d: %s, severity: %s\n", finding.ID, finding.Name, finding.Severity)
	fmt.Printf("%s, line %d column %d\n", finding.Code.Location, finding.Code.Line, finding.Code.Column)
	fmt.Printf("%s\n", finding.Code.Source)
	sechubUtil.PrintDashedLine()
}

func interactiveUnmarkFalsePositives(context *Context) {
	FalsePositivesList := newUnmarkFalsePositivesListFromConsole(context)
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("False-positives unmark list for upload:\n%+v", FalsePositivesList))

	if len(FalsePositivesList.JobData) == 0 {
		sechubUtil.Log("No false positives to unmark.", context.config.quiet)
		return
	}

	// ToDo: Are you sure?

	// upload to SecHub server
	unmarkFalsePositives(context, &FalsePositivesList)
}

func newUnmarkFalsePositivesListFromConsole(context *Context) (result FalsePositivesConfig) {
	result.APIVersion = CurrentAPIVersion
	result.Type = falsePositivesListType

	// download false-positives list from SecHub server
	jsonBlob := FalsePositivesList{serverResult: getFalsePositivesList(context), outputFolder: "", outputFileName: ""}

	var list FalsePositivesDefinition
	err := json.Unmarshal(jsonBlob.serverResult, &list)
	sechubUtil.HandleError(err, ExitCodeFailed)
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Read from Server:\n%+v", list))

	// ToDo: sort report by severity,finding id

	// iterate over entries and ask which to unmark
	var ExpectedInputs = []sechubUtil.ConsoleInputItem{
		{Input: "y", ShortDescription: "Yes"},
		{Input: "n", ShortDescription: "No"},
		{Input: "s", ShortDescription: "Skip the rest"},
		{Input: "c", ShortDescription: "Cancel"},
	}
	for _, falsepositive := range list.Items {
		printFalsePositiveDefinition(&falsepositive)

		input, err := sechubUtil.ReadAllowedItemFromConsole("Do you want to remove this false positive?", ExpectedInputs)
		sechubUtil.HandleError(err, ExitCodeFailed)
		if input == "y" {
			// append finding to list
			var listEntry = FalsePositivesJobData{falsepositive.JobData.JobUUID, falsepositive.JobData.FindingID, ""}
			result.JobData = append(result.JobData, listEntry)
		} else if input == "c" {
			os.Exit(ExitCodeOK)
		} else if input == "s" {
			break
		}
	}

	return result
}

func printFalsePositiveDefinition(falsepositive *FalsePositiveDefinition) {
	// Example output:
	// ------------------------------------------------------------------
	// Creation of Temp File in Dir with Incorrect Permissions, codeScan severity: LOW
	//   Origin: Finding ID 3 in job f94d815c-7f69-48c3-8433-8f03d52ce32a
	//   File: java/com/mercedes-benz/sechub/docgen/kubernetes/KubernetesTemplateFilesGenerator.java
	//   Code:                 File secHubServer = new File("./sechub-server");
	// (Added by admin at 2020-07-10 13:41:06; comment: "Only temporary directory")
	// ------------------------------------------------------------------
	sechubUtil.PrintDashedLine()
	fmt.Printf("%s, %s severity: %s\n", falsepositive.MetaData.Name, falsepositive.MetaData.ScanType, falsepositive.MetaData.Severity)
	fmt.Printf("  Origin: Finding ID %d in job %s\n", falsepositive.JobData.FindingID, falsepositive.JobData.JobUUID)
	// would be cool to have line and column in source code location
	fmt.Printf("  File: %s\n", falsepositive.MetaData.Code.Start.Location)
	fmt.Printf("  Code: %s\n", falsepositive.MetaData.Code.Start.SourceCode)
	fmt.Printf("(Added by %s at %s; comment: %q)\n", falsepositive.Author, falsepositive.Created, falsepositive.JobData.Comment)
	// added by name at date
	sechubUtil.PrintDashedLine()
}
