// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"
	"testing"

	. "mercedes-benz.com/sechub/testutil"
)

func TestLoadConfigFile(t *testing.T) {
	// PREPARE
	config := NewConfigByFlags()
	context := NewContext(config)
	configfileContent := `
{
	"apiVersion": "1.0",
	"project": "project-from-file",
	"server": "https://server-from-file",
	"user": "user-from-file",
	"codeScan": {"fileSystem": {"folders": ["src"]}}
}
	`
	dir := InitializeTestTempDir(t)
	defer os.RemoveAll(dir)
	configFileName := dir + "/sechub.json"
	CreateTestFile(configFileName, 0644, []byte(configfileContent), t)
	config.configFilePath = configFileName
	config.projectID = ""
	config.server = ""
	config.user = ""
	// EXECUTE
	loadConfigFile(context)
	// TEST
	AssertEquals("project-from-file", config.projectID, t)
	AssertEquals("https://server-from-file", config.server, t)
	AssertEquals("user-from-file", config.user, t)
}

func Example_lowercaseOrNotice() {
	// PREPARE
	lowercaseString := "teststring1"
	noLowercaseString := "TESTStrinG2"
	// EXECUTE
	result1 := lowercaseOrNotice(lowercaseString, "text1")
	result2 := lowercaseOrNotice(noLowercaseString, "text2")
	// TEST
	fmt.Println(result1)
	fmt.Println(result2)
	// Output:
	// NOTICE: Converted text2 'TESTStrinG2' to lowercase because it contained uppercase characters, which are not accepted by SecHub server.
	// teststring1
	// teststring2
}
