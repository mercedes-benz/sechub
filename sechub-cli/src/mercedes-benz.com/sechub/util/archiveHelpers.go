// SPDX-License-Identifier: MIT

package util

import (
	"fmt"
	"path/filepath"
	"strings"
)

func normalizeArchivePath(file string, currentWorkingDirectory string) (string, error) {
	fileAbs, err := filepath.Abs(file)
	if err != nil {
		return fmt.Sprintf("Could not get absolute filepath of %s", file), err
	}

	// Change to a Unix-Style path if on Windows
	fileAbs = ConvertToUnixStylePath(fileAbs)

	// Make zip path relative to current working directory (the usual case)
	zipPath := strings.TrimPrefix(fileAbs, currentWorkingDirectory)

	// If we still have an absolute path: use the non-absolute file path stripped from "./" and "../"
	if strings.HasPrefix(zipPath, "/") {
		zipPath = file
		zipPath = ConvertToUnixStylePath(zipPath)

		zipPath = strings.ReplaceAll(zipPath, "../", "")
		zipPath = strings.ReplaceAll(zipPath, "./", "")
		// Remove leading / from zip path
		zipPath = strings.TrimPrefix(zipPath, "/")
	}

	return zipPath, nil
}
