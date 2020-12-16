// SPDX-License-Identifier: MIT

package util

import (
	"fmt"
	"os"
)

// LogError - print unified error message with time stamp
func LogError(text string) {
	fmt.Fprintln(os.Stderr, Timestamp(), "ERROR:", text)
}

// LogWarning - print unified error message
func LogWarning(text string) {
	fmt.Println("WARNING:", text)
}

// LogVerbose - print unified verbose message
func LogVerbose(text string) {
	fmt.Println("VERBOSE:", text)
}

// LogDebug - Print message only if debug flag is set
func LogDebug(debug bool, text string) {
	if debug {
		fmt.Println(Timestamp(), "DEBUG:", text)
	}
}

// Log - log a text message timestamped to stdout
func Log(text string) {
	logWithTimestamp(text)
}

// logWithTimestamp - print message with time stamp
func logWithTimestamp(text string) {
	fmt.Println(Timestamp(), text)
}
