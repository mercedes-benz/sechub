// SPDX-License-Identifier: MIT
package cli

import (
	"fmt"
	"os"
	"path/filepath"
	"testing"

	. "daimler.com/sechub/testutil"
	. "daimler.com/sechub/util"
)

func TestFalsePositivesFilePathCorrectCreated(t *testing.T) {
	/* prepare */
	list := FalsePositivesList{serverResult: []byte("content"), outputFolder: "path1", outputFileName: "fileName1"}

	/* execute */
	result := list.createFilePath(false)

	/* test */
	expected := filepath.Join("path1", "fileName1")
	if result != expected {
		t.Fatalf("Strings differ:\nExpected:%s\nGot     :%s", expected, result)
	}
}

func TestFalsePositivesSaveWritesAFile(t *testing.T) {
	/* prepare */
	tempDir := InitializeTestTempDir(t)
	defer os.RemoveAll(tempDir)

	list := FalsePositivesList{serverResult: []byte("content"), outputFolder: tempDir, outputFileName: "a.out"}
	fmt.Printf("list: %q\n", list)

	var config Config
	context := NewContext(&config)

	/* execute */
	list.save(context)

	/* test */
	expected := filepath.Join(tempDir, "a.out")
	AssertFileExists(expected, t)
}
