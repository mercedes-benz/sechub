// SPDX-License-Identifier: MIT

package util

import (
	"os"
	"testing"

	sechubTestUtil "daimler.com/sechub/testutil"
)

func TestCreateChecksum(t *testing.T) {
	// PREPARE
	filecontent := `kjh32kihu6sgdsgSDKZT547jis
  RETHew§465gbsWESDRG§"$%€YAjji
  jsg92357825kjksdg
	`
	dir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)
	fileName := dir + "/testfile.json"
	sechubTestUtil.CreateTestFile(fileName, 0644, []byte(filecontent), t)

	//EXECUTE
	checksum := CreateChecksum(fileName)

	// TEST
	sechubTestUtil.AssertEquals("45cf1a80684f2049530aa938786b6b47d381348a096ffb32a0a05f9f0cc10d88", checksum, t)
}
