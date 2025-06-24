// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"testing"
	"time"

	. "mercedes-benz.com/sechub/testutil"
)

func Example_computeNextWaitInterval() {
	/* prepare */
	max := int64(60 * time.Second)

	/* execute */
	result1 := computeNextWaitInterval(int64(100*time.Second), max)
	result2 := computeNextWaitInterval(int64(10*time.Second), max)

	/* test */
	fmt.Println(result1)
	fmt.Println(result2)
	// Output:
	// 60000000000
	// 15000000000
}

func Test_failOnRedFalse(t *testing.T) {
	/* prepare */
	t.Setenv(SechubFailOnRedEnvVar, "false")

	config := NewConfigByFlags()
	parseConfigFromEnvironment(config)
	context := NewContext(config)

	/* execute */
	result := computeRedExitCode(context)

	/* test */
  AssertEquals(ExitCodeOK, result, t)
}

func Test_failOnRedTrue(t *testing.T) {
	/* prepare */
	config := NewConfigByFlags()
	parseConfigFromEnvironment(config)
	context := NewContext(config)

	/* execute */
	result := computeRedExitCode(context)

	/* test */
  AssertEquals(ExitCodeFailed, result, t)
}
