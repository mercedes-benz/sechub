// SPDX-License-Identifier: MIT

package cli

import (
	"flag"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"testing"
	"time"

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
	originalArgs := os.Args
	os.Args = []string{"sechub", "scan"}

	context := new(Context)
	config := NewConfigByFlags()
	context.config = config

	config.server = "https://test.example.org/"
	config.action = "version"
	config.apiToken = "not empty"
	config.projectID = "testproject"
	config.user = "testuser"
	// EXECUTE
	assertValidConfig(context)
	// TEST

	// Restore original arguments
	os.Args = originalArgs

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
	// NOTICE: Converted requested report format 'HTML' to lowercase because it contained uppercase characters, which are not accepted by SecHub server.
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
	// NOTICE: Converted requested report format 'Json' to lowercase because it contained uppercase characters, which are not accepted by SecHub server.
	// json
}

func Example_actionSpellCorrection() {
	// PREPARE
	// EXECUTE
	fmt.Println(actionSpellCorrection("scan"))
	fmt.Println(actionSpellCorrection("scanasync"))
	fmt.Println(actionSpellCorrection("HELP"))
	fmt.Println(actionSpellCorrection("interactivemarkfalsepositives"))
	fmt.Println(actionSpellCorrection("getstatuS"))
	fmt.Println(actionSpellCorrection("interactiveUnmarkFalsepositives"))
	// Output:
	// scan
	// scanAsync
	// help
	// interactiveMarkFalsePositives
	// getStatus
	// interactiveUnmarkFalsePositives
}

func Example_flagSpellCorrection() {
	// PREPARE
	// EXECUTE
	fmt.Println(flagSpellCorrection("apiToken"))
	fmt.Println(flagSpellCorrection("nonExisting"))
	fmt.Println(flagSpellCorrection("configFile"))
	fmt.Println(flagSpellCorrection("jobuuid"))
	fmt.Println(flagSpellCorrection("HELP"))
	// Output:
	// apitoken
	// nonExisting
	// configfile
	// jobUUID
	// help
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

func Example_spellCorrectionNormalizeCMDLineArgs() {
	// PREPARE
	argList0 := []string{"./sechub", "SCAN"}
	argList1 := []string{"./sechub", "getfalsepositives"}
	argList2 := []string{"./sechub", "-jobuuid", "3bdcc5c5-c2b6-4599-be84-f74380680808", "GETREPORT"}
	argList3 := []string{"./sechub", "getreport", "-JOBUUID", "3bdcc5c5-c2b6-4599-be84-f74380680808"}
	argList4 := []string{"./sechub", "-configFile", "my-sechub.json", "scan", "-Stop-On-Yellow"}
	argList5 := []string{"./sechub", "-CONFIGFILE", "my-sechub.json", "scanasync", "-WAIT", "30"}
	argList6 := []string{"./sechub", "listjobs"}
	// EXECUTE
	fmt.Println(normalizeCMDLineArgs(argList0))
	fmt.Println(normalizeCMDLineArgs(argList1))
	fmt.Println(normalizeCMDLineArgs(argList2))
	fmt.Println(normalizeCMDLineArgs(argList3))
	fmt.Println(normalizeCMDLineArgs(argList4))
	fmt.Println(normalizeCMDLineArgs(argList5))
	fmt.Println(normalizeCMDLineArgs(argList6))
	// Output:
	// [./sechub scan]
	// [./sechub getFalsePositives]
	// [./sechub -jobUUID 3bdcc5c5-c2b6-4599-be84-f74380680808 getReport]
	// [./sechub -jobUUID 3bdcc5c5-c2b6-4599-be84-f74380680808 getReport]
	// [./sechub -configfile my-sechub.json -stop-on-yellow scan]
	// [./sechub -configfile my-sechub.json -wait 30 scanAsync]
	// [./sechub listJobs]
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
	bigValue := 10000000

	// EXECUTE
	result1 := validateWaitTimeOrWarning(0)
	result2 := validateWaitTimeOrWarning(bigValue)

	// TEST
	sechubTestUtil.AssertEquals(MinimalWaitTimeSeconds, result1, t)
	sechubTestUtil.AssertEquals(bigValue, result2, t)
}

func Test_validateTimeoutOrWarning(t *testing.T) {
	// PREPARE
	bigValue := 10000000

	// EXECUTE
	result1 := validateTimeoutOrWarning(0)
	result2 := validateTimeoutOrWarning(bigValue)

	// TEST
	sechubTestUtil.AssertEquals(MinimalTimeoutInSeconds, result1, t)
	sechubTestUtil.AssertEquals(bigValue, result2, t)
}

func Test_validateInitialWaitIntervalOrWarning(t *testing.T) {
	// PREPARE
	var okay int64 = 60 * int64(time.Second)                // 60s
	var tooSmall int64 = int64(0.01 * float64(time.Second)) // 0.01s

	// EXECUTE
	result1 := validateInitialWaitIntervalOrWarning(okay)
	result2 := validateInitialWaitIntervalOrWarning(tooSmall)

	// TEST
	sechubTestUtil.AssertEquals(okay, result1, t)
	sechubTestUtil.AssertEquals(int64(MinimalInitialWaitIntervalSeconds*float64(time.Second)), result2, t)
}

func Example_will_reportfile_be_found_in_current_dir() {
	// PREPARE
	originalArgs := os.Args
	os.Args = []string{"sechub", "scan"}

	context := new(Context)
	config := new(Config)
	context.config = config

	config.action = interactiveMarkFalsePositivesAction
	config.projectID = "testproject"

	config.user = "testuser"
	config.apiToken = "not empty"
	config.server = "https://test.example.org"
	config.reportFormat = "json"
	config.timeOutSeconds = 10
	config.initialWaitIntervalNanoseconds = int64(2 * float64(time.Second))
	config.waitSeconds = 60

	// Create report file: sechub_report_testproject_45cd4f59-4be7-4a86-9bc7-47528ced16c2.json
	reportFileName := "./sechub_report_" + config.projectID + "_45cd4f59-4be7-4a86-9bc7-47528ced16c2.json"
	ioutil.WriteFile(reportFileName, []byte(""), 0644)
	defer os.Remove(reportFileName)

	// EXECUTE
	assertValidConfig(context)

	// TEST
	// Restore original arguments
	os.Args = originalArgs

	// Output:
	// Using latest report file "sechub_report_testproject_45cd4f59-4be7-4a86-9bc7-47528ced16c2.json".
}

func Example_check_if_uppercase_username_will_be_corrected() {
	// PREPARE
	originalArgs := os.Args
	os.Args = []string{"sechub", "scan"}

	context := new(Context)
	config := new(Config)
	context.config = config

	config.action = interactiveMarkFalsePositivesAction
	config.projectID = "testproject"

	config.user = "TESTUSER"
	config.apiToken = "not empty"
	config.server = "https://test.example.org"
	config.reportFormat = "json"
	config.timeOutSeconds = 10
	config.initialWaitIntervalNanoseconds = int64(2 * float64(time.Second))
	config.waitSeconds = 60

	// Create report file: sechub_report_testproject_45cd4f59-4be7-4a86-9bc7-47528ced16c2.json
	reportFileName := "./sechub_report_" + config.projectID + "_45cd4f59-4be7-4a86-9bc7-47528ced16c2.json"
	ioutil.WriteFile(reportFileName, []byte(""), 0644)
	defer os.Remove(reportFileName)

	// EXECUTE
	assertValidConfig(context)

	// TEST
	// Restore original arguments
	os.Args = originalArgs

	// Output:
	// NOTICE: Converted user id 'TESTUSER' to lowercase because it contained uppercase characters, which are not accepted by SecHub server.
	// Using latest report file "sechub_report_testproject_45cd4f59-4be7-4a86-9bc7-47528ced16c2.json".
}

func Test_check_if_too_many_cmdline_args_get_capped(t *testing.T) {
	// PREPARE
	originalArgs := os.Args
	os.Args = []string{"sechub"}

	for i := 0; i < MaximumNumberOfCMDLineArguments/2; i++ {
		os.Args = append(os.Args, "-tempdir")
		os.Args = append(os.Args, fmt.Sprintf("%d", i))
	}
	fmt.Print("os.Args=")
	fmt.Println(os.Args)
	before := len(os.Args)

	// EXECUTE
	validateMaximumNumberOfCMDLineArgumentsOrCapAndWarning()

	// TEST
	fmt.Print("os.Args=")
	fmt.Println(os.Args)
	after := len(os.Args)
	sechubTestUtil.AssertEquals(MaximumNumberOfCMDLineArguments+1, before, t)
	sechubTestUtil.AssertEquals(MaximumNumberOfCMDLineArguments, after, t)

	// Restore original arguments
	os.Args = originalArgs
}

func Test_check_max_cmdline_args_are_accepted(t *testing.T) {
	// PREPARE
	originalArgs := os.Args
	os.Args = []string{"sechub"}

	for i := 1; i < MaximumNumberOfCMDLineArguments; i++ {
		os.Args = append(os.Args, "-help")
	}
	fmt.Print("os.Args=")
	fmt.Println(os.Args)

	// EXECUTE
	validateMaximumNumberOfCMDLineArgumentsOrCapAndWarning()

	// TEST
	fmt.Print("os.Args=")
	fmt.Println(os.Args)

	sechubTestUtil.AssertEquals(MaximumNumberOfCMDLineArguments, len(os.Args), t)

	// Restore original arguments
	os.Args = originalArgs
}

func Example_label_from_cmdline_args() {
	// PREPARE
	argSafe := os.Args
	os.Args = []string{"sechub", "-label", "key1=value1", "-label", "key2=value2"}

	// EXECUTE
	flag.Parse()

	// restore original os.Args
	os.Args = argSafe

	// TEST
	fmt.Println(configFromInit.labels["key1"])
	fmt.Println(configFromInit.labels["key2"])

	// Output:
	// value1
	// value2
}

func Example_label_from_env_var() {
	// PREPARE
	os.Setenv(SechubLabelsEnvVar, "key1=value1,key2=value2")

	// EXECUTE
	parseConfigFromEnvironment(&configFromInit)

	// TEST
	fmt.Println(configFromInit.labels["key1"])
	fmt.Println(configFromInit.labels["key2"])

	// Output:
	// value1
	// value2
}

func Example_label_from_cmdline_args_and_env_var() {
	// PREPARE
	argSafe := os.Args
	os.Args = []string{"sechub", "-label", "key1=value1", "-label", "key2=value2"}

	os.Setenv(SechubLabelsEnvVar, "key2=value2x,key3=value3x")
	// args override env var - so key2 from env will be ignored

	// EXECUTE
	parseConfigFromEnvironment(&configFromInit)
	flag.Parse()

	// restore original os.Args
	os.Args = argSafe

	// TEST
	fmt.Println(configFromInit.labels["key1"])
	fmt.Println(configFromInit.labels["key2"])
	fmt.Println(configFromInit.labels["key3"])

	// Output:
	// value1
	// value2
	// value3x
}
