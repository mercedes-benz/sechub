// SPDX-License-Identifier: MIT

package util

import "fmt"

// LogError - print unified error message
func LogError(text string) {
	fmt.Printf("ERROR: " + text + "\n")
}

// LogVerbose - print unified verbose message
func LogVerbose(text string) {
	fmt.Printf("VERBOSE: " + text + "\n")
}

// LogDebug - Print message only if debug flag is set
func LogDebug(debug bool, text string) {
	if debug {
		fmt.Printf("DEBUG: " + text + "\n")
	}
}
