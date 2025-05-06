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
func TestLoadConfigFile_with_environment_variables(t *testing.T) {
	// PREPARE
	const testProjectName = "TEST-env-Variable1-Value"
	t.Setenv("TEST_ENV_VARIABLE_1P", testProjectName)

	const testServer = "https://from_env.sechub.example.org"
	t.Setenv("TEST_ENV_VARIABLE_1S", testServer)

	const testUser = "fake-user"
	t.Setenv("TEST_ENV_VARIABLE_1U", testUser)

	config := NewConfigByFlags()
	context := NewContext(config)
	configfileContent := `
{
	"apiVersion": "1.0",
	"project": "{{ .TEST_ENV_VARIABLE_1P }}",
	"server": "{{ .TEST_ENV_VARIABLE_1S }}",
	"user": "{{ .TEST_ENV_VARIABLE_1U }}",
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
	AssertEquals(testProjectName, config.projectID, t)
	AssertEquals(testServer, config.server, t)
	AssertEquals(testUser, config.user, t)

}

func Example_lowercaseOrNotice() {
	// PREPARE
	lowercaseString := "teststring1"
	noLowercaseString := "TESTStrinG2"

	// EXECUTE
	result1 := lowercaseOrNotice(lowercaseString, "text1", false)
	result2 := lowercaseOrNotice(noLowercaseString, "text2", false)

	// TEST
	fmt.Println(result1)
	fmt.Println(result2)
	// Output:
	// NOTICE: Converted text2 'TESTStrinG2' to lowercase because it contained uppercase characters, which are not accepted by SecHub server.
	// teststring1
	// teststring2
}

func Example_lowercaseOrNotice_hide_enabled() {
	// PREPARE
	lowercaseString := "teststring1"
	noLowercaseString := "TESTStrinG2"
	// EXECUTE
	result1 := lowercaseOrNotice(lowercaseString, "text1", true)
	result2 := lowercaseOrNotice(noLowercaseString, "text2", true)
	// TEST
	fmt.Println(result1)
	fmt.Println(result2)
	// Output:
	// NOTICE: Converted text2 '***hidden***' to lowercase because it contained uppercase characters, which are not accepted by SecHub server.
	// teststring1
	// teststring2
}
