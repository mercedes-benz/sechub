// SPDX-License-Identifier: MIT

package util

import (
	"errors"
	"fmt"
	"log"
	"os"
	"testing"
)

// InitializeTestTempDir - creates a new directory in tmp with a unique name
func InitializeTestTempDir(t *testing.T) (name string) {
	name, err := os.MkdirTemp("", "sechub-cli-temp")
	Check(err, t)

	CreateTestDirectory(name, 0755, t)

	return name
}

// CreateTestDirectory - create a directory in testing context
func CreateTestDirectory(dir string, mode os.FileMode, t *testing.T) {
	_, err := os.Stat(dir)
	if os.IsExist(err) {
		log.Printf("Folder already exists: %q\n", dir)
		return
	}

	err = os.MkdirAll(dir, mode)
	if err != nil {
		t.Fatalf("Cannot create folder. Error: %q\n", err)
	}
	fmt.Printf("Folder created: %q\n", dir)
}

// CreateTestFile - create a regular file with text content in testing context
func CreateTestFile(file string, mode os.FileMode, content []byte, t *testing.T) {
	_, err := os.Stat(file)
	if !os.IsNotExist(err) {
		fmt.Printf("File already exists: %q\n", file)
		return
	}

	err = os.WriteFile(file, content, mode)
	Check(err, t)

	_, err = os.Stat(file)
	if os.IsNotExist(err) {
		t.Fatalf("Creating file %q failed. Error: %q\n", file, err)
	} else {
		fmt.Printf("File created: %q\n", file)
	}
}

func CreateTestSymlink(file string, mode os.FileMode, symlinkTarget string, t *testing.T)  {
	_, err := os.Stat(file)
	if !os.IsNotExist(err) {
		fmt.Printf("File already exists: %q\n", file)
		return
	}

	// Create symlink
	err = os.Symlink(symlinkTarget, file)

	if errors.Is(err, errors.ErrUnsupported) {
		// Do we run on platforms that do not support symbolic links?
		fmt.Println("Your OS platform does not support symbolic links. No file created.")
		return
	} else if err != nil {
		t.Fatalf("Creating symlink file %q failed. Error: %q\n", file, err)
	} else {
		fmt.Printf("Symlink created: %q -> %q\n", file, symlinkTarget)
	}
}
