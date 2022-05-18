// SPDX-License-Identifier: MIT

package cli

import (
	"os"
	"strings"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
	sechubUtil "mercedes-benz.com/sechub/util"
)

func Test_prepareScan_repects_whitelistAll_and_DefaultSourceCodeExcludeDirPatterns(t *testing.T) {
	/* prepare */
	dir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	gitsubdir := dir + "/.git"
	srcsubdir := dir + "/src"
	sechubTestUtil.CreateTestDirectory(gitsubdir, 0755, t)
	sechubTestUtil.CreateTestDirectory(srcsubdir, 0755, t)

	filename0 := gitsubdir + "/config"
	filename1 := srcsubdir + "/file1.c"
	filename2 := srcsubdir + "/file2.png"

	content := []byte("Hello world!\n")
	sechubTestUtil.CreateTestFile(filename0, 0644, content, t)
	sechubTestUtil.CreateTestFile(filename1, 0644, content, t)
	sechubTestUtil.CreateTestFile(filename2, 0644, content, t)

	var config Config
	config.projectID = "test"
	config.tempDir = dir
	config.whitelistAll = true

	var sechubconfig SecHubConfig
	sechubconfig.CodeScan.FileSystem.Folders = []string{srcsubdir}

	context := NewContext(&config)
	context.sechubConfig = &sechubconfig

	zipfile := dir + "/sourcecode-test.zip"

	/* execute */
	prepareScan(context)
	list, err := sechubUtil.ReadContentOfZipFile(zipfile)
	sechubTestUtil.Check(err, t)

	/* test */
	sechubTestUtil.AssertContainsNot(list, strings.TrimPrefix(filename0, "/"), t) // this file must not be inside, because excluded by default
	sechubTestUtil.AssertContains(list, strings.TrimPrefix(filename1, "/"), t)    // this must remain
	sechubTestUtil.AssertContains(list, strings.TrimPrefix(filename2, "/"), t)    // this must remain due to: whitelistAll = true
	sechubTestUtil.AssertSize(list, 2, t)
}
