package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

class PrepareWrapperArchiveCreatorTest {

    PrepareWrapperArchiveCreator creatorToTest;

    ArchiveSupport archiveSupport;

    FileSupport fileSupport;

    PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport;
    TestFileWriter writer;

    @BeforeEach
    void beforeEach() {
        creatorToTest = new PrepareWrapperArchiveCreator();
        archiveSupport = new ArchiveSupport();
        fileSupport = mock(FileSupport.class);
        sechubConfigurationSupport = new PrepareWrapperSechubConfigurationSupport();
        writer = new TestFileWriter();

        sechubConfigurationSupport.fileSupport = fileSupport;
        creatorToTest.archiveSupport = archiveSupport;
        creatorToTest.sechubConfigurationSupport = sechubConfigurationSupport;
    }

    @Test
    void createArchive_creates_archive_for_binary() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-sechub_archive-creator").toFile();
        tempDir.deleteOnExit();

        String path = tempDir.getAbsolutePath();
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_binary_code_scan_example.json"));
        String testTarFilename = "/test-tar-filename.tar";
        writer.save(new File(path + testTarFilename), "testText", true);

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(path);
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);
        when(fileSupport.getTarFileFromDirectory(anyString())).thenReturn(testTarFilename);

        /* execute */
        assertDoesNotThrow(() -> creatorToTest.create(context));

        /* test */
        assertTrue(new File(path + "/binaries.tar").exists());
    }

    @Test
    void createArchive_creates_archive_for_source() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("sechub_config_support").toFile();
        tempDir.deleteOnExit();

        String path = tempDir.getAbsolutePath();
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_source_code_scan_example.json"));
        String testFile = "/some-test-file.java";
        String repoPath = "/repo";
        writer.save(new File(path + repoPath + testFile), "testText", true);

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(path);
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);
        when(fileSupport.getSubfolderFromDirectory(anyString())).thenReturn(repoPath);

        /* execute */
        assertDoesNotThrow(() -> creatorToTest.create(context));

        /* test */
        assertTrue(new File(path + "/sourcecode.zip").exists());
    }

}