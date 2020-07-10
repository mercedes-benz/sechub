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

// ReportDownload - struct for handling report downloads
type ReportDownload struct {
	outputFileName string
	outputFolder   string
	serverResult   []byte
}

// SecHubReport - structure of a SecHub json report
type SecHubReport struct {
	JobUUID      string             `json:"jobUUID"`
	Result       SecHubReportResult `json:"result"`
	TrafficLight string             `json:"trafficLight"`
}

// SecHubReportResult - structure of result part of a SecHub json report
type SecHubReportResult struct {
	Count    int                    `json:"count"`
	Findings []SecHubReportFindings `json:"findings"`
}

// SecHubReportFindings - structure of findings part of a SecHub json report
type SecHubReportFindings struct {
	ID       int                  `json:"id"`
	Name     string               `json:"name"`
	Severity string               `json:"severity"`
	Code     SecHubReportCodePart `json:"code"`
	Type     string               `json:"type"`
	CweID    int                  `json:"cweId"`
}

// SecHubReportCodePart - structure of codescan part of a SecHub json report
type SecHubReportCodePart struct {
	Location     string `json:"location"`
	Line         int    `json:"line"`
	Column       int    `json:"column"`
	Source       string `json:"source"`
	RelevantPart string `json:"relevantPart"`
}

func (report *ReportDownload) save(context *Context) {
	filePath := report.createFilePath(true)
	LogDebug(context.config.debug, fmt.Sprintf("Saving to filepath %q", filePath))

	content := append(report.serverResult, []byte("\n")...) // add newline to the end

	WriteContentToFile(filePath, content, context.config.reportFormat)

	fmt.Printf("- SecHub report written to %s\n", filePath)
}

func (report *ReportDownload) createFilePath(forceDirectory bool) string {
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

func newSecHubReportFromFile(context *Context) SecHubReport {
	LogDebug(context.config.debug, fmt.Sprintf("Loading config file: '%s'", context.config.file))

	/* open file and check exists */
	jsonFile, err := os.Open(context.config.file)
	defer jsonFile.Close()

	if HandleIOError(err) {
		showHelpHint()
		os.Exit(ExitCodeIOError)
	}

	var filecontent []byte
	filecontent, err = ioutil.ReadAll(jsonFile)
	if HandleIOError(err) {
		showHelpHint()
		os.Exit(ExitCodeIOError)
	}

	return newSecHubReportFromBytes(filecontent)
}

// newSecHubReportFromBytes - read json into SecHubReport struct
func newSecHubReportFromBytes(bytes []byte) SecHubReport {
	var report SecHubReport

	/* transform text to json */
	err := json.Unmarshal(bytes, &report)
	if err != nil {
		fmt.Println("sechub confiuration json is not valid json")
		showHelpHint()
		os.Exit(ExitCodeMissingConfigFile)
	}
	return report
}
