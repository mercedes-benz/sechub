// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchivesCreationResult;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.test.TestFileSupport;
import com.mercedesbenz.sechub.test.TestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType.TAR;
import static com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType.ZIP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ArchiveSupportTest {

    private static File testTar1File;
    private static File expectedTar1Folder;
    private static File testTar2File;
    private static File expectedTar2WithFilterReferenceName1AndNoRootAllowedFolder;
    private static File expectedTar2WithFilterReferenceName1AndReferenceName2Folder;
    private static File expectedOutputOfTestTar2WithRootOnly;
    private static File expectedCreateArchivesTest1DecompressWithoutFileStructureZip;
    private static File expectedCreateArchivesTest1DecompressWithoutFileStructureTar;
    private static File expectedCreateArchivesTest1DecompressWithFileStructureZip;
    private static File expectedCreateArchivesTest1DecompressWithFileStructureTar;
    private static final FileSize maxFileSizeUncompressed = new FileSize("100MB");
    private static final long maxEntries = 100L;
    private static final long maxDirectoryDepth = 10L;
    private static final Duration timeout = Duration.ofSeconds(10);
    private static final ArchiveExtractionConstraints archiveExtractionConstraints = new ArchiveExtractionConstraints(maxFileSizeUncompressed, maxEntries,
            maxDirectoryDepth, timeout);

    private ArchiveSupport supportToTest;

    @BeforeAll
    static void beforeAll() throws IOException {
        testTar1File = ensure("./src/test/resources/tar/test-tar1/test-tar1.tar");
        testTar2File = ensure("./src/test/resources/tar/test-tar2/test-tar2.tar");

        expectedTar1Folder = ensure("./src/test/resources/tar/test-tar1/expected-extracted");
        expectedTar2WithFilterReferenceName1AndNoRootAllowedFolder = ensureTar2("expected-extracted-reference-name-1-no-root-allowed");
        expectedTar2WithFilterReferenceName1AndReferenceName2Folder = ensureTar2("expected-extracted-reference-name-1-and-2-no-root-allowed");
        expectedOutputOfTestTar2WithRootOnly = ensureTar2("expected-extracted-with-root-allowed-only");

        expectedCreateArchivesTest1DecompressWithoutFileStructureZip = ensureCreateArchivesTest1("expected-decompress-without-filestructure-provider/zip");
        expectedCreateArchivesTest1DecompressWithoutFileStructureTar = ensureCreateArchivesTest1("expected-decompress-without-filestructure-provider/tar");

        expectedCreateArchivesTest1DecompressWithFileStructureZip = ensureCreateArchivesTest1("expected-decompress-with-filestructure-provider/zip");
        expectedCreateArchivesTest1DecompressWithFileStructureTar = ensureCreateArchivesTest1("expected-decompress-with-filestructure-provider/tar");
    }

    private static File ensureTar2(String path) {
        return ensure("./src/test/resources/tar/test-tar2/" + path);
    }

    private static File ensureCreateArchivesTest1(String path) {
        return ensure("./src/test/resources/create-archives/test1/" + path);
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

    @ParameterizedTest
    @ValueSource(strings = { "sechub-configuration.json", "sechub-configuration_relative_paths.json" })
    void create_archives_for_binaries_contains_all_content_as_expected_without_filestructure_provider(String configFileName) throws Exception {
        /* prepare */
        File workingDirectory = new File("./src/test/resources/create-archives/test1/working-directory");
        File configFile = new File("./src/test/resources/create-archives/test1/" + configFileName);
        String json = TestFileSupport.loadTextFile(configFile);
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);
        Path tempDir = TestUtil.createTempDirectoryInBuildFolder("create-archives");

        /* execute */
        ArchivesCreationResult result = supportToTest.createArchives(model, workingDirectory.toPath(), tempDir);

        /* test */
        assertNotNull(result.getBinaryArchiveFile());
        assertNotNull(result.getSourceArchiveFile());

        assertTrue(result.isBinaryArchiveCreated());
        assertTrue(result.isSourceArchiveCreated());

        // check ZIP content
        Path reverseFolder = TestUtil.createTempDirectoryInBuildFolder("decompressed-reverse");
        Path reverseFolderZip = reverseFolder.resolve("zip");
        supportToTest.extract(ZIP, new FileInputStream(result.getSourceArchiveFile().toFile()), result.getSourceArchiveFile().toFile().getAbsolutePath(),
                reverseFolderZip.toFile(), null, archiveExtractionConstraints);

        expectedExtractedFilesAreAllFoundInOutputDirectory(reverseFolderZip.toFile(),
                TestFileSupport.loadFilesAsFileList(expectedCreateArchivesTest1DecompressWithoutFileStructureZip),
                expectedCreateArchivesTest1DecompressWithoutFileStructureZip);

        // check TAR content
        Path reverseFolderTar = reverseFolder.resolve("tar");
        supportToTest.extract(TAR, new FileInputStream(result.getBinaryArchiveFile().toFile()), result.getBinaryArchiveFile().toFile().getAbsolutePath(),
                reverseFolderTar.toFile(), null, archiveExtractionConstraints);

        expectedExtractedFilesAreAllFoundInOutputDirectory(reverseFolderTar.toFile(),
                TestFileSupport.loadFilesAsFileList(expectedCreateArchivesTest1DecompressWithoutFileStructureTar),
                expectedCreateArchivesTest1DecompressWithoutFileStructureTar);

    }

    @Test
    void create_archives_for_sources_reduces_absolute_path_origins_to_relative_in_zip() throws Exception {
        /* prepare */
        File workingDirectory = new File("./src/test/resources/create-archives/test1/working-directory");
        File configFile = new File("./src/test/resources/create-archives/test1/sechub-configuration.json");
        String json = TestFileSupport.loadTextFile(configFile);
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);
        Path tempDir = TestUtil.createTempDirectoryInBuildFolder("create-archives");

        /* execute */
        ArchivesCreationResult result = supportToTest.createArchives(model, workingDirectory.toPath(), tempDir);

        /* test */
        assertNotNull(result.getBinaryArchiveFile());
        assertNotNull(result.getSourceArchiveFile());

        assertTrue(result.isBinaryArchiveCreated());
        assertTrue(result.isSourceArchiveCreated());

        // check ZIP content
        Path reverseFolder = TestUtil.createTempDirectoryInBuildFolder("decompressed-reverse-with-fs");
        Path reverseFolderZip = reverseFolder.resolve("zip");
        SecHubFileStructureDataProvider codeScanProvider = SecHubFileStructureDataProvider.builder().setModel(model).setScanType(ScanType.CODE_SCAN).build();

        supportToTest.extract(ZIP, new FileInputStream(result.getSourceArchiveFile().toFile()), result.getSourceArchiveFile().toFile().getAbsolutePath(),
                reverseFolderZip.toFile(), codeScanProvider, archiveExtractionConstraints);

        expectedExtractedFilesAreAllFoundInOutputDirectory(reverseFolderZip.toFile(),
                TestFileSupport.loadFilesAsFileList(expectedCreateArchivesTest1DecompressWithFileStructureZip),
                expectedCreateArchivesTest1DecompressWithFileStructureZip);

        // check TAR content
        SecHubFileStructureDataProvider licenseScanProvider = SecHubFileStructureDataProvider.builder().setModel(model).setScanType(ScanType.LICENSE_SCAN)
                .build();

        Path reverseFolderTar = reverseFolder.resolve("tar");
        supportToTest.extract(TAR, new FileInputStream(result.getBinaryArchiveFile().toFile()), result.getBinaryArchiveFile().toFile().getAbsolutePath(),
                reverseFolderTar.toFile(), licenseScanProvider, archiveExtractionConstraints);

        expectedExtractedFilesAreAllFoundInOutputDirectory(reverseFolderTar.toFile(),
                TestFileSupport.loadFilesAsFileList(expectedCreateArchivesTest1DecompressWithFileStructureTar),
                expectedCreateArchivesTest1DecompressWithFileStructureTar);

    }

    @Test
    void create_archives_for_binaries_contains_all_content_as_expected_with_filestructure_provider() throws Exception {
        /* prepare */
        File workingDirectory = new File("./src/test/resources/create-archives/test1/working-directory");
        File configFile = new File("./src/test/resources/create-archives/test1/sechub-configuration.json");
        String json = TestFileSupport.loadTextFile(configFile);
        SecHubConfigurationModel model = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);
        Path tempDir = TestUtil.createTempDirectoryInBuildFolder("create-archives");

        /* execute */
        ArchivesCreationResult result = supportToTest.createArchives(model, workingDirectory.toPath(), tempDir);

        /* test */
        assertNotNull(result.getBinaryArchiveFile());
        assertNotNull(result.getSourceArchiveFile());

        assertTrue(result.isBinaryArchiveCreated());
        assertTrue(result.isSourceArchiveCreated());

        // check ZIP content
        Path reverseFolder = TestUtil.createTempDirectoryInBuildFolder("decompressed-reverse-with-fs");
        Path reverseFolderZip = reverseFolder.resolve("zip");
        SecHubFileStructureDataProvider codeScanProvider = SecHubFileStructureDataProvider.builder().setModel(model).setScanType(ScanType.CODE_SCAN).build();

        supportToTest.extract(ZIP, new FileInputStream(result.getSourceArchiveFile().toFile()), result.getSourceArchiveFile().toFile().getAbsolutePath(),
                reverseFolderZip.toFile(), codeScanProvider, archiveExtractionConstraints);

        expectedExtractedFilesAreAllFoundInOutputDirectory(reverseFolderZip.toFile(),
                TestFileSupport.loadFilesAsFileList(expectedCreateArchivesTest1DecompressWithFileStructureZip),
                expectedCreateArchivesTest1DecompressWithFileStructureZip);

        // check TAR content
        SecHubFileStructureDataProvider licenseScanProvider = SecHubFileStructureDataProvider.builder().setModel(model).setScanType(ScanType.LICENSE_SCAN)
                .build();

        Path reverseFolderTar = reverseFolder.resolve("tar");
        supportToTest.extract(TAR, new FileInputStream(result.getBinaryArchiveFile().toFile()), result.getBinaryArchiveFile().toFile().getAbsolutePath(),
                reverseFolderTar.toFile(), licenseScanProvider, archiveExtractionConstraints);

        expectedExtractedFilesAreAllFoundInOutputDirectory(reverseFolderTar.toFile(),
                TestFileSupport.loadFilesAsFileList(expectedCreateArchivesTest1DecompressWithFileStructureTar),
                expectedCreateArchivesTest1DecompressWithFileStructureTar);

    }

    @Test
    void compress_zip_contains_all_files_from_folder() throws Exception {
        /* prepare */

        File targetFile = TestUtil.createTempFileInBuildFolder("output", "zip").toFile();
        File folder = new File("./src/test/resources/tar/test-tar1/expected-extracted");

        /* execute */
        supportToTest.compressFolder(ArchiveType.ZIP, folder, targetFile);

        /* test */
        assertTrue(targetFile.exists());

        // extract the created ZIP file again to reverse folder
        Path reverseFolder = TestUtil.createTempDirectoryInBuildFolder("compressed-reverse");
        supportToTest.extract(ZIP, new FileInputStream(targetFile), targetFile.getAbsolutePath(), reverseFolder.toFile(), null, archiveExtractionConstraints);

        // check extracted same as before
        expectedExtractedFilesAreAllFoundInOutputDirectory(reverseFolder.toFile(), TestFileSupport.loadFilesAsFileList(expectedTar1Folder), expectedTar1Folder);

    }

    @Test
    void compress_zip_files_from_folder_have_no_absolute_paths() throws Exception {
        /* prepare */

        File targetFile = TestUtil.createTempFileInBuildFolder("output", "zip").toFile();
        File folder = new File("./src/test/resources/tar/test-tar1/expected-extracted");

        /* execute */
        supportToTest.compressFolder(ArchiveType.ZIP, folder, targetFile);

        /* test */
        assertTrue(targetFile.exists());

        ZipEntry entry = null;
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(targetFile))) {
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().startsWith("/")) {
                    fail("Expected relative path only - but found absolute path: " + entry.getName() + " inside " + targetFile.getAbsolutePath());
                }
            }
        }

    }

    @Test
    void compress_zip_single_data_txt_file_compressed_output_contains_same_content() throws Exception {
        /* prepare */

        File parentFolder = TestUtil.createTempDirectoryInBuildFolder("archive-compress-zip-test").toFile();

        File dataFolder = new File(parentFolder, "data-folder1");
        File textFile = new File(dataFolder, "data.txt");

        File targetFile = new File(parentFolder, "sourcecode.zip");

        TextFileWriter writer = new TextFileWriter();
        writer.save(textFile, "This is just a test content", true);

        /* execute */
        supportToTest.compressFolder(ArchiveType.ZIP, dataFolder, targetFile);

        /* test */
        assertTrue(targetFile.exists());

        // extract the created ZIP file again to reverse folder
        Path reverseFolder = TestUtil.createTempDirectoryInBuildFolder("compressed-reverse");
        File outputFolder = reverseFolder.toFile();
        supportToTest.extract(ZIP, new FileInputStream(targetFile), targetFile.getAbsolutePath(), outputFolder, null, archiveExtractionConstraints);

        // check extracted same as before
        assertFileContains(new File(outputFolder, "data.txt"), "This is just a test content");

    }

    private void assertFileContains(File file, String expectedContent) throws IOException {
        if (!file.exists()) {
            fail("File:" + file + " does not exist!");
        }
        TextFileReader reader = new TextFileReader();
        String content = reader.loadTextFile(file);
        assertEquals(expectedContent, content);
    }

    @Test
    void compress_zip_two_txt_files_compressed_output_contains_same_content() throws Exception {
        /* prepare */

        File parentFolder = TestUtil.createTempDirectoryInBuildFolder("archive-compress-zip-test").toFile();

        File dataFolder = new File(parentFolder, "data-folder2");
        File textFile1 = new File(dataFolder, "test1.txt");
        File textFile2 = new File(dataFolder, "test2.txt");

        File targetFile = new File(parentFolder, "sourcecode.zip");

        TextFileWriter writer = new TextFileWriter();
        writer.save(textFile1, "text1", true);
        writer.save(textFile2, "text2", true);

        /* execute */
        supportToTest.compressFolder(ArchiveType.ZIP, dataFolder, targetFile);

        /* test */
        assertTrue(targetFile.exists());

        // extract the created ZIP file again to reverse folder
        Path reverseFolder = TestUtil.createTempDirectoryInBuildFolder("compressed-reverse");
        File outputFolder = reverseFolder.toFile();
        supportToTest.extract(ZIP, new FileInputStream(targetFile), targetFile.getAbsolutePath(), outputFolder, null, archiveExtractionConstraints);

        // check extracted same as before
        assertFileContains(new File(outputFolder, "test1.txt"), "text1");
        assertFileContains(new File(outputFolder, "test2.txt"), "text2");

    }

    @Test
    void bugfix_773_zipfile_without_explicit_directory_entries_can_be_extracted_as_well_when_root_folder_is_accepted() throws Exception {
        /* prepare */
        File singleZipfile = resolveTestFile("zipfiles/bugfix-773.zip");
        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_773-bugfix_test").toFile();
        targetFolder.mkdirs();

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveExtractionResult result = supportToTest.extract(ZIP, new FileInputStream(singleZipfile), singleZipfile.getAbsolutePath(), targetFolder,
                configuration, archiveExtractionConstraints);

        /* test */
        File docs = assertFolderExists(targetFolder, "docs");

        File latest = assertFolderExists(docs, "latest");
        assertContainsFiles(latest, "sechub-architecture.html");

        assertEquals(1, result.getExtractedFilesCount());
        assertEquals(2, result.getCreatedFoldersCount());
    }

    /**
     * This test is to ensure that the {@link SafeArchiveInputStream} is used when
     * calling the extract method. This is done by setting the timeout to 1ms to
     * enforce a timeout exception. The exception is of type
     * {@link ArchiveExtractionException} which is used by the
     * {@link SafeArchiveInputStream} class.
     */
    @Test
    void extract_uses_safe_archive_input_stream_when_extracting_archive() throws Exception {
        /* prepare */
        File twoFilesZipfile = resolveTestFile("zipfiles/two_files.zip");
        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_twofileszip_test").toFile();
        targetFolder.mkdirs();

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(true);

        // set timeout to 1ns to enforce a timeout exception
        Duration timeout = Duration.ofNanos(1);
        ArchiveExtractionConstraints archiveExtractionConstraints = new ArchiveExtractionConstraints(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth,
                timeout);

        /* execute & test */

        /* @formatter:off */
        ArchiveExtractionException exception = assertThrows(
                ArchiveExtractionException.class,
                () -> supportToTest.extract(
                        ZIP,
                        new FileInputStream(twoFilesZipfile),
                        twoFilesZipfile.getAbsolutePath(),
                        targetFolder,
                        configuration,
                        archiveExtractionConstraints
                )
        );
        /* @formatter:on */

        assertThat(exception.getMessage(), is("Timeout exceeded"));

    }

    @Test
    void single_file_zip_can_be_extracted_when_root_folder_is_accepted() throws Exception {
        /* prepare */
        File singleZipfile = resolveTestFile("zipfiles/single_file.zip");
        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("pds_sinlgezip_test").toFile();
        targetFolder.mkdirs();

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveExtractionResult result = supportToTest.extract(ZIP, new FileInputStream(singleZipfile), singleZipfile.getAbsolutePath(), targetFolder,
                configuration, archiveExtractionConstraints);

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

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveExtractionResult result = supportToTest.extract(ZIP, new FileInputStream(twoFilesZipfile), twoFilesZipfile.getAbsolutePath(), targetFolder,
                configuration, archiveExtractionConstraints);

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

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(true);

        /* execute */
        ArchiveExtractionResult result = supportToTest.extract(ZIP, new FileInputStream(abcZipfile), abcZipfile.getAbsolutePath(), targetFolder, configuration,
                archiveExtractionConstraints);

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

        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("tar-test1").toFile();

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(true);

        /* execute */
        supportToTest.extract(TAR, new FileInputStream(tarFile), tarFile.getAbsolutePath(), targetFolder, configuration, archiveExtractionConstraints);

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(targetFolder, TestFileSupport.loadFilesAsFileList(expectedTar1Folder), expectedTar1Folder);

    }

    @Test
    void test_tar1_can_be_extracted_and_output_contains_no_files_when_root_folder_not_accepted() throws Exception {
        /* prepare */
        File tarFile = testTar1File;

        File targetFolder = TestUtil.createTempDirectoryInBuildFolder("tar-test1").toFile();

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(false);

        /* execute */
        supportToTest.extract(TAR, new FileInputStream(tarFile), tarFile.getAbsolutePath(), targetFolder, configuration, archiveExtractionConstraints);

        /* test */
        assertEquals(0, targetFolder.listFiles().length);

    }

    @Test
    void test_tar2_can_be_extracted_with_configuration_only_root_folder_accepted_but_contains_only_one_file_from_root() throws Exception {
        /* prepare */
        File tarFile = testTar2File;
        File expectedFilesFolder = expectedOutputOfTestTar2WithRootOnly;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test2").toFile();
        List<File> expectedFiles = TestFileSupport.loadFilesAsFileList(expectedFilesFolder);

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(true);

        /* execute */
        supportToTest.extract(TAR, new FileInputStream(tarFile), tarFile.getAbsolutePath(), outputDirectory, configuration, archiveExtractionConstraints);

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(outputDirectory, expectedFiles, expectedFilesFolder);

    }

    @Test
    void test_tar2_can_be_extracted_with_configuration_no_root_folder_accepted_will_be_empty() throws Exception {
        /* prepare */
        File tarFile = testTar2File;

        File outputDirectory = TestUtil.createTempDirectoryInBuildFolder("tar-test2").toFile();

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(false);

        /* execute */
        supportToTest.extract(TAR, new FileInputStream(tarFile), tarFile.getAbsolutePath(), outputDirectory, configuration, archiveExtractionConstraints);

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

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(false);
        configuration.addAcceptedReferenceNames(List.of("reference-name-1"));

        /* execute */
        supportToTest.extract(TAR, new FileInputStream(tarFile), tarFile.getAbsolutePath(), outputDirectory, configuration, archiveExtractionConstraints);

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

        MutableSecHubFileStructureDataProvider configuration = new MutableSecHubFileStructureDataProvider();
        configuration.setRootFolderAccepted(false);
        configuration.addAcceptedReferenceNames(Arrays.asList("reference-name-1", "reference-name-2"));

        /* execute */
        supportToTest.extract(TAR, new FileInputStream(tarFile), tarFile.getAbsolutePath(), outputDirectory, configuration, archiveExtractionConstraints);

        /* test */
        expectedExtractedFilesAreAllFoundInOutputDirectory(outputDirectory, expectedFiles, expectedFilesFolder);

    }

    private void expectedExtractedFilesAreAllFoundInOutputDirectory(File outputDirectory, List<File> allExpectedFiles, File expectedOutputBaseFolder)
            throws IOException {
        List<File> allExtractedFiles = TestFileSupport.loadFilesAsFileList(outputDirectory);
        List<String> relativePathExpected = reduceToRelativePath(allExpectedFiles, expectedOutputBaseFolder);
        List<String> relativePathExtracted = reduceToRelativePath(allExtractedFiles, outputDirectory);

        if (allExpectedFiles.size() != allExtractedFiles.size()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Number of files differs: Expected:").append(allExpectedFiles.size() + " but was:" + allExtractedFiles.size());
            sb.append("\n\nExpected files:\n");
            dump(allExpectedFiles, sb);
            sb.append("\n\nExtracted files:\n");
            dump(allExtractedFiles, sb);
            fail(sb.toString());
            assertEquals(allExpectedFiles.size(), allExtractedFiles.size(), "Number of files differed!");
        }
        for (String expected : relativePathExpected) {
            if (!relativePathExtracted.contains(expected)) {
                StringBuilder sb = new StringBuilder();
                sb.append("did not find'").append(expected).append("' inside:");
                dump(relativePathExtracted, sb);
                sb.append("\n\nOutput location:").append(outputDirectory.getAbsolutePath());
                fail(sb.toString());
            }
        }
    }

    private void dump(List<?> list, StringBuilder sb) {
        int i = 0;
        for (Object entry : list) {
            i++;
            sb.append("\n  - (").append(i).append(") ").append(entry);
        }
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
