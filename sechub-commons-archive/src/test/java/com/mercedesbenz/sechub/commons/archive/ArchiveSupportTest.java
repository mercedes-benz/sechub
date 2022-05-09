package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.UnzipResult;
import com.mercedesbenz.sechub.test.TestFileSupport;
import com.mercedesbenz.sechub.test.TestUtil;

class ArchiveSupportTest {

    private static File testTar1File;
    private static File expectedOutputOfTestTar1Folder;
    private static File testTar2File;
    private static File expectedOutputOfTestTar2NoFilterFolder;
    private static File expectedOutputOfTestTar2WithFilterReferenceName1AndNoRootAllowedFolder;
    private ArchiveSupport supportToTest;

    @BeforeAll
    static void beforeAll() throws IOException {
        testTar1File = new File("./src/test/resources/tar/test-tar1/test-tar1.tar");
        testTar2File = new File("./src/test/resources/tar/test-tar2/test-tar2.tar");

        expectedOutputOfTestTar1Folder = new File("./src/test/resources/tar/test-tar1/expected-extracted");
        expectedOutputOfTestTar2NoFilterFolder = new File("./src/test/resources/tar/test-tar2/expected-extracted-without-filter");
        expectedOutputOfTestTar2WithFilterReferenceName1AndNoRootAllowedFolder = new File(
                "./src/test/resources/tar/test-tar2/expected-extracted-filter-reference-name-1-no-root-allowed");

        assertTrue(testTar1File.exists(), "Testcase corrupt, tar file not found at:" + testTar1File.getAbsolutePath());
        assertTrue(testTar2File.exists(), "Testcase corrupt, tar file not found at:" + testTar2File.getAbsolutePath());

        assertTrue(expectedOutputOfTestTar1Folder.exists(),
                "Testcase corrupt, expected output folder is not found at:" + expectedOutputOfTestTar1Folder.getAbsolutePath());
    }

    @BeforeEach
    void beforeEach() {
        supportToTest = new ArchiveSupport();
    }

    @Test
    void bugfix_773_zipfile_without_explicit_directory_entries_can_be_extracted_as_well() throws Exception {
        /* prepare */
        File singleZipfile = resolveTestFile("zipfiles/bugfix-773.zip");
        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_773-bugfix_test").toFile();
        targetFolder.mkdirs();

        /* execute */
        UnzipResult result = supportToTest.unzipArchive(singleZipfile, targetFolder);

        /* test */
        File docs = assertFolderExists(targetFolder, "docs");

        File latest = assertFolderExists(docs, "latest");
        assertContainsFiles(latest, "sechub-architecture.html");

        assertEquals(1, result.getExtractedFilesCount());
        assertEquals(2, result.getCreatedFoldersCount());
    }

    @Test
    void single_file_zip_can_be_extracted() throws Exception {
        /* prepare */
        File singleZipfile = resolveTestFile("zipfiles/single_file.zip");
        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_sinlgezip_test").toFile();
        targetFolder.mkdirs();

        /* execute */
        UnzipResult result = supportToTest.unzipArchive(singleZipfile, targetFolder);

        /* test */
        assertContainsFiles(targetFolder, "hardcoded_password.go");
        assertEquals(1, result.getExtractedFilesCount());
        assertEquals(0, result.getCreatedFoldersCount());
    }

    @Test
    void two_files_zip_can_be_extracted() throws Exception {
        /* prepare */
        File twoFilesZipfile = resolveTestFile("zipfiles/two_files.zip");
        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_twofileszip_test").toFile();
        targetFolder.mkdirs();

        /* execute */
        UnzipResult result = supportToTest.unzipArchive(twoFilesZipfile, targetFolder);

        /* test */
        assertContainsFiles(targetFolder, "hardcoded_password.go", "README.md");
        assertEquals(2, result.getExtractedFilesCount());
        assertEquals(0, result.getCreatedFoldersCount());
    }

    @Test
    void hierarchical_files_zip__contained_folders_and_files_can_be_extracted() throws Exception {
        /* prepare */
        File abcZipfile = resolveTestFile("zipfiles/hierarchical_files.zip");

        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_abczip_test").toFile();
        targetFolder.mkdirs();

        /* execute */
        UnzipResult result = supportToTest.unzipArchive(abcZipfile, targetFolder);

        /* test */
        assertContainsFiles(targetFolder, "abc");

        File abcFolder = assertFolderExists(targetFolder, "abc");
        assertContainsFiles(abcFolder, "def", "hardcoded_password.go", "sql_injection.go");

        File defFolder = assertFolderExists(abcFolder, "def");
        assertContainsFiles(defFolder, "ghi", "README-def.md");

        File ghiFolder = assertFolderExists(defFolder, "ghi");
        assertContainsFiles(ghiFolder, "README-ghi.md");

        assertEquals(4, result.getExtractedFilesCount());
        assertEquals(3, result.getCreatedFoldersCount());
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ............... Helpers .......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    private File assertFolderExists(File folder, String subFolder) {
        File[] children = folder.listFiles();
        for (File child : children) {
            if (child.getName().equals(subFolder)) {
                if (!child.isDirectory()) {
                    fail("Subfolder:" + subFolder + " found but is not a directory: " + child);
                }
                return child;
            }
        }
        fail("Subfolder:" + subFolder + " not found inside directory: " + folder);
        throw new IllegalStateException("Should not be called");

    }

    private void assertContainsFiles(File folder, String... childNames) {
        File[] children = folder.listFiles();
        assertEquals(childNames.length, children.length);

        /* build temporary list with names */
        List<String> foundChildNames = new ArrayList<>();

        for (File child : children) {
            foundChildNames.add(child.getName());
        }

        /* test names contained */
        for (String childName : childNames) {
            if (!foundChildNames.contains(childName)) {
                fail("Child: " + childName + " not found found inside list, but: " + foundChildNames);
            }
        }

    }

    private File resolveTestFile(String relativePath) {
        File file = new File("./src/test/resources/" + relativePath);
        assertTrue(file.exists(), "File must exist:" + file);
        return file;
    }

    @Test
    void test_tar1_can_be_extracted_without_filter_and_output_contains_expected_files() throws Exception {
        /* prepare */
        File tarFile = testTar1File;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test1").toFile();
        InputStream is = new FileInputStream(tarFile);

        /* execute */
        supportToTest.extractTar(is, tarFile.getAbsolutePath(), outputDirectory);

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(outputDirectory, TestFileSupport.loadFilesAsFileList(expectedOutputOfTestTar1Folder),
                expectedOutputOfTestTar1Folder);

    }

    @Test
    void test_tar2_can_be_extracted_without_filter_and_output_contains_expected_files() throws Exception {
        /* prepare */
        File tarFile = testTar2File;
        File expectedFilesFolder = expectedOutputOfTestTar2NoFilterFolder;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test2").toFile();
        List<File> expectedFiles = TestFileSupport.loadFilesAsFileList(expectedFilesFolder);
        InputStream is = new FileInputStream(tarFile);

        /* execute */
        supportToTest.extractTar(is, tarFile.getAbsolutePath(), outputDirectory);

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(outputDirectory, expectedFiles, expectedFilesFolder);

    }

    @Test
    void test_tar2_can_be_extracted_with_referencenamefilter_for_ref_name_1_only_and_output_contains_expected_files() throws Exception {
        /* prepare */
        File tarFile = testTar2File;
        File expectedFilesFolder = expectedOutputOfTestTar2WithFilterReferenceName1AndNoRootAllowedFolder;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test2").toFile();
        List<File> expectedFiles = TestFileSupport.loadFilesAsFileList(expectedFilesFolder);
        InputStream is = new FileInputStream(tarFile);

        ReferenceNameAndRootFolderArchiveFilterData data = new ReferenceNameAndRootFolderArchiveFilterData();
        data.rootFolderAccepted = false;
        data.acceptedReferenceNames.add("reference-name-1");

        /* execute */
        supportToTest.extractTar(is, tarFile.getAbsolutePath(), outputDirectory, new ReferenceNameAndRootFolderArchivePathInspector(data));

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(outputDirectory, expectedFiles, expectedFilesFolder);

    }

    @Test
    void when_test_tar1_is_extracted_by_filter_accepting_not_subfolder_2_only_2_files_are_found() throws Exception {
        /* prepare */
        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test").toFile();
        InputStream is = new FileInputStream(testTar1File);

        ArchivePathInspector inspector = new ArchivePathInspector() {

            @Override
            public ArchivePathInspectionResult inspect(String path) {
                ArchivePathInspectionResult result = new ArchivePathInspectionResult();
                if (!path.contains("subfolder2/")) {
                    result.accepted = true;
                }
                return result;
            }
        };
        /* execute */
        supportToTest.extractTar(is, testTar1File.getAbsolutePath(), outputDirectory, inspector);

        /* test */
        List<File> found = TestFileSupport.loadFilesAsFileList(outputDirectory);
        assertEquals(2, found.size());

    }

    private void expectedExtractedFilesAreAllFoundInOutputDirectory(File outputDirectory, List<File> allExpectedFiles, File expectedOutputBaseFolder)
            throws IOException {
        List<File> allExtractedFiles = TestFileSupport.loadFilesAsFileList(outputDirectory);
        List<String> relativePathExpected = reduceToRelativePath(allExpectedFiles, expectedOutputBaseFolder);
        List<String> relativePathExtracted = reduceToRelativePath(allExtractedFiles, outputDirectory);

        for (String expected : relativePathExpected) {
            if (!relativePathExtracted.contains(expected)) {
                fail("did not found " + expected + " inside: " + relativePathExtracted + "\nOutput was at:" + outputDirectory.getAbsolutePath());
            }
        }
        assertEquals(allExtractedFiles.size(), allExpectedFiles.size(), "Amount of files did differ!");
    }

    private List<String> reduceToRelativePath(List<File> files, File parentFolder) {
        List<String> list = new ArrayList<>();
        String parentAbsolutePath = parentFolder.getAbsolutePath();

        for (File file : files) {
            String absolutePath = file.getAbsolutePath();
            if (absolutePath.startsWith(parentAbsolutePath)) {
                list.add(absolutePath.substring(parentAbsolutePath.length()));
            } else {
                throw new IllegalStateException("Testcase corrupt: " + absolutePath + " is not inside " + parentAbsolutePath);
            }
        }
        return list;
    }

}
