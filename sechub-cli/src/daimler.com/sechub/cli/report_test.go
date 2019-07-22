// SPDX-License-Identifier: MIT
package cli

import (
	"path/filepath"
	"testing"
)

func TestFilePathCorrectCreated(t *testing.T) {
	/* prepare */
	report := Report{serverResult: "content", outputFolder: "path1", outputFileName: "fileName1"}
    context:= new(Context)

	/* execute */
	result := report.createReportFilePath(context, false)

	/* test */
	expected := filepath.Join("path1", "fileName1")
	if result != expected {
		t.Fatalf("Strings differ:\nExpected:%s\nGot     :%s", expected, result)
	}
}
