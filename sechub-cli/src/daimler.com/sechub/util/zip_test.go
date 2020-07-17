// SPDX-License-Identifier: MIT
package util

import (
	"archive/zip"
	"os"
	"testing"

	. "daimler.com/sechub/testutil"
)

func TestZipFileBeingPartOfScannedFoldersIsRejected(t *testing.T) {
	/* prepare */
	dir := InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	CreateTestDirectory(dirname1, 0755, t)
	CreateTestDirectory(dirname2, 0755, t)
	CreateTestDirectory(dirname3, 0755, t)

	filename1 := dirname1 + "/file1.txt"
	filename2 := dirname2 + "/file2.txt"
	filename3 := dirname3 + "/file3.txt"

	CreateTestFile(filename1, 0644, t)
	CreateTestFile(filename2, 0644, t)
	CreateTestFile(filename3, 0644, t)

	// path to zipfile is also part of added files - because in dirname1
	path := dirname1 + "/testoutput.zip"

	/* execute */
	err := ZipFolders(path, &ZipConfig{Folders: []string{dirname1, dirname2}, SourceCodePatterns: []string{"*.txt"}})

	/* test */
	AssertErrorHasExpectedMessage(err, "Target zipfile would be part of zipped content, leading to infinite loop. Please change target path!", t)

	}

func TestZipFileEmptyIsRejected(t *testing.T) {
	/* prepare */
	dir := InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	CreateTestDirectory(dirname1, 0755, t)
	CreateTestDirectory(dirname2, 0755, t)
	CreateTestDirectory(dirname3, 0755, t)

	//  we do only add empty folders, but not any content - so zip file will be empty. The implementation
	//  must ensure that this cannot happen because otherwise we upload empty data which will always have a
	//  greeen result
	path := dir + "/testoutput.zip"

	/* execute */
	err := ZipFolders(path, &ZipConfig{Folders: []string{dirname1, dirname2}})

	/* test */
	AssertErrorHasExpectedMessage(err, "Zipfile has no content!", t)

}

func TestZipFileCanBeCreated(t *testing.T) {
	/* prepare */
	dir := InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	CreateTestDirectory(dirname1, 0755, t)
	CreateTestDirectory(dirname2, 0755, t)
	CreateTestDirectory(dirname3, 0755, t)

	filename1 := dirname1 + "/file1.txt"
	filename2 := dirname2 + "/file2.txt"
	filename3 := dirname3 + "/file3.txt"

	CreateTestFile(filename1, 0644, t)
	CreateTestFile(filename2, 0644, t)
	CreateTestFile(filename3, 0644, t)

	path := dir + "/testoutput.zip"

	/* execute */
	err := ZipFolders(path, &ZipConfig{Folders: []string{dirname1, dirname2}, SourceCodePatterns: []string{".txt"}})

	/* test */
	Check(err, t)

	AssertFileExists(path, t)
	AssertMinimalFileSize(path, 300, t) // check if zip file is empty

	list := readContentOfZipFile(path, t)
	
	AssertContains(list, "file1.txt", t)
	AssertContains(list, "file2.txt", t)
	AssertContains(list, "sub3/file3.txt", t)
	AssertSize(list, 3, t)

	// cross check: checksum calculated twice for generated zip does always be the same
	checksum1 := CreateChecksum(path)
	checksum2 := CreateChecksum(path)
	AssertEquals(checksum1, checksum2, t)
}

func TestZipFileCanBeCreated_with_exclude_patterns_applied(t *testing.T) {
	/* prepare */
	dir := InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	CreateTestDirectory(dirname1, 0755, t)
	CreateTestDirectory(dirname2, 0755, t)
	CreateTestDirectory(dirname3, 0755, t)

	filename0 := dirname1 + "/file0.txt"
	filename1 := dirname1 + "/file1.txt"
	filename2 := dirname2 + "/file2.txt"
	filename3 := dirname3 + "/file3.txt"

	CreateTestFile(filename0, 0644, t)
	CreateTestFile(filename1, 0644, t)
	CreateTestFile(filename2, 0644, t)
	CreateTestFile(filename3, 0644, t)

	path := dir + "/testoutput.zip"

	/* execute */
	config := ZipConfig{
		Folders:            []string{dirname1, dirname2},
		Excludes:           []string{"**/file3.txt", "f*0*.txt"},
		SourceCodePatterns: []string{".txt"}}
	err := ZipFolders(path, &config)

	/* test */
	Check(err, t)

	AssertFileExists(path, t)
	AssertMinimalFileSize(path, 300, t) // check if zip file is empty

    list := readContentOfZipFile(path, t)

	AssertContainsNot(list, "file0.txt", t)      // this file may not be inside, because excluded! (/sub1/file0.txt)
	AssertContains(list, "file1.txt", t)         // this must remain
	AssertContains(list, "file2.txt", t)         // this must remain
	AssertContainsNot(list, "sub3/file3.txt", t) // this file may not be inside, because excluded!
	AssertSize(list, 2, t)

}

func TestZipFileCanBeCreated_and_contains_only_sourcefiles(t *testing.T) {
	/* prepare */
	dir := InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub1/sub2"

	CreateTestDirectory(dirname1, 0755, t)
	CreateTestDirectory(dirname2, 0755, t)

	filename0 := dirname1 + "/file0.txt" // should be ignored
	filename1 := dirname1 + "/file1.c"   // should be added
	filename2 := dirname2 + "/file2.jpg" // should be ignored
	filename3 := dirname2 + "/file3.go"  // shoud be added

	CreateTestFile(filename0, 0644, t)
	CreateTestFile(filename1, 0644, t)
	CreateTestFile(filename2, 0644, t)
	CreateTestFile(filename3, 0644, t)

	path := dir + "/testoutput.zip"

	/* execute */
	config := ZipConfig{Folders: []string{dirname1}, SourceCodePatterns: []string{".c", ".go"}}
	err := ZipFolders(path, &config)

	/* test */
	Check(err, t)
	
	AssertFileExists(path, t)
	AssertMinimalFileSize(path, 300, t) // check if zip file is empty

    list := readContentOfZipFile(path, t)
	
	AssertContainsNot(list, "file0.txt", t)      // file must not be in zip
	AssertContains(list, "file1.c", t)           // file must exist
	AssertContainsNot(list, "sub2/file2.jpg", t) // file must not be in zip
	AssertContains(list, "sub2/file3.go", t)     // file must exist
	AssertSize(list, 2, t)                       // we expect 2 files in the list
}

func TestZipFileNonExistingFolderIsRejected(t *testing.T) {
	/* prepare */
	dir := InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/nonexistant"

	path := dir + "/testoutput.zip"

	/* execute */
	err := ZipFolders(path, &ZipConfig{Folders: []string{dirname1}})

	/* test */
	AssertErrorHasExpectedStartMessage(err, "Folder not found:", t)

}

/* -------------------------------------*/
/* --------- Helpers -------------------*/
/* -------------------------------------*/
func readContentOfZipFile(path string, t *testing.T) []string {

	zipfile, err := zip.OpenReader(path)
	Check(err, t)
	defer zipfile.Close()

	list := []string{}
	for _, file := range zipfile.File {
		name := ConvertBackslashPath(file.Name)
		list = append(list, name)
	}
	return list
}
