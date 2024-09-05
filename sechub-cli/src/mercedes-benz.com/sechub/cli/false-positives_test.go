// SPDX-License-Identifier: MIT

package cli

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
)

func TestFalsePositivesFilePathCorrectCreated(t *testing.T) {
	/* prepare */
	list := FalsePositivesList{serverResult: []byte("content"), outputFolder: "path1", outputFileName: "fileName1"}

	/* execute */
	result := list.createFilePath(false)

	/* test */
	expected := filepath.Join("path1", "fileName1")
	if result != expected {
		t.Fatalf("Strings differ:\nExpected:%s\nGot     :%s", expected, result)
	}
}

func TestFalsePositivesSaveWritesAFile(t *testing.T) {
	/* prepare */
	tempDir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(tempDir)

	list := FalsePositivesList{serverResult: []byte("content"), outputFolder: tempDir, outputFileName: "a.out"}
	fmt.Printf("list: %q\n", list)

	var config Config
	context := NewContext(&config)

	/* execute */
	list.save(context)

	/* test */
	expected := filepath.Join(tempDir, "a.out")
	sechubTestUtil.AssertFileExists(expected, t)
}

func Example_defineFalsePositives() {
	/* prepare */
	definedFalsePositives := []FalsePositivesJobData{
		{JobUUID: "11111111-1111-1111-1111-111111111111", FindingID: 1, Comment: "test1"},
		{JobUUID: "22222222-2222-2222-2222-222222222222", FindingID: 2, Comment: "test2"},
		{JobUUID: "33333333-3333-3333-3333-333333333333", FindingID: 3, Comment: "test3"},
		{JobUUID: "55555555-5555-5555-5555-555555555555", FindingID: 5, Comment: "test5"},
	}
	falsePositivesDefinitionList := FalsePositivesConfig{APIVersion: CurrentAPIVersion, Type: falsePositivesListType, JobData: definedFalsePositives}

	falsePositivesServerList := []FalsePositiveDefinition{
		{JobData: FalsePositivesJobData{JobUUID: "11111111-1111-1111-1111-111111111111", FindingID: 1}},
		{JobData: FalsePositivesJobData{JobUUID: "22222222-2222-2222-2222-222222222222", FindingID: 2}},
		{JobData: FalsePositivesJobData{JobUUID: "44444444-4444-4444-4444-444444444444", FindingID: 4}},
	}

	/* execute */
	falsePositivesToAdd, falsePositivesToRemove := defineFalsePositives(falsePositivesDefinitionList, falsePositivesServerList)

	/* test */
	fmt.Printf("Add: %+v\n", falsePositivesToAdd)
	fmt.Printf("Remove: %+v\n", falsePositivesToRemove)

	// Output:
	// Add: {APIVersion:1.0 Type:falsePositiveJobDataList JobData:[{JobUUID:33333333-3333-3333-3333-333333333333 FindingID:3 Comment:test3} {JobUUID:55555555-5555-5555-5555-555555555555 FindingID:5 Comment:test5}] ProjectData:[]}
	// Remove: {APIVersion:1.0 Type:falsePositiveJobDataList JobData:[{JobUUID:44444444-4444-4444-4444-444444444444 FindingID:4 Comment:}] ProjectData:[]}
}

func Example_defineFalsePositivesEmptyInputList() {
	// An empty input list will remove all defined false-positives

	/* prepare */
	definedFalsePositives := []FalsePositivesJobData{}
	falsePositivesDefinitionList := FalsePositivesConfig{APIVersion: CurrentAPIVersion, Type: falsePositivesListType, JobData: definedFalsePositives}

	falsePositivesServerList := []FalsePositiveDefinition{
		{JobData: FalsePositivesJobData{JobUUID: "11111111-1111-1111-1111-111111111111", FindingID: 1}},
		{JobData: FalsePositivesJobData{JobUUID: "22222222-2222-2222-2222-222222222222", FindingID: 2}},
	}

	/* execute */
	falsePositivesToAdd, falsePositivesToRemove := defineFalsePositives(falsePositivesDefinitionList, falsePositivesServerList)

	/* test */
	fmt.Printf("Add: %+v\n", falsePositivesToAdd)
	fmt.Printf("Remove: %+v\n", falsePositivesToRemove)

	// Output:
	// Add: {APIVersion:1.0 Type:falsePositiveJobDataList JobData:[] ProjectData:[]}
	// Remove: {APIVersion:1.0 Type:falsePositiveJobDataList JobData:[{JobUUID:11111111-1111-1111-1111-111111111111 FindingID:1 Comment:} {JobUUID:22222222-2222-2222-2222-222222222222 FindingID:2 Comment:}] ProjectData:[]}
}

func Example_defineFalsePositivesEmptyServerList() {
	// An empty server list will simply add all defined false-positives

	/* prepare */
	definedFalsePositives := []FalsePositivesJobData{
		{JobUUID: "11111111-1111-1111-1111-111111111111", FindingID: 1, Comment: "test1"},
		{JobUUID: "22222222-2222-2222-2222-222222222222", FindingID: 2, Comment: "test2"},
	}
	falsePositivesDefinitionList := FalsePositivesConfig{APIVersion: CurrentAPIVersion, Type: falsePositivesListType, JobData: definedFalsePositives}

	falsePositivesServerList := []FalsePositiveDefinition{}

	/* execute */
	falsePositivesToAdd, falsePositivesToRemove := defineFalsePositives(falsePositivesDefinitionList, falsePositivesServerList)

	/* test */
	fmt.Printf("Add: %+v\n", falsePositivesToAdd)
	fmt.Printf("Remove: %+v\n", falsePositivesToRemove)

	// Output:
	// Add: {APIVersion:1.0 Type:falsePositiveJobDataList JobData:[{JobUUID:11111111-1111-1111-1111-111111111111 FindingID:1 Comment:test1} {JobUUID:22222222-2222-2222-2222-222222222222 FindingID:2 Comment:test2}] ProjectData:[]}
	// Remove: {APIVersion:1.0 Type:falsePositiveJobDataList JobData:[] ProjectData:[]}
}

func Example_getFalsePositivesUploadChunk1() {
	/* prepare */

	// Override global MaxChunkSizeFalsePositives to get reproducable results
	MaxChunkSizeFalsePositives = 2

	definedFalsePositives := []FalsePositivesJobData{
		{JobUUID: "11111111-1111-1111-1111-111111111111", FindingID: 1, Comment: "test1"},
		{JobUUID: "22222222-2222-2222-2222-222222222222", FindingID: 2, Comment: "test2"},
		{JobUUID: "33333333-3333-3333-3333-333333333333", FindingID: 3, Comment: "test3"},
	}
	falsePositivesDefinitionList := FalsePositivesConfig{APIVersion: CurrentAPIVersion, Type: falsePositivesListType, JobData: definedFalsePositives}

	/* execute */
	fmt.Printf("%+v\n", getFalsePositivesUploadChunk(falsePositivesDefinitionList, 0))
	fmt.Printf("%+v\n", getFalsePositivesUploadChunk(falsePositivesDefinitionList, 1))
	fmt.Printf("%+v\n", getFalsePositivesUploadChunk(falsePositivesDefinitionList, 2))

	/* test */

	// Output:
	// {APIVersion:1.0 Type:falsePositiveJobDataList JobData:[{JobUUID:11111111-1111-1111-1111-111111111111 FindingID:1 Comment:test1} {JobUUID:22222222-2222-2222-2222-222222222222 FindingID:2 Comment:test2}] ProjectData:[]}
	// {APIVersion:1.0 Type:falsePositiveJobDataList JobData:[{JobUUID:33333333-3333-3333-3333-333333333333 FindingID:3 Comment:test3}] ProjectData:[]}
	// {APIVersion:1.0 Type:falsePositiveJobDataList JobData:[] ProjectData:[]}
}

func Example_newFalsePositivesListFromBytes_jobData()  {
	/* prepare */
	fpJSON := `
{
	"apiVersion": "1.0", 
	"type": "falsePositiveDataList", 
	"jobData": [
		{
			"jobUUID": "6cfa2ccf-da13-4dee-b529-0225ed9661bd", 
			"findingId": 1, 
			"comment": "Meaningful comment" 
		},
		{
			"jobUUID": "6cfa2ccf-da13-4dee-b529-0225ed9661bd", 
			"findingId": 2
		}
	]
}
	`

	inputfile := []byte(fpJSON)

	/* execute */
	fpList := newFalsePositivesListFromBytes(inputfile)

	/* test */
	jsonBlob, _ := json.Marshal(fpList)
	fmt.Println(string(jsonBlob))

	// Output:
	// {"apiVersion":"1.0","type":"falsePositiveDataList","jobData":[{"jobUUID":"6cfa2ccf-da13-4dee-b529-0225ed9661bd","findingId":1,"comment":"Meaningful comment"},{"jobUUID":"6cfa2ccf-da13-4dee-b529-0225ed9661bd","findingId":2,"comment":""}],"projectData":null}
}

func Example_newFalsePositivesListFromBytes_projectData()  {
	/* prepare */
	fpJSON := `
{
	"apiVersion": "1.0", 
	"type": "falsePositiveDataList", 
	"projectData": [
		{
			"id": "my-id",
			"comment": "text1",
			"webScan": {
				"cweId": 89,
				"urlPattern": "https://myapp-*.example.com:80*/rest/*/search?*",
				"methods": [ "GET", "DELETE" ]
			}
		}
	]
}
	`

	inputfile := []byte(fpJSON)

	/* execute */
	fpList := newFalsePositivesListFromBytes(inputfile)

	/* test */
	jsonBlob, _ := json.Marshal(fpList)
	fmt.Println(string(jsonBlob))

	// Output:
	// {"apiVersion":"1.0","type":"falsePositiveDataList","jobData":null,"projectData":[{"id":"my-id","comment":"text1","webScan":{"cweId":89,"urlPattern":"https://myapp-*.example.com:80*/rest/*/search?*","methods":["GET","DELETE"]}}]}
}

func Example_newFalsePositivesListFromBytes_projectDataWithoutCWE()  {
	/* prepare */
	fpJSON := `
{
	"apiVersion": "1.0", 
	"type": "falsePositiveDataList", 
	"projectData": [
		{
			"id": "my-id",
			"webScan": {
				"urlPattern": "https://myapp-*.example.com/rest/login?*"
			}
		}
	]
}
	`

	inputfile := []byte(fpJSON)

	/* execute */
	fpList := newFalsePositivesListFromBytes(inputfile)

	/* test */
	jsonBlob, _ := json.Marshal(fpList)
	fmt.Println(string(jsonBlob))

	// Output:
	// {"apiVersion":"1.0","type":"falsePositiveDataList","jobData":null,"projectData":[{"id":"my-id","comment":"","webScan":{"cweId":0,"urlPattern":"https://myapp-*.example.com/rest/login?*","methods":null}}]}
}
