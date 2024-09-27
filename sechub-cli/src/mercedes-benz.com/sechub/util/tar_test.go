// SPDX-License-Identifier: MIT
package util

import (
	"archive/tar"
	"fmt"
	"os"
	"strings"
	"testing"

	sechubUtil "mercedes-benz.com/sechub/testutil"
)

func TestTarFileTarsItselfIsRejected(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	sechubUtil.CreateTestDirectory(dirname1, 0755, t)

	filename1 := dirname1 + "/file1.bin"

	content := []byte("Binary :-)\n")
	sechubUtil.CreateTestFile(filename1, 0644, content, t)

	// path to tar file is also part of added files - because in dirname1
	tarFile := dirname1 + "/testoutput.tar"
	// create tar file
	newTarFile, _ := os.Create(tarFile)
	// create tar writer
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: tarFile,
		TarWriter:   tarWriter,
		PrefixInTar: "",
		Folders:     []string{dirname1},
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	sechubUtil.AssertErrorHasExpectedMessage(err, TargetTarFileLoop, t)
}

func TestTarFileCanBeCreated(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/sub1"
	dirname2 := dir + "/sub2"
	dirname3 := dir + "/sub2/sub3"

	sechubUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubUtil.CreateTestDirectory(dirname2, 0755, t)
	sechubUtil.CreateTestDirectory(dirname3, 0755, t)

	filename1 := dirname1 + "/file1.bin"
	filename2 := dirname2 + "/file2.bin"
	filename3 := dirname3 + "/file3.bin"

	content := []byte("Binary :-)\n")
	sechubUtil.CreateTestFile(filename1, 0644, content, t)
	sechubUtil.CreateTestFile(filename2, 0644, content, t)
	sechubUtil.CreateTestFile(filename3, 0644, content, t)

	path := dir + "/testoutput.tar"
	newTarFile, _ := os.Create(path)
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: path,
		TarWriter:   tarWriter,
		PrefixInTar: "__data__/test/",
		Files:       []string{filename1},
		Folders:     []string{dirname2},
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	sechubUtil.Check(err, t)
	sechubUtil.AssertFileExists(path, t)

	list, _ := ListContentOfTarFile(path)
	sechubUtil.AssertContains(list, config.PrefixInTar+strings.TrimPrefix(filename1, "/"), t)
	sechubUtil.AssertContains(list, config.PrefixInTar+strings.TrimPrefix(filename2, "/"), t)
	sechubUtil.AssertContains(list, config.PrefixInTar+strings.TrimPrefix(filename3, "/"), t)
	sechubUtil.AssertSize(list, 3, t)
}

func TestTarFileCanBeCreated_with_exclude_patterns_applied(t *testing.T) {
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
	filename1 := dirname1 + "/file1.bin"
	filename2 := dirname2 + "/file2.bin"
	filename3 := dirname3 + "/file3.bin"

	content := []byte("Binary :-)\n")
	sechubUtil.CreateTestFile(filename0, 0644, content, t)
	sechubUtil.CreateTestFile(filename1, 0644, content, t)
	sechubUtil.CreateTestFile(filename2, 0644, content, t)
	sechubUtil.CreateTestFile(filename3, 0644, content, t)

	path := dir + "/testoutput.tar"
	newTarFile, _ := os.Create(path)
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: path,
		TarWriter:   tarWriter,
		PrefixInTar: "",
		Folders:     []string{dirname1, dirname2},
		Excludes:    []string{"**/sub3/**", "*.txt"},
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	list, _ := ListContentOfTarFile(path)
	sechubUtil.AssertContainsNot(list, strings.TrimPrefix(filename0, "/"), t) // pattern excluded
	sechubUtil.AssertContains(list, strings.TrimPrefix(filename1, "/"), t)    // this must remain
	sechubUtil.AssertContains(list, strings.TrimPrefix(filename2, "/"), t)    // this must remain
	sechubUtil.AssertContainsNot(list, strings.TrimPrefix(filename3, "/"), t) // folder excluded
	sechubUtil.AssertSize(list, 2, t)
}

func TestTarFileNonExistingFolderIsRejected(t *testing.T) {
	/* prepare */
	dir := sechubUtil.InitializeTestTempDir(t)
	defer os.RemoveAll(dir)

	dirname1 := dir + "/nonexistant"

	path := dir + "/testoutput.tar"
	newTarFile, _ := os.Create(path)
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: path,
		TarWriter:   tarWriter,
		PrefixInTar: "",
		Folders:     []string{dirname1},
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	sechubUtil.AssertErrorHasExpectedStartMessage(err, "Folder not found:", t)
}

func TestTarFileContainsRelativeDefinedFolders(t *testing.T) {
	/* prepare */
	RelativeTmpTestDir := "sechub-cli-tmptest"
	sechubUtil.CreateTestDirectory(RelativeTmpTestDir, 0755, t)
	defer os.RemoveAll(RelativeTmpTestDir)

	dirname1 := RelativeTmpTestDir + "/sub1"
	dirname2 := RelativeTmpTestDir + "/sub2"
	filepath1 := dirname1 + "/file1.bin"
	filepath2 := dirname2 + "/file2.bin"

	// create dirs
	sechubUtil.CreateTestDirectory(dirname1, 0755, t)
	sechubUtil.CreateTestDirectory(dirname2, 0755, t)

	// create files
	content := []byte("Binary :-)\n")
	sechubUtil.CreateTestFile(filepath1, 0644, content, t)
	sechubUtil.CreateTestFile(filepath2, 0644, content, t)

	path := RelativeTmpTestDir + "/testoutput.tar"
	newTarFile, _ := os.Create(path)
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: path,
		TarWriter:   tarWriter,
		PrefixInTar: "",
		Files:       []string{filepath1},
		Folders:     []string{dirname2},
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	list, _ := ListContentOfTarFile(path)
	sechubUtil.AssertContains(list, filepath1, t)
	sechubUtil.AssertContains(list, filepath2, t)
}

func TestTarFileContainsRelativeFoldersOutsideCurrent(t *testing.T) {
	/* prepare */
	RelativeTmpTestDir := "./../sechub-cli-tmptest"
	sechubUtil.CreateTestDirectory(RelativeTmpTestDir, 0755, t)
	defer os.RemoveAll(RelativeTmpTestDir)

	filepath1 := RelativeTmpTestDir + "/file1.bin"

	// create files
	content := []byte("Binary :-)\n")
	sechubUtil.CreateTestFile(filepath1, 0644, content, t)

	path := RelativeTmpTestDir + "/testoutput.tar"
	newTarFile, _ := os.Create(path)
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: path,
		TarWriter:   tarWriter,
		PrefixInTar: "",
		Folders:     []string{RelativeTmpTestDir},
		Excludes:    []string{"**/*.tar"}, // exclude our own tar
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	list, _ := ListContentOfTarFile(path)
	// "./../sechub-cli-tmptest/file1.bin" becomes "sechub-cli-tmptest/file1.bin" in tar file
	sechubUtil.AssertContains(list, "sechub-cli-tmptest/file1.bin", t)
}

func Example_tarDetectsNonExistingFiles() {
	/* prepare */
	var t testing.T
	RelativeTmpTestDir := "sechub-cli-tmptest"
	sechubUtil.CreateTestDirectory(RelativeTmpTestDir, 0755, &t)
	defer os.RemoveAll(RelativeTmpTestDir)

	path := RelativeTmpTestDir + "/testoutput.tar"
	newTarFile, _ := os.Create(path)
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: path,
		TarWriter:   tarWriter,
		PrefixInTar: "",
		Files:       []string{RelativeTmpTestDir + "/non-existing-file.bin"},
		Quiet:       true,
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	fmt.Println(err)
	// Output:
	// Folder created: "sechub-cli-tmptest"
	// open sechub-cli-tmptest/non-existing-file.bin: no such file or directory
}

func TestTarIsSkippingSymlinks(t *testing.T) {
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

	path := RelativeTmpTestDir + "/testoutput.tar"
	newTarFile, _ := os.Create(path)
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: path,
		TarWriter:   tarWriter,
		PrefixInTar: "",
		Folders:     []string{RelativeTmpTestDir},
		Excludes:    []string{"**/*.tar"}, // exclude our own tar
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	sechubUtil.Check(err, t)

	list, _ := ListContentOfTarFile(path)
	sechubUtil.AssertContains(list, filepath1, t)
	// None of the symlinks must be in the tar file
	sechubUtil.AssertContainsNot(list, filepath2, t)
	sechubUtil.AssertContainsNot(list, filepath3, t)
}

func TestTarFileCanBeCreated_with_absolute_path_and_exclude_patterns_applied(t *testing.T) {
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

	path := dir + "/testoutput.tar"
	newTarFile, _ := os.Create(path)
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: path,
		TarWriter:   tarWriter,
		PrefixInTar: "test123/",
		Folders:     []string{dir},
		Excludes:    []string{dirname2 + "/**", "*.tar"},
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	sechubUtil.Check(err, t)
	sechubUtil.AssertFileExists(path, t)

	list, _ := ListContentOfTarFile(path)
	sechubUtil.AssertContains(list, strings.TrimPrefix("test123"+filename0, "/"), t)    // this must remain
	sechubUtil.AssertContains(list, strings.TrimPrefix("test123"+filename1, "/"), t)    // this must remain
	sechubUtil.AssertContainsNot(list, strings.TrimPrefix("test123"+filename2, "/"), t) // excluded!
	sechubUtil.AssertContainsNot(list, strings.TrimPrefix("test123"+filename3, "/"), t) // excluded!
	sechubUtil.AssertSize(list, 2, t)
}

func TestTarFileCanBeCreated_with_relative_path_and_exclude_patterns_applied(t *testing.T) {
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

	path := RelativeTmpTestDir + "/testoutput.tar"
	newTarFile, _ := os.Create(path)
	tarWriter := tar.NewWriter(newTarFile)

	config := TarConfig{
		TarFileName: path,
		TarWriter:   tarWriter,
		PrefixInTar: "test123/",
		Folders:     []string{RelativeTmpTestDir},
		Excludes:    []string{dirname2 + "/**", "*.tar"},
	}

	/* execute */
	err := Tar(&config)
	tarWriter.Close()
	newTarFile.Close()

	/* test */
	sechubUtil.Check(err, t)
	sechubUtil.AssertFileExists(path, t)

	list, _ := ListContentOfTarFile(path)
	sechubUtil.AssertContains(list, "test123/"+filepath1, t)
	sechubUtil.AssertContainsNot(list, "test123/"+filepath2, t) // excluded!
	sechubUtil.AssertContainsNot(list, "test123/"+filepath3, t) // excluded!
}
