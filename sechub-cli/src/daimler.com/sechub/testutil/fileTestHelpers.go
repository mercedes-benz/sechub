// SPDX-License-Identifier: MIT

package util

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"testing"
)

// InitializeTestTempDir - creates a new directory in tmp with a unique name
func InitializeTestTempDir(t *testing.T) (name string) {
	name, err := ioutil.TempDir("", "sechub-cli-temp")
	Check(err, t)

	CreateTestDirectory(name, 0755, t)

	return name
}

// CreateTestDirectory - create a directory in testing context
func CreateTestDirectory(dir string, mode os.FileMode, t *testing.T) {
	_, err := os.Stat(dir)
	if os.IsExist(err) {
		log.Printf("Folder already exists: %q", dir)
		return
	}

	err = os.MkdirAll(dir, mode)
	if err != nil {
		t.Fatalf("Cannot create folder. Error: %q", err)
	}
	fmt.Printf("Folder created: %q", dir)
}

// CreateTestFile - create a regular file with text content in testing context
func CreateTestFile(file string, mode os.FileMode, content []byte, t *testing.T) {
	_, err := os.Stat(file)
	if !os.IsNotExist(err) {
		fmt.Printf("File already exists: %q\n", file)
		return
	}

	err = ioutil.WriteFile(file, content, mode)
	Check(err, t)

	_, err = os.Stat(file)
	if os.IsNotExist(err) {
		t.Fatalf("Creating file %q failed. Error: %q", file, err)
	} else {
		fmt.Printf("File created: %q\n", file)
	}
}
