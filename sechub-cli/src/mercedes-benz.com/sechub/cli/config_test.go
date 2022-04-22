// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"
	"path/filepath"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
)

func TestConfigByFlagsThrowsNoError(t *testing.T) {
	config := NewConfigByFlags()
	if config == nil {
		t.Fatal("NewConfigByFlags() return null!")
	}
}

func Example_parseConfigFromEnvironmentVerification() {
	// PREPARE
	config := NewConfigByFlags()
	config.apiToken = "old-apitoken"
	os.Setenv(SechubApitokenEnvVar, "apitoken-from-environment")
	config.debug = false
	os.Setenv(SechubDebugEnvVar, "true")
	config.ignoreDefaultExcludes = false
	os.Setenv(SechubIgnoreDefaultExcludesEnvVar, "true")
	config.keepTempFiles = false
	os.Setenv(SechubKeepTempfilesEnvVar, "true")
	config.quiet = false
	os.Setenv(SechubQuietEnvVar, "true")
	config.server = "old-server"
	os.Setenv(SechubServerEnvVar, "server-from-environment")
	config.projectID = "old.projectID"
	os.Setenv(SechubProjectEnvVar, "project-from-environment")
	config.trustAll = false
	os.Setenv(SechubTrustAllEnvVar, "true")
	config.user = "old-userID"
	os.Setenv(SechubUserIDEnvVar, "user-from-environment")
	config.waitSeconds = 0
	os.Setenv(SechubWaittimeDefaultEnvVar, "777")
	config.whitelistAll = false
	os.Setenv(SechubWhitelistAllEnvVar, "true")
	// EXECUTE
	parseConfigFromEnvironment(config)
	// TEST
	fmt.Println(config.apiToken)
	fmt.Println(config.debug)
	fmt.Println(config.ignoreDefaultExcludes)
	fmt.Println(config.keepTempFiles)
	fmt.Println(config.quiet)
	fmt.Println(config.server)
	fmt.Println(config.projectID)
	fmt.Println(config.trustAll)
	fmt.Println(config.user)
	fmt.Println(config.waitSeconds)
	fmt.Println(config.whitelistAll)
	// Output:
	// apitoken-from-environment
	// true
	// true
	// true
	// true
	// server-from-environment
	// project-from-environment
	// true
	// user-from-environment
	// 777
	// true
}

func Example_isConfigFieldFilledVerification() {
	// PREPARE
	config := NewConfigByFlags()
	config.apiToken = "not empty"
	config.user = ""
	config.debug = false
	config.trustAll = true
	// EXECUTE
	r1 := isConfigFieldFilled(config, "apiToken")
	r2 := isConfigFieldFilled(config, "user")
	r3 := isConfigFieldFilled(config, "debug")
	r4 := isConfigFieldFilled(config, "trustAll")
	// TEST
	fmt.Println(r1, r2, r3, r4)
	// Output: true false false true
}

func Example_willTrailingSlashBeRemovedFromUrl() {
	// PREPARE
	config := NewConfigByFlags()
	config.server = "https://test.example.org/"
	config.action = "version"
	config.apiToken = "not empty"
	config.projectID = "testproject"
	config.user = "testuser"
	// EXECUTE
	assertValidConfig(config)
	// TEST
	fmt.Println(config.server)
	// Output: https://test.example.org
}

func TestValidateRequestedReportFormat(t *testing.T) {
	// PREPARE
	config := NewConfigByFlags()
	config.reportFormat = "written-on-paper"
	// EXECUTE
	validateRequestedReportFormat(config)
	// TEST
	if config.reportFormat != "json" {
		t.Errorf("Reportformat was not changed to 'json'. Got '%s'.", config.reportFormat)
	}
}

func Example_validateRequestedReportFormatMakesLowercase1() {
	// PREPARE
	config := NewConfigByFlags()
	config.reportFormat = "HTML"
	// EXECUTE
	validateRequestedReportFormat(config)
	// TEST
	fmt.Println(config.reportFormat)
	// Output:
	// NOTICE: Converted requested report format 'HTML' to lowercase. Because it contained uppercase characters, which are not accepted by SecHub server.
	// html
}

func Example_validateRequestedReportFormatMakesLowercase2() {
	// PREPARE
	config := NewConfigByFlags()
	config.reportFormat = "Json"
	// EXECUTE
	validateRequestedReportFormat(config)
	// TEST
	fmt.Println(config.reportFormat)
	// Output:
	// NOTICE: Converted requested report format 'Json' to lowercase. Because it contained uppercase characters, which are not accepted by SecHub server.
	// json
}

func Example_normalizeCMDLineArgs() {
	// PREPARE
	argList0 := []string{"./sechub"}
	argList1 := []string{"./sechub", "scan"}
	argList2 := []string{"./sechub", "-jobUUID", "3bdcc5c5-c2b6-4599-be84-f74380680808", "getReport"}
	argList3 := []string{"./sechub", "getReport", "-jobUUID", "3bdcc5c5-c2b6-4599-be84-f74380680808"}
	argList4 := []string{"./sechub", "-configfile", "my-sechub.json", "scan", "-stop-on-yellow"}
	argList5 := []string{"./sechub", "-configfile", "my-sechub.json", "scan", "-wait", "30"}
	argList6 := []string{"./sechub", "-version"}
	// EXECUTE
	fmt.Println(normalizeCMDLineArgs(argList0))
	fmt.Println(normalizeCMDLineArgs(argList1))
	fmt.Println(normalizeCMDLineArgs(argList2))
	fmt.Println(normalizeCMDLineArgs(argList3))
	fmt.Println(normalizeCMDLineArgs(argList4))
	fmt.Println(normalizeCMDLineArgs(argList5))
	fmt.Println(normalizeCMDLineArgs(argList6))
	// Output:
	// [./sechub]
	// [./sechub scan]
	// [./sechub -jobUUID 3bdcc5c5-c2b6-4599-be84-f74380680808 getReport]
	// [./sechub -jobUUID 3bdcc5c5-c2b6-4599-be84-f74380680808 getReport]
	// [./sechub -configfile my-sechub.json -stop-on-yellow scan]
	// [./sechub -configfile my-sechub.json -wait 30 scan]
	// [./sechub -version]
}

func Example_tempFile_current_dir() {
	// PREPARE
	var config Config
	config.tempDir = "."
	context := NewContext(&config)

	// EXECUTE
	result := tempFile(context, "sources.zip")

	// TEST
	fmt.Println(result)
	// Output:
	// sources.zip
}

func Example_tempFile_absolute_path() {
	// PREPARE
	var config Config
	config.tempDir = "/tmp/my_dir"
	context := NewContext(&config)

	// EXECUTE
	result := tempFile(context, "sources.zip")

	// TEST
	fmt.Println(result)
	// Output:
	// /tmp/my_dir/sources.zip
}

func Test_validateTempDir(t *testing.T) {
	// PREPARE
	tempDir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(tempDir)

	regularFile := filepath.Join(tempDir, "regular_file")
	sechubTestUtil.CreateTestFile(regularFile, 0644, []byte(""), t)

	var config1 Config
	var config2 Config
	var config3 Config
	var config4 Config
	config1.tempDir = "."
	config2.tempDir = "/this/really/does/not/exist"
	config3.tempDir = tempDir
	config4.tempDir = regularFile

	// EXECUTE
	result1 := validateTempDir(&config1)
	result2 := validateTempDir(&config2)
	result3 := validateTempDir(&config3)
	result4 := validateTempDir(&config4)

	// TEST
	sechubTestUtil.AssertTrue(result1, t)
	sechubTestUtil.AssertFalse(result2, t)
	sechubTestUtil.AssertTrue(result3, t)
	sechubTestUtil.AssertFalse(result4, t)
}

func Test_validateOutputLocation_current_dir(t *testing.T) {
	// PREPARE
	var config Config
	config.outputLocation = "."

	// EXECUTE
	result := validateOutputLocation(&config)

	// TEST
	sechubTestUtil.AssertTrue(result, t)
	sechubTestUtil.AssertEquals(".", config.outputFolder, t)
	sechubTestUtil.AssertEquals("", config.outputFileName, t)
}

func Test_validateOutputLocation_relative_dir(t *testing.T) {
	// PREPARE
	tempDir := "Test_validateOutputLocation_relative_dir"
	sechubTestUtil.CreateTestDirectory(tempDir, 0755, t)
	defer os.RemoveAll(tempDir)

	var config Config
	config.outputLocation = tempDir

	// EXECUTE
	result := validateOutputLocation(&config)

	// TEST
	sechubTestUtil.AssertTrue(result, t)
	sechubTestUtil.AssertStringContains(config.outputFolder, tempDir, t)
	sechubTestUtil.AssertEquals("", config.outputFileName, t)
}

func Test_validateOutputLocation_absolute_dir(t *testing.T) {
	// PREPARE
	tempDir := sechubTestUtil.InitializeTestTempDir(t)
	sechubTestUtil.CreateTestDirectory(tempDir, 0755, t)
	defer os.RemoveAll(tempDir)

	var config Config
	config.outputLocation = tempDir

	// EXECUTE
	result := validateOutputLocation(&config)

	// TEST
	sechubTestUtil.AssertTrue(result, t)
	sechubTestUtil.AssertStringContains(config.outputFolder, tempDir, t)
	sechubTestUtil.AssertEquals("", config.outputFileName, t)
}

func Test_validateOutputLocation_non_existing_dir(t *testing.T) {
	// PREPARE
	tempDir := "/this/really/does/not/exist"
	var config Config
	config.outputLocation = tempDir

	// EXECUTE
	result := validateOutputLocation(&config)

	// TEST
	sechubTestUtil.AssertFalse(result, t)
}

func Test_validateOutputLocation_absolute_filepath(t *testing.T) {
	// PREPARE
	tempFile := "testfile.json"
	tempDir := sechubTestUtil.InitializeTestTempDir(t)
	sechubTestUtil.CreateTestDirectory(tempDir, 0755, t)
	defer os.RemoveAll(tempDir)

	var config Config
	config.outputLocation = filepath.Join(tempDir, tempFile)

	// EXECUTE
	result := validateOutputLocation(&config)

	// TEST
	sechubTestUtil.AssertTrue(result, t)
	sechubTestUtil.AssertEquals(tempDir, config.outputFolder, t)
	sechubTestUtil.AssertEquals(tempFile, config.outputFileName, t)
}

func Test_validateOutputLocation_invalid_filepath(t *testing.T) {
	// PREPARE
	tempFile := "testfile.json"
	tempDir := "/this/really/does/not/exist"

	var config Config
	config.outputLocation = filepath.Join(tempDir, tempFile)

	// EXECUTE
	result := validateOutputLocation(&config)

	// TEST
	sechubTestUtil.AssertFalse(result, t)
}

func Test_validateOutputLocation_filename_only(t *testing.T) {
	// PREPARE
	tempFile := "Test_validateOutputLocation_filename_only.json"

	var config Config
	config.outputLocation = tempFile

	// EXECUTE
	result := validateOutputLocation(&config)

	// TEST
	sechubTestUtil.AssertTrue(result, t)
	sechubTestUtil.AssertEquals(".", config.outputFolder, t)
	sechubTestUtil.AssertEquals(tempFile, config.outputFileName, t)
}

func Test_validateOutputLocation_empty(t *testing.T) {
	// PREPARE
	var config Config

	// EXECUTE
	result := validateOutputLocation(&config)

	// TEST
	sechubTestUtil.AssertTrue(result, t)
	sechubTestUtil.AssertEquals(".", config.outputFolder, t)
	sechubTestUtil.AssertEquals("", config.outputFileName, t)
}

func Test_validateWaitTimeOrWarning(t *testing.T) {
	// PREPARE
	bigvalue := 10000000

	// EXECUTE
	result1 := validateWaitTimeOrWarning(0)
	result2 := validateWaitTimeOrWarning(bigvalue)

	// TEST
	sechubTestUtil.AssertEquals(MinimalWaitTimeSeconds, result1, t)
	sechubTestUtil.AssertEquals(bigvalue, result2, t)
}

func Test_validateTimeoutOrWarning(t *testing.T) {
	// PREPARE
	bigvalue := 10000000

	// EXECUTE
	result1 := validateTimeoutOrWarning(0)
	result2 := validateTimeoutOrWarning(bigvalue)

	// TEST
	sechubTestUtil.AssertEquals(MinimalTimeoutInSeconds, result1, t)
	sechubTestUtil.AssertEquals(bigvalue, result2, t)
}
