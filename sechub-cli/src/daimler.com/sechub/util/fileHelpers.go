// SPDX-License-Identifier: MIT

package util

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
)

// WriteContentToFile - Write content to a file; do pretty printing if of type json
func WriteContentToFile(filePath string, content []byte, format string) error {
	if format == "json" {
		content = JSONPrettyPrint(content)
	}

	err := ioutil.WriteFile(filePath, content, 0644)

	// Exit if file cannot be written
	if HandleIOError(err) {
		os.Exit(1)
	}
	return err
}

// JSONPrettyPrint - beautify json by indenting
func JSONPrettyPrint(in []byte) []byte {
	var out bytes.Buffer
	err := json.Indent(&out, in, "", "   ")
	if err != nil {
		return in
	}
	return out.Bytes()
}

// HandleIOError - Helper function for handling errors in i/o operations
func HandleIOError(err error) bool {
	if err != nil {
		LogError(fmt.Sprintf("I/O error: %q", err))
		return true
	}
	return false
}
