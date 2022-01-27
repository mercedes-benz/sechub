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

// LogWarning - print unified warn message
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

// Log - log a text message timestamped to stdout if passed boolean is 'false'
func Log(text string, silent bool) {
	if !silent {
		logWithTimestamp(text)
	}
}

// logWithTimestamp - print message with time stamp
func logWithTimestamp(text string) {
	fmt.Println(Timestamp(), text)
}

// PrintIfNotSilent - print text if passed boolean is 'false'
func PrintIfNotSilent(text string, silent bool) {
	if !silent {
		fmt.Print(text)
	}
}
