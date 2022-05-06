package com.mercedesbenz.sechub.pds;

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

import com.mercedesbenz.sechub.test.TestFileSupport;
import com.mercedesbenz.sechub.test.TestUtil;

class PDSTarSupportTest {

    private static File testTar1File;
    private static File expectedOutputOfTestTar1Folder;
    private static File testTar2File;
    private static File expectedOutputOfTestTar2NoFilterFolder;
    private static File expectedOutputOfTestTar2WithFilterReferenceName1AndNoRootAllowedFolder;
    private PDSTarSupport supportToTest;

    @BeforeAll
    static void beforeAll() throws IOException {
        testTar1File = new File("./src/test/resources/archive/test-tar1/test-tar1.tar");
        testTar2File = new File("./src/test/resources/archive/test-tar2/test-tar2.tar");

        expectedOutputOfTestTar1Folder = new File("./src/test/resources/archive/test-tar1/expected-extracted");
        expectedOutputOfTestTar2NoFilterFolder = new File("./src/test/resources/archive/test-tar2/expected-extracted-without-filter");
        expectedOutputOfTestTar2WithFilterReferenceName1AndNoRootAllowedFolder = new File(
                "./src/test/resources/archive/test-tar2/expected-extracted-filter-reference-name-1-no-root-allowed");

        assertTrue(testTar1File.exists(), "Testcase corrupt, tar file not found at:" + testTar1File.getAbsolutePath());
        assertTrue(testTar2File.exists(), "Testcase corrupt, tar file not found at:" + testTar2File.getAbsolutePath());

        assertTrue(expectedOutputOfTestTar1Folder.exists(),
                "Testcase corrupt, expected output folder is not found at:" + expectedOutputOfTestTar1Folder.getAbsolutePath());
    }

    @BeforeEach
    void beforeEach() {
        supportToTest = new PDSTarSupport();
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
