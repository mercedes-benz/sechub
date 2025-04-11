// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"
	"strings"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
	sechubUtil "mercedes-benz.com/sechub/util"
)

func Test_prepareScan_respects_whitelistAll_and_DefaultSourceCodeExcludeDirPatterns(t *testing.T) {
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

func Test_prepareScan_binary_upload_respects_exclude_patterns(t *testing.T) {
	/* prepare */
	dir := "sechub-cli-tmptest"

	subdir := dir + "/binaries"
	subsubdir := subdir + "/ignore"
	sechubTestUtil.CreateTestDirectory(dir, 0755, t)
	sechubTestUtil.CreateTestDirectory(subdir, 0755, t)
	sechubTestUtil.CreateTestDirectory(subsubdir, 0755, t)
	defer os.RemoveAll(dir)

	filename0 := dir + "/test.bin"
	filename1 := subdir + "/test.bin"
	filename2 := subdir + "/testfile1"
	filename3 := subsubdir + "/testfile2"

	content := []byte("I am binary 😀\n")
	sechubTestUtil.CreateTestFile(filename0, 0644, content, t)
	sechubTestUtil.CreateTestFile(filename1, 0644, content, t)
	sechubTestUtil.CreateTestFile(filename2, 0644, content, t)
	sechubTestUtil.CreateTestFile(filename3, 0644, content, t)

	var config Config
	config.projectID = "test"
	config.tempDir = dir

	sechubJSON := `
	{
		"data": {
			"binaries": [
				{
					"name": "my-test",
					"fileSystem": {
						"files": [ "sechub-cli-tmptest/test.bin" ],
						"folders": [ "sechub-cli-tmptest/binaries/" ]
					},
					"excludes": [ "**/ignore/**", "*.bin" ]
				}
			]
		}
	}
	`
	sechubconfig := newSecHubConfigFromBytes([]byte(sechubJSON))

	context := NewContext(&config)
	context.sechubConfig = &sechubconfig

	tarfile := dir + "/binaries-test.tar"

	/* execute */
	prepareScan(context)
	list, err := sechubUtil.ListContentOfTarFile(tarfile)
	sechubTestUtil.Check(err, t)

	/* test */
	fmt.Printf("%+v\n", list)
	sechubTestUtil.AssertContains(list, "__data__/my-test/sechub-cli-tmptest/test.bin", t)
	sechubTestUtil.AssertContains(list, "__data__/my-test/sechub-cli-tmptest/binaries/testfile1", t)
	sechubTestUtil.AssertContainsNot(list, "__data__/my-test/sechub-cli-tmptest/binaries/ignore/testfile2", t) // exclude pattern 1
	sechubTestUtil.AssertContainsNot(list, "__data__/my-test/sechub-cli-tmptest/binaries/test.bin", t)         // exclude pattern 2
	sechubTestUtil.AssertSize(list, 2, t)
}

func Example_verifySecHubConfigForbiddenNames() {
	// PREPARE
	var config Config
	config.projectID = "test"

	sechubJSON := `
	{
		"data": {
			"binaries": [
				{ "name": "okay1" },
				{ "name": "__binaries_archive_root__" }
			],
			"sources":  [
				{ "name": "__sourcecode_archive_root__" },
 				{ "name": "okay2" }
			]
		}
	}
	`
	sechubconfig := newSecHubConfigFromBytes([]byte(sechubJSON))

	context := NewContext(&config)
	context.sechubConfig = &sechubconfig

	// EXECUTE
	result := verifySecHubConfig(context)

	// TEST
	fmt.Printf("result: %+v\n", result)

	// Output:
	// result: false
}

func Example_verifySecHubConfigForbiddenNamesOkay() {
	// PREPARE
	var config Config
	config.projectID = "test"

	sechubJSON := `
	{
		"data": {
			"binaries": [ { "name": "okay1" } ],
			"sources":  [ { "name": "okay2" } ]
		}
	}
	`
	sechubconfig := newSecHubConfigFromBytes([]byte(sechubJSON))

	context := NewContext(&config)
	context.sechubConfig = &sechubconfig

	// EXECUTE
	result := verifySecHubConfig(context)

	// TEST
	fmt.Printf("result: %+v\n", result)

	// Output:
	// result: true
}
