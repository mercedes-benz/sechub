// SPDX-License-Identifier: MIT

package util

import (
	"path/filepath"
	"strings"
)

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
