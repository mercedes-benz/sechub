// SPDX-License-Identifier: MIT
package util

import (
	"archive/zip"
	"os"
	"testing"

	sechubUtil "daimler.com/sechub/testutil"
)

func TestZipFileBeingPartOfScannedFoldersIsRejected(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	sechubUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubUtil.CreateTestDirectory(dirname3, 0755, t)

	filename1 := dirname1 + "/file1.txt"
	filename2 := dirname2 + "/file2.txt"
	filename3 := dirname3 + "/file3.txt"

	content := []byte("Hello world!\n")
	sechubUtil.CreateTestFile(filename1, 0644, content, t)
	sechubUtil.CreateTestFile(filename2, 0644, content, t)
	sechubUtil.CreateTestFile(filename3, 0644, content, t)

	// path to zipfile is also part of added files - because in dirname1
	path := dirname1 + "/testoutput.zip"

	/* execute */
	err := ZipFolders(path, &ZipConfig{Folders: []string{dirname1, dirname2}, SourceCodePatterns: []string{"*.txt"}}, false)

	/* test */
	sechubUtil.AssertErrorHasExpectedMessage(err, "Target zipfile would be part of zipped content, leading to infinite loop. Please change target path!", t)

}

func TestZipFileEmptyIsRejected(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	sechubUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubUtil.CreateTestDirectory(dirname3, 0755, t)

	//  we do only add empty folders, but not any content - so zip file will be empty. The implementation
	//  must ensure that this cannot happen because otherwise we upload empty data which will always have a
	//  greeen result
	path := dir + "/testoutput.zip"

	/* execute */
	err := ZipFolders(path, &ZipConfig{Folders: []string{dirname1, dirname2}}, false)

	/* test */
	sechubUtil.AssertErrorHasExpectedMessage(err, "Zipfile has no content!", t)

}

func TestZipFileCanBeCreated(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	sechubUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubUtil.CreateTestDirectory(dirname3, 0755, t)

	filename1 := dirname1 + "/file1.txt"
	filename2 := dirname2 + "/file2.txt"
	filename3 := dirname3 + "/file3.txt"

	content := []byte("Hello world!\n")
	sechubUtil.CreateTestFile(filename1, 0644, content, t)
	sechubUtil.CreateTestFile(filename2, 0644, content, t)
	sechubUtil.CreateTestFile(filename3, 0644, content, t)

	path := dir + "/testoutput.zip"

	/* execute */
	err := ZipFolders(path, &ZipConfig{Folders: []string{dirname1, dirname2}, SourceCodePatterns: []string{".txt"}}, false)

	/* test */
	sechubUtil.Check(err, t)

	sechubUtil.AssertFileExists(path, t)
	sechubUtil.AssertMinimalFileSize(path, 300, t) // check if zip file is empty

	list := readContentOfZipFile(path, t)

	sechubUtil.AssertContains(list, "file1.txt", t)
	sechubUtil.AssertContains(list, "file2.txt", t)
	sechubUtil.AssertContains(list, "sub3/file3.txt", t)
	sechubUtil.AssertSize(list, 3, t)

	// cross check: checksum calculated twice for generated zip does always be the same
	checksum1 := CreateChecksum(path)
	checksum2 := CreateChecksum(path)
	sechubUtil.AssertEquals(checksum1, checksum2, t)
}

func TestZipFileCanBeCreated_with_exclude_patterns_applied(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	sechubUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubUtil.CreateTestDirectory(dirname3, 0755, t)

	filename0 := dirname1 + "/file0.txt"
	filename1 := dirname1 + "/file1.txt"
	filename2 := dirname2 + "/file2.txt"
	filename3 := dirname3 + "/file3.txt"

	content := []byte("Hello world!\n")
	sechubUtil.CreateTestFile(filename0, 0644, content, t)
	sechubUtil.CreateTestFile(filename1, 0644, content, t)
	sechubUtil.CreateTestFile(filename2, 0644, content, t)
	sechubUtil.CreateTestFile(filename3, 0644, content, t)

	path := dir + "/testoutput.zip"

	/* execute */
	config := ZipConfig{
		Folders:            []string{dirname1, dirname2},
		Excludes:           []string{"**/file3.txt", "f*0*.txt"},
		SourceCodePatterns: []string{".txt"}}
	err := ZipFolders(path, &config, false)

	/* test */
	sechubUtil.Check(err, t)

	sechubUtil.AssertFileExists(path, t)
	sechubUtil.AssertMinimalFileSize(path, 300, t) // check if zip file is empty

	list := readContentOfZipFile(path, t)

	sechubUtil.AssertContainsNot(list, "file0.txt", t)      // this file may not be inside, because excluded! (/sub1/file0.txt)
	sechubUtil.AssertContains(list, "file1.txt", t)         // this must remain
	sechubUtil.AssertContains(list, "file2.txt", t)         // this must remain
	sechubUtil.AssertContainsNot(list, "sub3/file3.txt", t) // this file may not be inside, because excluded!
	sechubUtil.AssertSize(list, 2, t)

}

func TestZipFileCanBeCreated_and_contains_only_sourcefiles(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub1/sub2"
	dirname3 := dir + "/node_modules" // directory should be skipped by default

	sechubUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubUtil.CreateTestDirectory(dirname3, 0755, t)

	filename0 := dirname1 + "/file0.txt"         // should be ignored
	filename1 := dirname1 + "/file1.c"           // should be added
	filename2 := dirname2 + "/file2.jpg"         // should be ignored
	filename3 := dirname2 + "/file3.go"          // shoud be added
	filename4 := dirname3 + "/nodejs-libfile.js" // shoud be ignored

	content := []byte("Hello world!\n")
	sechubUtil.CreateTestFile(filename0, 0644, content, t)
	sechubUtil.CreateTestFile(filename1, 0644, content, t)
	sechubUtil.CreateTestFile(filename2, 0644, content, t)
	sechubUtil.CreateTestFile(filename3, 0644, content, t)
	sechubUtil.CreateTestFile(filename4, 0644, content, t)

	path := dir + "/testoutput.zip"

	/* execute */
	config := ZipConfig{Folders: []string{dirname1}, SourceCodePatterns: []string{".c", ".go"}}
	err := ZipFolders(path, &config, false)

	/* test */
	sechubUtil.Check(err, t)

	sechubUtil.AssertFileExists(path, t)
	sechubUtil.AssertMinimalFileSize(path, 300, t) // check if zip file is empty

	list := readContentOfZipFile(path, t)

	sechubUtil.AssertContainsNot(list, "file0.txt", t)         // file must not be in zip
	sechubUtil.AssertContains(list, "file1.c", t)              // file must exist
	sechubUtil.AssertContainsNot(list, "sub2/file2.jpg", t)    // file must not be in zip
	sechubUtil.AssertContains(list, "sub2/file3.go", t)        // file must exist
	sechubUtil.AssertSize(list, 2, t)                          // we expect 2 files in the list
	sechubUtil.AssertContainsNot(list, "nodejs-libfile.js", t) // file must not be in zip
}

func TestZipFileNonExistingFolderIsRejected(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/nonexistant"

	path := dir + "/testoutput.zip"

	/* execute */
	err := ZipFolders(path, &ZipConfig{Folders: []string{dirname1}}, false)

	/* test */
	sechubUtil.AssertErrorHasExpectedStartMessage(err, "Folder not found:", t)

}

/* -------------------------------------*/
/* --------- Helpers -------------------*/
/* -------------------------------------*/
func readContentOfZipFile(path string, t *testing.T) []string {

	zipfile, err := zip.OpenReader(path)
	sechubUtil.Check(err, t)
	defer zipfile.Close()

	list := []string{}
	for _, file := range zipfile.File {
		name := ConvertBackslashPath(file.Name)
		list = append(list, name)
	}
	return list
}
