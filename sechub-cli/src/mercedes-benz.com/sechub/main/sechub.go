// SPDX-License-Identifier: MIT

package main

/*
 * Main entry point for SECHUB CLI client.
 */
import (
	"mercedes-benz.com/sechub/cli"
)

func main() {
	/* mainpackage : cli so cli.Execute() - done by controller.go*/
	cli.Execute()
}
