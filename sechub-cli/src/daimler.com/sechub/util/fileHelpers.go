// SPDX-License-Identifier: MIT

package util

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"testing"

	"daimler.com/sechub/testutil"
)

func initializeTestTempDir(t *testing.T) (name string) {
	name, err := ioutil.TempDir("", "sechub-cli-temp")
	testutil.Check(err, t)

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
	testutil.Check(err, t)

	_, err = os.Stat(file)
	if os.IsNotExist(err) {
		t.Fatalf("Creating file %q failed. Error: %q", file, err)
	} else {
		fmt.Printf("File created: %q\n", file)
	}
}
