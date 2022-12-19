// SPDX-License-Identifier: MIT

package util

import (
	"fmt"
	"path/filepath"
	"regexp"
	"strings"
)

// FilePathMatch - This method provides ANT like selectors.
// See https://ant.apache.org/manual/dirtasks.html
// For example: "**/a*.txt" will accept
//               - "/home/tester/xyz/a1234.txt"
//               - "a1b.txt"
//
func FilePathMatch(path string, pattern string) (result bool) {

	// Let's turn the ant style pattern into a regexp:
	doublestarPatterns := strings.Split(pattern, "**/")

	for i, subElement := range doublestarPatterns {
		// esacpe . with backslash and convert * to .*
		doublestarPatterns[i] = strings.Replace(subElement, ".", "\\.", -1)
		doublestarPatterns[i] = strings.Replace(doublestarPatterns[i], "*", ".*", -1)
	}
	regexpPattern := "^" + strings.Join(doublestarPatterns, ".*/") + "$"

	// add a './' in front of the path except it starts with a '/'
	// so e.g. "**/.git/**" will also match the current working directory and not only subdirectories
	if !strings.HasPrefix(path, "/") && strings.Contains(pattern, "/") {
		path = "./" + path
	}

	matched, err := regexp.MatchString(regexpPattern, path)
	if err != nil {
		LogError(fmt.Sprintln("Error evaluating filepath matches:", err, matched))
	}

	return matched
}

// ConvertBackslashPath - converts a path containing windows separators to unix ones
func ConvertBackslashPath(path string) string {
	return strings.Replace(path, "\\", "/", -1) /* convert all \ to / if on a windows machine */
}

// ConvertToUnixStylePath - eliminate Volume name and change backslashes to forward slashes
func ConvertToUnixStylePath(path string) string {
	newpath := strings.TrimPrefix(path, filepath.VolumeName(path)) // eliminate e.g. "C:"
	newpath = ConvertBackslashPath(newpath)                        // '\' -> '/'
	return newpath
}
