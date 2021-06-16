// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"
	"testing"
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
