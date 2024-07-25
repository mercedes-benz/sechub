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
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Saving to filepath %q", filePath))

	content := append(report.serverResult, []byte("\n")...) // add newline to the end

	sechubUtil.WriteContentToFile(filePath, content, context.config.reportFormat)

	if context.config.quiet {
		fmt.Println(filePath)
	} else {
		sechubUtil.Log(fmt.Sprintf("SecHub report written to %s", filePath), context.config.quiet)
	}
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
	sechubUtil.Log(fmt.Sprintf("Fetching result (format=%s) for job %s", context.config.reportFormat, context.config.secHubJobUUID), context.config.quiet)

	header := make(map[string]string)
	header["Content-Type"] = "application/json"

	switch context.config.reportFormat {
	case ReportFormatHTML:
		header["Accept"] = "text/html"
	case ReportFormatJSON:
		header["Accept"] = "application/json"
	case ReportFormatSPDXJSON:
		header["Accept"] = "application/json"
	}

	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("getSecHubJobReport: header=%q", header))
	response := sendWithHeader("GET", buildGetSecHubJobReportAPICall(context), context, header)

	data, err := io.ReadAll(response.Body)
	sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)

	return data
}

func newSecHubReportFromFile(context *Context) SecHubReport {
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Loading config file: '%s'", context.config.file))

	/* open file and check exists */
	jsonFile, err := os.Open(context.config.file)
	if sechubUtil.HandleIOError(err) {
		showHelpHint()
		os.Exit(ExitCodeIOError) // exiting from go implicitely closes all open files
	}
	defer jsonFile.Close()

	var filecontent []byte
	filecontent, err = io.ReadAll(jsonFile)
	if sechubUtil.HandleIOError(err) {
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
		sechubUtil.LogError("Report data is not valid json")
		showHelpHint()
		os.Exit(ExitCodeMissingConfigFile)
	}
	return report
}
