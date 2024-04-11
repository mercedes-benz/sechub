// SPDX-License-Identifier: MIT

package cli

import (
	"io"
	"os"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
	sechubUtil "mercedes-benz.com/sechub/util"
)

func TestZipFileEmptyIsRejected(t *testing.T) {
	/* prepare */
	var context Context
	var config Config
	var sechubConfig SecHubConfig
	context.config = &config
	context.sechubConfig = &sechubConfig

	dir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	sechubTestUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubTestUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubTestUtil.CreateTestDirectory(dirname3, 0755, t)

	//  We add empty folders only, so the zip file will be empty.
	// This should trigger an error, because scanning empty zip files makes no sense.

	context.sourceZipFileName = dir + "/testoutput.zip"
	context.sechubConfig.CodeScan.FileSystem.Folders = []string{dirname1, dirname2}

	/* execute */
	err := createSouceCodeZipFile(&context)

	/* test */
	sechubTestUtil.AssertErrorHasExpectedMessage(err, sechubUtil.ZipFileHasNoContent, t)
}

func Test_createSouceCodeZipFile_DataSourcesSectionWorksWithAbsolutePaths(t *testing.T) {
	/* prepare */
	var context Context
	var config Config
	var sechubConfig SecHubConfig
	context.config = &config
	context.sechubConfig = &sechubConfig

	// dir is abolute path
	dir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	// create subdirectories
	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"
	sechubTestUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubTestUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubTestUtil.CreateTestDirectory(dirname3, 0755, t)

	// create files
	filepath1 := dirname1 + "/file-sub1.txt"
	filepath2 := dirname2 + "/file-sub2.txt"
	filepath3 := dirname3 + "/file-sub3.txt"
	filepath4 := dir + "/standalone1.txt"
	filepath5 := dir + "/standalone2.txt"
	content := []byte("Hello world!\n")
	sechubTestUtil.CreateTestFile(filepath1, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath2, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath3, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath4, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath5, 0644, content, t)

	// create configs
	namedCodeScanConfig1 := NamedCodeScanConfig{
		Name:               "testname1",
		FileSystem:         FileSystemConfig{Files: []string{filepath4, filepath5}, Folders: []string{dirname1}},
		SourceCodePatterns: []string{".txt"},
	}

	namedCodeScanConfig2 := NamedCodeScanConfig{
		Name:               "testname2",
		FileSystem:         FileSystemConfig{Folders: []string{dirname2}},
		SourceCodePatterns: []string{".txt"},
	}

	context.sourceZipFileName = dir + "/test.zip"
	context.sechubConfig.Data.Sources = []NamedCodeScanConfig{namedCodeScanConfig1, namedCodeScanConfig2}

	/* execute */
	createSouceCodeZipFile(&context)

	/* test */
	list, _ := sechubUtil.ReadContentOfZipFile(context.sourceZipFileName)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedCodeScanConfig1.Name+filepath1, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedCodeScanConfig1.Name+filepath4, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedCodeScanConfig1.Name+filepath5, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedCodeScanConfig2.Name+filepath2, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedCodeScanConfig2.Name+filepath3, t)
}

func Test_createSouceCodeZipFile_DataSourcesSectionWorksWithRelativePaths(t *testing.T) {
	/* prepare */
	var context Context
	var config Config
	var sechubConfig SecHubConfig
	context.config = &config
	context.sechubConfig = &sechubConfig

	// dir is relative path
	dir := "sechub-cli-tmptest"
	sechubTestUtil.CreateTestDirectory(dir, 0755, t)
	defer os.RemoveAll(dir)

	// create subdirectories
	dirname1 := dir + "/sub1"
	sechubTestUtil.CreateTestDirectory(dirname1, 0755, t)

	// create files
	filepath1 := dirname1 + "/file-sub1.txt"
	filepath2 := dir + "/standalone1.txt"
	content := []byte("Hello world!\n")
	sechubTestUtil.CreateTestFile(filepath1, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath2, 0644, content, t)

	// create configs
	namedCodeScanConfig1 := NamedCodeScanConfig{
		Name:               "testname1",
		FileSystem:         FileSystemConfig{Files: []string{filepath2}, Folders: []string{dirname1}},
		SourceCodePatterns: []string{".txt"},
	}

	context.sourceZipFileName = dir + "/test.zip"
	context.sechubConfig.Data.Sources = []NamedCodeScanConfig{namedCodeScanConfig1}

	/* execute */
	createSouceCodeZipFile(&context)

	/* test */
	list, _ := sechubUtil.ReadContentOfZipFile(context.sourceZipFileName)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedCodeScanConfig1.Name+"/"+filepath1, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedCodeScanConfig1.Name+"/"+filepath2, t)
}

func Test_createSouceCodeZipFile_HandleRemoteDataSection(t *testing.T) {
	/* prepare */
	var context Context
	var config Config
	var sechubConfig SecHubConfig
	context.config = &config
	context.sechubConfig = &sechubConfig

	dir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)
	context.sourceZipFileName = dir + "/testoutput.zip"
	scanConfig := NamedCodeScanConfig{
		Name: "remote-data-section-test",
		// Currently the client does not parse the remote data section. So we leave it empty for now.
	}
	context.sechubConfig.Data.Sources = []NamedCodeScanConfig{scanConfig}

	/* execute */

	// Capture stdout
	rescueStdout := os.Stdout
	r, w, _ := os.Pipe()
	os.Stdout = w

	err := createSouceCodeZipFile(&context)

	// Stop capturing stdout
	w.Close()
	out, _ := io.ReadAll(r)
	os.Stdout = rescueStdout

	/* test */
	sechubTestUtil.AssertNoError(err, t)
	sechubTestUtil.AssertStringContains(string(out), sechubUtil.ZipFileNotCreated, t)
}
