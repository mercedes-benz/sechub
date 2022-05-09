package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.TestFileSupport;
import com.mercedesbenz.sechub.test.TestUtil;

class ArchiveSupportTest {

    private static File testTar1File;
    private static File expectedTar1Folder;
    private static File testTar2File;
    private static File expectedTar2WithFilterReferenceName1AndNoRootAllowedFolder;
    private static File expectedTar2WithFilterReferenceName1AndReferenceName2Folder;
    private static File expectedOutputOfTestTar2WithRootOnly;
    private ArchiveSupport supportToTest;

    @BeforeAll
    static void beforeAll() throws IOException {
        testTar1File = ensure("./src/test/resources/tar/test-tar1/test-tar1.tar");
        testTar2File = ensure("./src/test/resources/tar/test-tar2/test-tar2.tar");

        expectedTar1Folder = ensure("./src/test/resources/tar/test-tar1/expected-extracted");
        expectedTar2WithFilterReferenceName1AndNoRootAllowedFolder = ensureTar2("expected-extracted-reference-name-1-no-root-allowed");
        expectedTar2WithFilterReferenceName1AndReferenceName2Folder = ensureTar2("expected-extracted-reference-name-1-and-2-no-root-allowed");
        expectedOutputOfTestTar2WithRootOnly = ensureTar2("expected-extracted-with-root-allowed-only");

    }

    private static File ensureTar2(String path) {
        return ensure("./src/test/resources/tar/test-tar2/" + path);
    }

    private static File ensure(String path) {
        File file = new File(path);
        assertTrue(file.exists(), "Testcase corrupt, expected output folder is not found at:" + path);
        return file;
    }

    @BeforeEach
    void beforeEach() {
        supportToTest = new ArchiveSupport();
    }

    @Test
    void bugfix_773_zipfile_without_explicit_directory_entries_can_be_extracted_as_well_when_root_folder_is_accepted() throws Exception {
        /* prepare */
        File singleZipfile = resolveTestFile("zipfiles/bugfix-773.zip");
        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_773-bugfix_test").toFile();
        targetFolder.mkdirs();

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveExtractionResult result = supportToTest.extractZip(singleZipfile, targetFolder, configuration);

        /* test */
        File docs = assertFolderExists(targetFolder, "docs");

        File latest = assertFolderExists(docs, "latest");
        assertContainsFiles(latest, "sechub-architecture.html");

        assertEquals(1, result.getExtractedFilesCount());
        assertEquals(2, result.getCreatedFoldersCount());
    }

    @Test
    void single_file_zip_can_be_extracted_when_root_folder_is_accepted() throws Exception {
        /* prepare */
        File singleZipfile = resolveTestFile("zipfiles/single_file.zip");
        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_sinlgezip_test").toFile();
        targetFolder.mkdirs();

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveExtractionResult result = supportToTest.extractZip(singleZipfile, targetFolder, configuration);

        /* test */
        assertContainsFiles(targetFolder, "hardcoded_password.go");
        assertEquals(1, result.getExtractedFilesCount());
        assertEquals(0, result.getCreatedFoldersCount());
    }

    @Test
    void two_files_zip_can_be_extracted_when_root_folder_is_accepted() throws Exception {
        /* prepare */
        File twoFilesZipfile = resolveTestFile("zipfiles/two_files.zip");
        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_twofileszip_test").toFile();
        targetFolder.mkdirs();

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveExtractionResult result = supportToTest.extractZip(twoFilesZipfile, targetFolder, configuration);

        /* test */
        assertContainsFiles(targetFolder, "hardcoded_password.go", "README.md");
        assertEquals(2, result.getExtractedFilesCount());
        assertEquals(0, result.getCreatedFoldersCount());
    }

    @Test
    void hierarchical_files_zip__contained_folders_and_files_can_be_extracted_when_root_folder_is_accepted() throws Exception {
        /* prepare */
        File abcZipfile = resolveTestFile("zipfiles/hierarchical_files.zip");

        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_abczip_test").toFile();
        targetFolder.mkdirs();

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveExtractionResult result = supportToTest.extractZip(abcZipfile, targetFolder, configuration);

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

    @Test
    void test_tar1_can_be_extracted_and_output_contains_all_files_when_root_folder_accepted_because_no_data() throws Exception {
        /* prepare */
        File tarFile = testTar1File;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test1").toFile();
        InputStream is = new FileInputStream(tarFile);

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(true);

        /* execute */
        supportToTest.extractTar(is, tarFile.getAbsolutePath(), outputDirectory, configuration);

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(outputDirectory, TestFileSupport.loadFilesAsFileList(expectedTar1Folder), expectedTar1Folder);

    }

    @Test
    void test_tar1_can_be_extracted_and_output_contains_no_files_when_root_folder_not_accepted() throws Exception {
        /* prepare */
        File tarFile = testTar1File;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test1").toFile();
        InputStream is = new FileInputStream(tarFile);

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(false);

        /* execute */
        supportToTest.extractTar(is, tarFile.getAbsolutePath(), outputDirectory, configuration);

        /* test */
        assertEquals(0, outputDirectory.listFiles().length);

    }

    @Test
    void test_tar2_can_be_extracted_with_configuration_only_root_folder_accepted_but_contains_only_one_file_from_root() throws Exception {
        /* prepare */
        File tarFile = testTar2File;
        File expectedFilesFolder = expectedOutputOfTestTar2WithRootOnly;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test2").toFile();
        List<File> expectedFiles = TestFileSupport.loadFilesAsFileList(expectedFilesFolder);
        InputStream is = new FileInputStream(tarFile);

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(true);

        /* execute */
        supportToTest.extractTar(is, tarFile.getAbsolutePath(), outputDirectory, configuration);

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(outputDirectory, expectedFiles, expectedFilesFolder);

    }

    @Test
    void test_tar2_can_be_extracted_with_configuration_no_root_folder_accepted_will_be_empty() throws Exception {
        /* prepare */
        File tarFile = testTar2File;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test2").toFile();
        InputStream is = new FileInputStream(tarFile);

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(false);

        /* execute */
        supportToTest.extractTar(is, tarFile.getAbsolutePath(), outputDirectory, configuration);

        /* test */
        assertEquals(0, outputDirectory.listFiles().length);

    }

    @Test
    void test_tar2_can_be_extracted_with_referencenamefilter_for_ref_name_1_only_and_output_contains_expected_files() throws Exception {
        /* prepare */
        File tarFile = testTar2File;
        File expectedFilesFolder = expectedTar2WithFilterReferenceName1AndNoRootAllowedFolder;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test2").toFile();
        List<File> expectedFiles = TestFileSupport.loadFilesAsFileList(expectedFilesFolder);
        InputStream is = new FileInputStream(tarFile);

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(false);
        configuration.addAcceptedReferenceNames(Arrays.asList("reference-name-1"));

        /* execute */
        supportToTest.extractTar(is, tarFile.getAbsolutePath(), outputDirectory, configuration);

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(outputDirectory, expectedFiles, expectedFilesFolder);

    }

    @Test
    void test_tar2_can_be_extracted_with_referencenamefilter_for_ref_name_1_and_name_2_and_output_contains_expected_files() throws Exception {
        /* prepare */
        File tarFile = testTar2File;
        File expectedFilesFolder = expectedTar2WithFilterReferenceName1AndReferenceName2Folder;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test2").toFile();
        List<File> expectedFiles = TestFileSupport.loadFilesAsFileList(expectedFilesFolder);
        InputStream is = new FileInputStream(tarFile);

        SecHubFileStructureConfiguration configuration = new SecHubFileStructureConfiguration();
        configuration.setRootFolderAccepted(false);
        configuration.addAcceptedReferenceNames(Arrays.asList("reference-name-1", "reference-name-2"));

        /* execute */
        supportToTest.extractTar(is, tarFile.getAbsolutePath(), outputDirectory, configuration);

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(outputDirectory, expectedFiles, expectedFilesFolder);

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

}
