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
	os.Setenv("SECHUB_APITOKEN", "apitoken-from-environment")
	config.debug = false
	os.Setenv("SECHUB_DEBUG", "true")
	config.keepTempFiles = false
	os.Setenv("SECHUB_KEEP_TEMPFILES", "true")
	config.quiet = false
	os.Setenv("SECHUB_QUIET", "true")
	config.server = "old-server"
	os.Setenv("SECHUB_SERVER", "server-from-environment")
	config.projectID = "old.projectID"
	os.Setenv("SECHUB_PROJECT", "project-from-environment")
	config.trustAll = false
	os.Setenv("SECHUB_TRUSTALL", "true")
	config.user = "old-userID"
	os.Setenv("SECHUB_USERID", "user-from-environment")
	config.waitSeconds = 0
	os.Setenv("SECHUB_WAITTIME_DEFAULT", "777")
	// EXECUTE
	parseConfigFromEnvironment(config)
	// TEST
	fmt.Println(config.apiToken)
	fmt.Println(config.debug)
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
