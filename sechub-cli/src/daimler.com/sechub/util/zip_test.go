// SPDX-License-Identifier: MIT
package util

import (
	"strings"

	. "daimler.com/sechub/testutil"
	//"path/filepath"
	"archive/zip"
	"fmt"
	"io/ioutil"
	"log"
	"os"

	//"strings"
	//	"path/filepath"
	"testing"
)

func TestZipFileBeingPartOfScannedFoldersIsRejected(t *testing.T) {
	/* prepare */
	dir, err := ioutil.TempDir("", "sechub-cli-temp")
	if err != nil {
		log.Fatal(err)
	}
	defer os.RemoveAll(dir)

	if _, err := os.Stat(dir); os.IsNotExist(err) {
		err = os.MkdirAll(dir, 0644)
		log.Printf("Created not existing directory:%s", dir)
		if err != nil {
			t.Fatalf("cannot create test zip folder output - error: %s", err)
		}
	}
	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	os.Mkdir(dirname1, 0777)
	os.Mkdir(dirname2, 0777)
	os.Mkdir(dirname3, 0777)

	filename1 := dirname1 + "/file1.txt"
	filename2 := dirname2 + "/file2.txt"
	filename3 := dirname3 + "/file3.txt"

	d1 := []byte("hello\ngo1\n")
	err = ioutil.WriteFile(filename1, d1, 0644)
	Check(err, t)
	fmt.Printf("written file1:%s\n", filename1)

	d2 := []byte("hello\ngo2\n")
	err = ioutil.WriteFile(filename2, d2, 0644)
	Check(err, t)
	fmt.Printf("written file2:%s\n", filename2)

	d3 := []byte("hello\ngo3\n")
	err = ioutil.WriteFile(filename3, d3, 0644)
	Check(err, t)
	fmt.Printf("written file3:%s\n", filename3)

	/* path to zipfile is also part of added files - because in dirname1*/
	path := dirname1 + "/testoutput.zip"

	/* execute */
	err = ZipFolders(path, &ZipConfig{Folders: []string{dirname1, dirname2}, SourceCodePatterns: []string{"*.txt"}})

	/* test */
	expectedErrMsg := "Target zipfile would be part of zipped content, leading to infinite loop. Please change target path!"

	if err == nil {
		t.Fatalf("No error returned!")
	}

	if err.Error() != expectedErrMsg {
		t.Fatalf("Wrong Error\nActual   = %v, \nExpected = %v.", err.Error(), expectedErrMsg)
	}

}

func TestZipFileEmptyIsRejected(t *testing.T) {
	/* prepare */
	dir, err := ioutil.TempDir("", "sechub-cli-temp")
	if err != nil {
		log.Fatal(err)
	}
	defer os.RemoveAll(dir)

	if _, err := os.Stat(dir); os.IsNotExist(err) {
		err = os.MkdirAll(dir, 0644)
		log.Printf("Created not existing directory:%s", dir)
		if err != nil {
			t.Fatalf("cannot create test zip folder output - error: %s", err)
		}
	}
	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	os.Mkdir(dirname1, 0777)
	os.Mkdir(dirname2, 0777)
	os.Mkdir(dirname3, 0777)

	/* we do only add empty folders, but not any content - so zip file will be empty. The implementation
	 * must ensure that this cannot happen because otherwise we upload empty data which will always have a
	 * greeen result
	 */
	path := dir + "/testoutput.zip"

	/* execute */
	err = ZipFolders(path, &ZipConfig{Folders: []string{dirname1, dirname2}})
	expectedErrMsg := "Zipfile has no content!"

	/* test */
	if err == nil {
		t.Fatalf("No error returned!")
	}

	if err.Error() != expectedErrMsg {
		t.Fatalf("Error actual = %v, and Expected = %v.", err.Error(), expectedErrMsg)
	}

}

func TestZipFileCanBeCreated(t *testing.T) {
	/* prepare */
	dir, err := ioutil.TempDir("", "sechub-cli-temp")
	if err != nil {
		log.Fatal(err)
	}
	defer os.RemoveAll(dir)

	if _, err := os.Stat(dir); os.IsNotExist(err) {
		err = os.MkdirAll(dir, 0644)
		log.Printf("Created not existing directory:%s", dir)
		if err != nil {
			t.Fatalf("cannot create test zip folder output - error: %s", err)
		}
	}
	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	os.Mkdir(dirname1, 0777)
	os.Mkdir(dirname2, 0777)
	os.Mkdir(dirname3, 0777)

	filename1 := dirname1 + "/file1.txt"
	filename2 := dirname2 + "/file2.txt"
	filename3 := dirname3 + "/file3.txt"

	d1 := []byte("hello\ngo1\n")
	err = ioutil.WriteFile(filename1, d1, 0644)
	Check(err, t)
	fmt.Printf("written file1:%s\n", filename1)

	d2 := []byte("hello\ngo2\n")
	err = ioutil.WriteFile(filename2, d2, 0644)
	Check(err, t)
	fmt.Printf("written file2:%s\n", filename2)

	d3 := []byte("hello\ngo3\n")
	err = ioutil.WriteFile(filename3, d3, 0644)
	Check(err, t)
	fmt.Printf("written file3:%s\n", filename3)

	path := dir + "/testoutput.zip"

	/* execute */
	err = ZipFolders(path, &ZipConfig{Folders: []string{dirname1, dirname2}, SourceCodePatterns: []string{".txt"}})

	/* ---- */
	/* test */
	/* ---- */
	Check(err, t)
	if _, err := os.Stat(path); os.IsNotExist(err) {
		t.Fatalf("resulted zipfile does not exist!")
	}
	fi, err2 := os.Stat(path)
	if err2 != nil {
		t.Fatalf("found error: %s", err2)
	}
	if fi.Size() < 300 {
		t.Fatalf("resulted empty zip file!!!")
	}

	/* read content of zipfile*/
	zf, err := zip.OpenReader(path)
	Check(err, t)
	defer zf.Close()

	list := []string{}
	for _, file := range zf.File {
		name := ConvertBackslashPath(file.Name)
		list = append(list, name)
	}
	AssertContains(list, "file1.txt", t)
	AssertContains(list, "file2.txt", t)
	AssertContains(list, "sub3/file3.txt", t)
	AssertSize(list, 3, t)

	/* cross check: checksum calculated twice for generated zip does always be the same */
	checksum1 := CreateChecksum(path)
	checksum2 := CreateChecksum(path)
	AssertEquals(checksum1, checksum2, t)
}

func TestZipFileCanBeCreated_with_exclude_patterns_applied(t *testing.T) {
	/* prepare */
	dir, err := ioutil.TempDir("", "sechub-cli-temp")
	if err != nil {
		log.Fatal(err)
	}
	defer os.RemoveAll(dir)

	if _, err := os.Stat(dir); os.IsNotExist(err) {
		err = os.MkdirAll(dir, 0644)
		log.Printf("Created not existing directory:%s", dir)
		if err != nil {
			t.Fatalf("cannot create test zip folder output - error: %s", err)
		}
	}
	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	os.Mkdir(dirname1, 0777)
	os.Mkdir(dirname2, 0777)
	os.Mkdir(dirname3, 0777)

	filename0 := dirname1 + "/file0.txt"
	filename1 := dirname1 + "/file1.txt"
	filename2 := dirname2 + "/file2.txt"
	filename3 := dirname3 + "/file3.txt"

	d0 := []byte("hello\ngo0\n")
	err = ioutil.WriteFile(filename0, d0, 0644)
	Check(err, t)
	fmt.Printf("written file0:%s\n", filename0)

	d1 := []byte("hello\ngo1\n")
	err = ioutil.WriteFile(filename1, d1, 0644)
	Check(err, t)
	fmt.Printf("written file1:%s\n", filename1)

	d2 := []byte("hello\ngo2\n")
	err = ioutil.WriteFile(filename2, d2, 0644)
	Check(err, t)
	fmt.Printf("written file2:%s\n", filename2)

	d3 := []byte("hello\ngo3\n")
	err = ioutil.WriteFile(filename3, d3, 0644)
	Check(err, t)
	fmt.Printf("written file3:%s\n", filename3)

	path := dir + "/testoutput.zip"

	/* execute */
	config := ZipConfig{
		Folders:            []string{dirname1, dirname2},
		Excludes:           []string{"**/file3.txt", "f*0*.txt"},
		SourceCodePatterns: []string{".txt"}}
	err = ZipFolders(path, &config)

	/* ---- */
	/* test */
	/* ---- */
	Check(err, t)
	if _, err := os.Stat(path); os.IsNotExist(err) {
		t.Fatalf("resulted zipfile does not exist!")
	}
	fi, err2 := os.Stat(path)
	if err2 != nil {
		t.Fatalf("found error: %s", err2)
	}
	if fi.Size() < 300 {
		t.Fatalf("resulted empty zip file!!!")
	}

	/* read content of zipfile*/
	zf, err := zip.OpenReader(path)
	Check(err, t)
	defer zf.Close()

	list := []string{}
	for _, file := range zf.File {
		name := ConvertBackslashPath(file.Name)
		list = append(list, name)
	}
	AssertContainsNot(list, "file0.txt", t)      // this file may not be inside, because excluded! (/sub1/file0.txt)
	AssertContains(list, "file1.txt", t)         // this must remain
	AssertContains(list, "file2.txt", t)         // this must remain
	AssertContainsNot(list, "sub3/file3.txt", t) // this file may not be inside, because excluded!
	AssertSize(list, 2, t)

}

func TestZipFileCanBeCreated_and_contains_only_sourcefiles(t *testing.T) {
	/* prepare */
	dir, err := ioutil.TempDir("", "sechub-cli-temp")
	if err != nil {
		log.Fatal(err)
	}
	defer os.RemoveAll(dir)

	if _, err := os.Stat(dir); os.IsNotExist(err) {
		err = os.MkdirAll(dir, 0644)
		log.Printf("Created not existing directory:%s", dir)
		if err != nil {
			t.Fatalf("cannot create test zip folder output - error: %s", err)
		}
	}
	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub1/sub2"

	os.Mkdir(dirname1, 0777)
	os.Mkdir(dirname2, 0777)

	filename0 := dirname1 + "/file0.txt" // should be ignored
	filename1 := dirname1 + "/file1.c"   // should be added
	filename2 := dirname2 + "/file2.jpg" // should be ignored
	filename3 := dirname2 + "/file3.go"  // shoud be added

	err = ioutil.WriteFile(filename0, []byte("hello world0\n"), 0644)
	Check(err, t)
	fmt.Printf("written file0: %s\n", filename0)

	err = ioutil.WriteFile(filename1, []byte("hello world1\n"), 0644)
	Check(err, t)
	fmt.Printf("written file1: %s\n", filename1)

	err = ioutil.WriteFile(filename2, []byte("hello world2\n"), 0644)
	Check(err, t)
	fmt.Printf("written file2: %s\n", filename2)

	err = ioutil.WriteFile(filename3, []byte("hello world3\n"), 0644)
	Check(err, t)
	fmt.Printf("written file3: %s\n", filename3)

	path := dir + "/testoutput.zip"

	/* execute */
	config := ZipConfig{Folders: []string{dirname1}, SourceCodePatterns: []string{".c", ".go"}}
	err = ZipFolders(path, &config)

	/* ---- */
	/* test */
	/* ---- */
	Check(err, t)
	if _, err := os.Stat(path); os.IsNotExist(err) {
		t.Fatalf("resulted zipfile does not exist!")
	}
	fi, err2 := os.Stat(path)
	if err2 != nil {
		t.Fatalf("found error: %s", err2)
	}
	if fi.Size() < 300 {
		t.Fatalf("resulted empty zip file!!!")
	}

	/* read content of zipfile*/
	zf, err := zip.OpenReader(path)
	Check(err, t)
	defer zf.Close()

	list := []string{}
	for _, file := range zf.File {
		name := ConvertBackslashPath(file.Name)
		list = append(list, name)
	}
	AssertContainsNot(list, "file0.txt", t)      // file must not be in zip
	AssertContains(list, "file1.c", t)           // file must exist
	AssertContainsNot(list, "sub2/file2.jpg", t) // file must not be in zip
	AssertContains(list, "sub2/file3.go", t)     // file must exist
	AssertSize(list, 2, t)                       // we expect 2 files in the list
}

func TestZipFileNonExistingFolderIsRejected(t *testing.T) {
	/* prepare */
	dir, err := ioutil.TempDir("", "sechub-cli-temp")
	if err != nil {
		log.Fatal(err)
	}
	defer os.RemoveAll(dir)

	dirname1 := dir + "/nonexistant"

	path := dir + "/testoutput.zip"

	/* execute */
	err = ZipFolders(path, &ZipConfig{Folders: []string{dirname1}})
	expectedErrMsg := "Folder not found:"

	/* test */
	if err == nil {
		t.Fatalf("No error returned!")
	}

	if !strings.HasPrefix(err.Error(), expectedErrMsg) {
		t.Fatalf("Error actual = \"%v\", and expected beginning with = \"%v\"...", err.Error(), expectedErrMsg)
	}

}
