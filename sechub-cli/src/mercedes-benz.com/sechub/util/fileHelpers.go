// SPDX-License-Identifier: MIT

package util

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/fs"
	"io/ioutil"
	"os"
	"regexp"
)

// WriteContentToFile - Write content to a file; do pretty printing if of type json
func WriteContentToFile(filePath string, content []byte, format string) error {
	if format == "json" {
		content = JSONPrettyPrint(content)
	}

	err := os.WriteFile(filePath, content, 0644)

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

// FindNewestMatchingFileInDir - used e.g. for finding the latest report file
func FindNewestMatchingFileInDir(filePattern string, dir string, debug bool) string {
	LogDebug(debug, fmt.Sprintf("FindNewestMatchingFileInDir: Pattern='%s' ; dir='%s'", filePattern, dir))

	var newestFile string = ""
	var newestTime int64 = 0

	files, err := ioutil.ReadDir(dir)
	HandleIOError(err)

	for _, file := range files {
		matched, _ := regexp.MatchString(filePattern, file.Name())
		if !matched {
			continue
		}

		fi, err := os.Stat(dir + "/" + file.Name())
		HandleIOError(err)

		currTime := fi.ModTime().Unix()
		if currTime > newestTime {
			newestTime = currTime
			newestFile = file.Name()
		}
	}
	return newestFile
}

// VerifyDirectoryExists - verify that directory exists on the file system
func VerifyDirectoryExists(directory string) bool {
	fileinfo, err := os.Stat(directory)
	if os.IsNotExist(err) {
		return false
	}
	if !fileinfo.IsDir() {
		return false
	}

	return true
}

// GetFileSize - return file size in bytes
func GetFileSize(filepath string) int64 {
	fileinfo, err := os.Stat(filepath)
	HandleIOError(err)

	return fileinfo.Size()
}

// IsSymlink - return true if the file exists and is a symlink
func IsSymlink(filepath string) bool {
	fileinfo, err := os.Lstat(filepath)
	if err != nil {
		return false
	}

	return fileinfo.Mode()&fs.ModeSymlink != 0
}
