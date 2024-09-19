// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"
	"path/filepath"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
)

func TestReportFilePathCorrectCreated(t *testing.T) {
	/* prepare */
	report := ReportDownload{serverResult: []byte("content"), outputFolder: "path1", outputFileName: "fileName1"}

	/* execute */
	result := report.createFilePath(false)

	/* test */
	expected := filepath.Join("path1", "fileName1")
	if result != expected {
		t.Fatalf("Strings differ:\nExpected:%s\nGot     :%s", expected, result)
	}
}

func TestReportSaveWritesAFile(t *testing.T) {
	/* prepare - now all shall work again... */
	tempDir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(tempDir)

	report := ReportDownload{serverResult: []byte("content"), outputFolder: tempDir, outputFileName: "a.out"}
	fmt.Printf("Report: %q\n", report)

	var config Config
	context := NewContext(&config)

	/* execute */
	report.save(context)

	/* test */
	expected := filepath.Join(tempDir, "a.out")
	sechubTestUtil.AssertFileExists(expected, t)
}

func TestNewSecHubReportFromFile(t *testing.T) {
	// PREPARE
	config := NewConfigByFlags()
	context := NewContext(config)
	demoreport := `
	{
		"jobUUID": "d9822de2-3415-4b54-8448-bb1635be917e",
		"result": {
			 "count": 0,
			 "findings": [
					{
						 "id": 1,
						 "name": "Absolute Path Traversal",
						 "severity": "HIGH",
						 "code": {
								"location": "java/com/mercedes-benz/sechub/docgen/AsciidocGenerator.java",
								"line": 28,
								"column": 35,
								"source": "\tpublic static void main(String[] args) throws Exception {/*SECHUB mockdata*/",
								"relevantPart": "args",
								"calls": {
									 "location": "java/com/mercedes-benz/sechub/docgen/AsciidocGenerator.java",
									 "line": 33,
									 "column": 17,
									 "source": "\t\tString path = args[0];",
									 "relevantPart": "args",
									 "calls": {
											"location": "java/com/mercedes-benz/sechub/docgen/AsciidocGenerator.java",
											"line": 33,
											"column": 10,
											"source": "\t\tString path = args[0];",
											"relevantPart": "path",
											"calls": {
												 "location": "java/com/mercedes-benz/sechub/docgen/AsciidocGenerator.java",
												 "line": 34,
												 "column": 38,
												 "source": "\t\tFile documentsGenFolder = new File(path);",
												 "relevantPart": "path",
												 "calls": {
														"location": "java/com/mercedes-benz/sechub/docgen/AsciidocGenerator.java",
														"line": 34,
														"column": 29,
														"source": "\t\tFile documentsGenFolder = new File(path);",
														"relevantPart": "File"
												 }
											}
									 }
								}
						 },
						 "type": "codeScan",
						 "cweId": 36
					},
					{
						 "id": 106,
						 "name": "Race Condition Format Flaw",
						 "severity": "LOW",
						 "code": {
								"location": "java/com/mercedes-benz/sechub/docgen/kubernetes/KubernetesTemplateFilesGenerator.java",
								"line": 228,
								"column": 27,
								"source": "\t\treturn dateFormat.format(new Date());",
								"relevantPart": "format"
						 },
						 "type": "codeScan",
						 "cweId": 362
					}
			 ]
		},
		"trafficLight": "RED"
 }
	`
	dir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)
	reportFileName := dir + "/sechub_report_demo.json"
	sechubTestUtil.CreateTestFile(reportFileName, 0644, []byte(demoreport), t)
	config.file = reportFileName
	// EXECUTE
	result := newSecHubReportFromFile(context)
	// TEST
	sechubTestUtil.AssertEquals("d9822de2-3415-4b54-8448-bb1635be917e", result.JobUUID, t)
	sechubTestUtil.AssertEquals("RED", result.TrafficLight, t)
	sechubTestUtil.AssertEquals(0, result.Result.Count, t) // AssertEquals umbauen mit interface{} als Typ !!
	sechubTestUtil.AssertEquals("HIGH", result.Result.Findings[0].Severity, t)
	sechubTestUtil.AssertEquals("codeScan", result.Result.Findings[0].Type, t)
	sechubTestUtil.AssertEquals(28, result.Result.Findings[0].Code.Line, t)
	sechubTestUtil.AssertEquals("Race Condition Format Flaw", result.Result.Findings[1].Name, t)
	sechubTestUtil.AssertEquals(362, result.Result.Findings[1].CweID, t)
}
