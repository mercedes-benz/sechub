// SPDX-License-Identifier: MIT
package util

import (
	"archive/zip"
	"fmt"
	"os"
	"strings"
	"testing"

	sechubUtil "mercedes-benz.com/sechub/testutil"
)

func TestZipFileBeingPartOfScannedFoldersIsRejected(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	sechubUtil.CreateTestDirectory(dirname1, 0755, t)

	filename1 := dirname1 + "/file1.txt"

	content := []byte("Hello world!\n")
	sechubUtil.CreateTestFile(filename1, 0644, content, t)

	// path to zipfile is also part of added files - because in dirname1
	path := dirname1 + "/testoutput.zip"

	newZipFile, _ := os.Create(path)

	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName:        path,
		ZipWriter:          zipWriter,
		PrefixInZip:        "",
		Folders:            []string{dirname1},
		SourceCodePatterns: []string{".txt", ".zip"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.AssertErrorHasExpectedMessage(err, TargetZipFileLoop, t)
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
	newZipFile, _ := os.Create(path)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName:        path,
		ZipWriter:          zipWriter,
		PrefixInZip:        "",
		Folders:            []string{dirname1, dirname2},
		SourceCodePatterns: []string{".txt"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	sechubUtil.AssertFileExists(path, t)
	sechubUtil.AssertMinimalFileSize(path, 300, t) // check if zip file is empty

	list := readContentOfZipFileTest(path, t)

	sechubUtil.AssertContains(list, strings.TrimPrefix(filename1, "/"), t)
	sechubUtil.AssertContains(list, strings.TrimPrefix(filename2, "/"), t)
	sechubUtil.AssertContains(list, strings.TrimPrefix(filename3, "/"), t)
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
	newZipFile, _ := os.Create(path)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName:        path,
		ZipWriter:          zipWriter,
		PrefixInZip:        "",
		Folders:            []string{dirname1, dirname2},
		Excludes:           []string{"**/file3.txt", "**/f*0*.txt"},
		SourceCodePatterns: []string{".txt"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	sechubUtil.AssertFileExists(path, t)
	sechubUtil.AssertMinimalFileSize(path, 300, t) // check if zip file is empty

	list := readContentOfZipFileTest(path, t)

	sechubUtil.AssertContainsNot(list, strings.TrimPrefix(filename0, "/"), t) // this file may not be inside, because excluded! (/sub1/file0.txt)
	sechubUtil.AssertContains(list, strings.TrimPrefix(filename1, "/"), t)    // this must remain
	sechubUtil.AssertContains(list, strings.TrimPrefix(filename2, "/"), t)    // this must remain
	sechubUtil.AssertContainsNot(list, strings.TrimPrefix(filename3, "/"), t) // this file may not be inside, because excluded!
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
	newZipFile, _ := os.Create(path)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName:        path,
		ZipWriter:          zipWriter,
		PrefixInZip:        "",
		Folders:            []string{dirname1},
		SourceCodePatterns: []string{".c", ".go"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	sechubUtil.AssertFileExists(path, t)
	sechubUtil.AssertMinimalFileSize(path, 300, t) // check if zip file is empty

	list := readContentOfZipFileTest(path, t)

	sechubUtil.AssertContainsNot(list, strings.TrimPrefix(filename0, "/"), t) // file must not be in zip
	sechubUtil.AssertContains(list, strings.TrimPrefix(filename1, "/"), t)    // file must exist
	sechubUtil.AssertContainsNot(list, strings.TrimPrefix(filename2, "/"), t) // file must not be in zip
	sechubUtil.AssertContains(list, strings.TrimPrefix(filename3, "/"), t)    // file must exist
	sechubUtil.AssertContainsNot(list, strings.TrimPrefix(filename4, "/"), t) // file must not be in zip
	sechubUtil.AssertSize(list, 2, t)                                         // we expect 2 files in the list
}

func TestZipFileNonExistingFolderIsRejected(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/nonexistant"

	path := dir + "/testoutput.zip"
	newZipFile, _ := os.Create(path)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName: path,
		ZipWriter:   zipWriter,
		PrefixInZip: "",
		Folders:     []string{dirname1},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.AssertErrorHasExpectedStartMessage(err, "Folder not found:", t)
}

func TestZipFileContainsRelativeSourceFolders(t *testing.T) {
	/* prepare */
	RelativeTmpTestDir := "sechub-cli-tmptest"
	sechubUtil.CreateTestDirectory(RelativeTmpTestDir, 0755, t)
	defer os.RemoveAll(RelativeTmpTestDir)

	dirname1 := RelativeTmpTestDir + "/" + "sub1"
	dirname2 := RelativeTmpTestDir + "/" + "sub2"
	filepath1 := dirname1 + "/file1.txt"
	filepath2 := dirname2 + "/file1.txt"

	// create dirs
	sechubUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubUtil.CreateTestDirectory(dirname2, 0755, t)

	// create files
	content := []byte("Hello world!\n")
	sechubUtil.CreateTestFile(filepath1, 0644, content, t)
	sechubUtil.CreateTestFile(filepath2, 0644, content, t)

	zipfilepath := RelativeTmpTestDir + "/testoutput.zip"
	newZipFile, _ := os.Create(zipfilepath)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName:        zipfilepath,
		ZipWriter:          zipWriter,
		PrefixInZip:        "",
		Folders:            []string{dirname1, dirname2},
		SourceCodePatterns: []string{".txt"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	list := readContentOfZipFileTest(zipfilepath, t)

	sechubUtil.AssertContains(list, filepath1, t)
	sechubUtil.AssertContains(list, filepath2, t)
}

func TestZipFileContainsRelativeFoldersOutsideCurrent(t *testing.T) {
	/* prepare */
	RelativeTmpTestDir := "./../sechub-cli-tmptest"
	sechubUtil.CreateTestDirectory(RelativeTmpTestDir, 0755, t)
	defer os.RemoveAll(RelativeTmpTestDir)

	filepath1 := RelativeTmpTestDir + "/file1.txt"

	// create files
	content := []byte("Hello world!\n")
	sechubUtil.CreateTestFile(filepath1, 0644, content, t)

	zipfilepath := RelativeTmpTestDir + "/testoutput.zip"
	newZipFile, _ := os.Create(zipfilepath)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName:        zipfilepath,
		ZipWriter:          zipWriter,
		PrefixInZip:        "",
		Folders:            []string{RelativeTmpTestDir},
		SourceCodePatterns: []string{".txt"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	list := readContentOfZipFileTest(zipfilepath, t)
	// "./../sechub-cli-tmptest/file1.txt" becomes "sechub-cli-tmptest/file1.txt" in zip file
	sechubUtil.AssertContains(list, "sechub-cli-tmptest/file1.txt", t)
}

func Example_zipDetectsNonExistingFiles() {
	/* prepare */
	var t testing.T
	RelativeTmpTestDir := "sechub-cli-tmptest"
	sechubUtil.CreateTestDirectory(RelativeTmpTestDir, 0755, &t)
	defer os.RemoveAll(RelativeTmpTestDir)

	zipfilepath := RelativeTmpTestDir + "/testoutput.zip"
	newZipFile, _ := os.Create(zipfilepath)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName: zipfilepath,
		ZipWriter:   zipWriter,
		PrefixInZip: "",
		Files:       []string{RelativeTmpTestDir + "/non-existing-file.txt"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	fmt.Println(err)
	// Output:
	// Folder created: "sechub-cli-tmptest"
	// open sechub-cli-tmptest/non-existing-file.txt: no such file or directory
}

func TestZipIsSkippingSymlinks(t *testing.T) {
	/* prepare */
	RelativeTmpTestDir := "sechub-cli-tmptest"
	sechubUtil.CreateTestDirectory(RelativeTmpTestDir, 0755, t)
	defer os.RemoveAll(RelativeTmpTestDir)

	filepath1 := RelativeTmpTestDir + "/realfile.txt"
	filepath2 := RelativeTmpTestDir + "/good_symlink.txt"
	filepath3 := RelativeTmpTestDir + "/dangling_symlink.txt"

	// create files
	content := []byte("Hello world!\n")
	sechubUtil.CreateTestFile(filepath1, 0644, content, t)
	sechubUtil.CreateTestSymlink(filepath2, 0644, "realfile.txt", t)
	sechubUtil.CreateTestSymlink(filepath3, 0644, "doesnotexist.txt", t)

	zipfilepath := RelativeTmpTestDir + "/testoutput.zip"
	newZipFile, _ := os.Create(zipfilepath)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName:        zipfilepath,
		ZipWriter:          zipWriter,
		PrefixInZip:        "",
		Folders:            []string{RelativeTmpTestDir},
		SourceCodePatterns: []string{".txt"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	list := readContentOfZipFileTest(zipfilepath, t)

	sechubUtil.AssertContains(list, filepath1, t)
	// None of the symlinks must be in the zip file
	sechubUtil.AssertContainsNot(list, filepath2, t)
	sechubUtil.AssertContainsNot(list, filepath3, t)
}


/* -------------------------------------*/
/* --------- Helpers -------------------*/
/* -------------------------------------*/

func readContentOfZipFileTest(path string, t *testing.T) []string {

	list, err := ReadContentOfZipFile(path)
	sechubUtil.Check(err, t)

	return list
}

func TestZipFileCanBeCreated_with_absolute_path_and_exclude_patterns_applied(t *testing.T) {
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
	newZipFile, _ := os.Create(path)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName:        path,
		ZipWriter:          zipWriter,
		PrefixInZip:        "test123/",
		Folders:            []string{dir},
		Excludes:           []string{dirname2 + "/**"},
		SourceCodePatterns: []string{".txt"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	sechubUtil.AssertFileExists(path, t)
	sechubUtil.AssertMinimalFileSize(path, 300, t) // check if zip file is empty

	list := readContentOfZipFileTest(path, t)

	sechubUtil.AssertContains(list, strings.TrimPrefix("test123"+filename0, "/"), t)    // this must remain
	sechubUtil.AssertContains(list, strings.TrimPrefix("test123"+filename1, "/"), t)    // this must remain
	sechubUtil.AssertContainsNot(list, strings.TrimPrefix("test123"+filename2, "/"), t) // excluded!
	sechubUtil.AssertContainsNot(list, strings.TrimPrefix("test123"+filename3, "/"), t) // excluded!
	sechubUtil.AssertSize(list, 2, t)
}

func TestZipFileCanBeCreated_with_relative_path_and_exclude_patterns_applied(t *testing.T) {
	/* prepare */
	RelativeTmpTestDir := "sechub-cli-tmptest"
	sechubUtil.CreateTestDirectory(RelativeTmpTestDir, 0755, t)
	defer os.RemoveAll(RelativeTmpTestDir)

	dirname1 := RelativeTmpTestDir + "/sub1"
	dirname2 := RelativeTmpTestDir + "/sub2"
	dirname3 := RelativeTmpTestDir + "/sub2/sub3"
	filepath1 := dirname1 + "/file1.txt"
	filepath2 := dirname2 + "/file1.txt"
	filepath3 := dirname3 + "/file1.txt"

	// create dirs
	sechubUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubUtil.CreateTestDirectory(dirname3, 0755, t)

	// create files
	content := []byte("Hello world!\n")
	sechubUtil.CreateTestFile(filepath1, 0644, content, t)
	sechubUtil.CreateTestFile(filepath2, 0644, content, t)
	sechubUtil.CreateTestFile(filepath3, 0644, content, t)

	zipfilepath := RelativeTmpTestDir + "/testoutput.zip"
	newZipFile, _ := os.Create(zipfilepath)
	zipWriter := zip.NewWriter(newZipFile)

	config := ZipConfig{
		ZipFileName:        zipfilepath,
		ZipWriter:          zipWriter,
		PrefixInZip:        "test123/",
		Folders:            []string{RelativeTmpTestDir},
		Excludes:           []string{dirname2 + "/**"},
		SourceCodePatterns: []string{".txt"},
	}

	/* execute */
	err := Zip(&config)
	zipWriter.Close()
	newZipFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	list := readContentOfZipFileTest(zipfilepath, t)

	sechubUtil.AssertContains(list, "test123/"+filepath1, t)
	sechubUtil.AssertContainsNot(list, "test123/"+filepath2, t)
	sechubUtil.AssertContainsNot(list, "test123/"+filepath3, t)
}
