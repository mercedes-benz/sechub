// SPDX-License-Identifier: MIT

package util

import (
	"os"
	"path/filepath"
	"strings"

	"github.com/bmatcuk/doublestar/v4"
)

// OS's path separator as string (Unix-like: "/", Windows: "\")
const PathSeparator = string(os.PathSeparator)

// FilePathMatch - This method provides ANT like selectors.
// See https://ant.apache.org/manual/dirtasks.html
// For example: "**/a*.txt" will accept
//               - "/home/tester/xyz/a1234.txt"
//               - "a1b.txt"
//
func FilePathMatch(path string, pattern string) (result bool) {
	// Make simple patterns like `*.java` also work with doublestar.PathMatch()
	if !strings.Contains(pattern, PathSeparator) {
		pattern = "**" + PathSeparator + pattern
	}

	match, _ := doublestar.PathMatch(pattern, path)
	return match
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
