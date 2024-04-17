// SPDX-License-Identifier: MIT

package cli

import (
	"io"
	"os"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
	sechubUtil "mercedes-benz.com/sechub/util"
)

func Test_createBinariesTarFile_EmptyTarFileIsRejected(t *testing.T) {
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
	dirname3 := dir + "/sub2/sub3_with_a_veeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeery_loooooooooooooong_name"

	sechubTestUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubTestUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubTestUtil.CreateTestDirectory(dirname3, 0755, t)

	// We add empty folders only, so the tar archive will be empty (containing only directories).
	// This should trigger an error, because uploading empty archives makes no sense.

	context.binariesTarFileName = dir + "/testoutput.tar"
	scanConfig := NamedBinariesScanConfig{
		Name:       "my-test",
		FileSystem: FileSystemConfig{Folders: []string{dirname1, dirname2}},
	}
	context.sechubConfig.Data.Binaries = []NamedBinariesScanConfig{scanConfig}

	/* execute */
	err := createBinariesTarFile(&context)

	/* test */
	sechubTestUtil.AssertErrorHasExpectedMessage(err, sechubUtil.TarFileHasNoContent, t)
}

func Test_createBinariesTarFile_DataBinariesSectionWorksWithAbsolutePaths(t *testing.T) {
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
	filepath1 := dirname1 + "/file-sub1.bin"
	filepath2 := dirname2 + "/file-sub2.bin"
	filepath3 := dirname3 + "/file-sub3.bin"
	filepath4 := dir + "/standalone1.bin"
	filepath5 := dir + "/standalone2.bin"
	content := []byte("Binary :-)\n")
	sechubTestUtil.CreateTestFile(filepath1, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath2, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath3, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath4, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath5, 0644, content, t)

	// create configs
	namedBinariesScanConfig1 := NamedBinariesScanConfig{
		Name:       "testname1",
		FileSystem: FileSystemConfig{Files: []string{filepath4, filepath5}, Folders: []string{dirname1}},
	}

	namedBinariesScanConfig2 := NamedBinariesScanConfig{
		Name:       "testname2",
		FileSystem: FileSystemConfig{Folders: []string{dirname2}},
	}

	context.sechubConfig.Data.Binaries = []NamedBinariesScanConfig{namedBinariesScanConfig1, namedBinariesScanConfig2}
	context.binariesTarFileName = dir + "/test.tar"

	/* execute */
	createBinariesTarFile(&context)

	/* test */
	list, _ := sechubUtil.ListContentOfTarFile(context.binariesTarFileName)
	sechubTestUtil.AssertSize(list, 5, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedBinariesScanConfig1.Name+filepath1, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedBinariesScanConfig1.Name+filepath4, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedBinariesScanConfig1.Name+filepath5, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedBinariesScanConfig2.Name+filepath2, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedBinariesScanConfig2.Name+filepath3, t)
}

func Test_createBinariesTarFile_DataBinariesSectionWorksWithRelativePaths(t *testing.T) {
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
	filepath1 := dirname1 + "/file-sub1.bin"
	filepath2 := dir + "/standalone1.bin"
	content := []byte("Binary :-)\n")
	sechubTestUtil.CreateTestFile(filepath1, 0644, content, t)
	sechubTestUtil.CreateTestFile(filepath2, 0644, content, t)

	// create configs
	namedBinariesScanConfig1 := NamedBinariesScanConfig{
		Name:       "testname1",
		FileSystem: FileSystemConfig{Files: []string{filepath2}, Folders: []string{dirname1}},
	}

	context.sechubConfig.Data.Binaries = []NamedBinariesScanConfig{namedBinariesScanConfig1}
	context.binariesTarFileName = dir + "/test.tar"

	/* execute */
	createBinariesTarFile(&context)

	/* test */
	list, _ := sechubUtil.ListContentOfTarFile(context.binariesTarFileName)
	sechubTestUtil.AssertSize(list, 2, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedBinariesScanConfig1.Name+"/"+filepath1, t)
	sechubTestUtil.AssertContains(list, archiveDataPrefix+"/"+namedBinariesScanConfig1.Name+"/"+filepath2, t)
}

func Test_createBinariesTarFile_HandlesRemoteDataSection(t *testing.T) {
	/* prepare */
	var context Context
	var config Config
	var sechubConfig SecHubConfig

	context.config = &config
	context.sechubConfig = &sechubConfig

	dir := sechubTestUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)
	context.binariesTarFileName = dir + "/testoutput.tar"
	scanConfig := NamedBinariesScanConfig{
		Name: "remote-data-section-test",
		// Currently the client does not parse the remote data section. So we leave it empty for now.
	}
	context.sechubConfig.Data.Binaries = []NamedBinariesScanConfig{scanConfig}

	/* execute */

	// Capture stdout
	rescueStdout := os.Stdout
	r, w, _ := os.Pipe()
	os.Stdout = w

	err := createBinariesTarFile(&context)

	// Stop capturing stdout
	w.Close()
	out, _ := io.ReadAll(r)
	os.Stdout = rescueStdout

	/* test */
	sechubTestUtil.AssertNoError(err, t)
	sechubTestUtil.AssertStringContains(string(out), sechubUtil.TarFileNotCreated, t)
}
