// SPDX-License-Identifier: MIT

package util

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"testing"

	. "daimler.com/sechub/testutil"
)

// InitializeTestTempDir - creates a new directory in tmp with a unique name
func InitializeTestTempDir(t *testing.T) (name string) {
	name, err := ioutil.TempDir("", "sechub-cli-temp")
	Check(err, t)

	createTestDirectory(name, 0755, t)

	return name
}

func createTestDirectory(dir string, mode os.FileMode, t *testing.T) {
	_, err := os.Stat(dir)
	if os.IsExist(err) {
		log.Printf("Folder already exists: %q", dir)
		return
	}

	err = os.MkdirAll(dir, mode)
	if err != nil {
		t.Fatalf("Cannot create folder. Error: %q", err)
	}
	log.Printf("Folder created: %q", dir)
}

func createTestFile(file string, mode os.FileMode, t *testing.T) {
	_, err := os.Stat(file)
	if !os.IsNotExist(err) {
		fmt.Printf("File already exists: %q\n", file)
		return
	}

	content := []byte("Hello world!\n")
	err = ioutil.WriteFile(file, content, mode)
	Check(err, t)

	_, err = os.Stat(file)
	if os.IsNotExist(err) {
		t.Fatalf("Creating file %q failed. Error: %q", file, err)
	} else {
		fmt.Printf("File created: %q\n", file)
	}
}

// WriteContentToFile - Write content to a file; do pretty printing if of type json
func WriteContentToFile(filePath string, content []byte, format string) error {
	if format == "json" {
		content = JSONPrettyPrint(content)
	}

	err := ioutil.WriteFile(filePath, content, 0644)
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
