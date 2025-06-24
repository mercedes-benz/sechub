// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractPrepareToolContext;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareToolContext;

class PrepareWrapperArchiveCreatorTest {

    private PrepareWrapperArchiveCreator creatorToTest;
    private FileNameSupport fileNameSupport;
    private TestFileWriter writer;

    @BeforeEach
    void beforeEach() {
        fileNameSupport = mock(FileNameSupport.class);
        ArchiveSupport archiveSupport = new ArchiveSupport();
        writer = new TestFileWriter();

        PrepareWrapperSechubConfigurationSupport sechubConfigurationSupport = new PrepareWrapperSechubConfigurationSupport(fileNameSupport);
        creatorToTest = new PrepareWrapperArchiveCreator(archiveSupport, sechubConfigurationSupport);

    }

    @Test
    void createArchive_creates_archive_for_binary() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-sechub_archive-creator").toFile();
        tempDir.deleteOnExit();

        Path uploadDirectory = tempDir.toPath().resolve(Path.of("upload"));
        Path testDownload = tempDir.toPath().resolve(Path.of("test-download"));
        Path testTarFilename = Path.of("test-tar.tar");
        writer.writeTextToFile(testDownload.resolve(testTarFilename).toFile(), "testText", true);

        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/sechub_remote_data_config_binary_code_scan_example.json"));

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.getAbsolutePath());
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);

        PrepareToolContext abstractToolContext = mock(AbstractPrepareToolContext.class);
        when(abstractToolContext.getUploadDirectory()).thenReturn(uploadDirectory);
        when(abstractToolContext.getToolDownloadDirectory()).thenReturn(tempDir.toPath().resolve(testDownload));

        List<Path> pathList = new ArrayList<>();
        pathList.add(testDownload.resolve(testTarFilename));
        when(fileNameSupport.getTarFilesFromDirectory(any())).thenReturn(pathList);

        /* execute */
        assertDoesNotThrow(() -> creatorToTest.create(context, abstractToolContext));

        /* test */
        assertTrue(new File(uploadDirectory + "/binaries.tar").exists());
    }

    @Test
    void createArchive_creates_archive_for_source() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-sechub_archive-creator").toFile();
        tempDir.deleteOnExit();

        Path uploadDirectory = tempDir.toPath().resolve(Path.of("upload"));
        Path testDownload = tempDir.toPath().resolve(Path.of("test-download"));
        Path testRepoName = Path.of("test-repos");
        writer.writeTextToFile(testDownload.resolve(testRepoName).resolve(Path.of(".git")).toFile(), "testText", true);

        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/sechub_remote_data_config_source_code_scan_example.json"));

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.getAbsolutePath());
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);

        List<Path> pathList = new ArrayList<>();
        pathList.add(testRepoName.getFileName());
        when(fileNameSupport.getRepositoriesFromDirectory(any())).thenReturn(pathList);

        PrepareToolContext abstractToolContext = mock(AbstractPrepareToolContext.class);
        when(abstractToolContext.getUploadDirectory()).thenReturn(uploadDirectory);
        when(abstractToolContext.getToolDownloadDirectory()).thenReturn(testDownload);

        /* execute */
        assertDoesNotThrow(() -> creatorToTest.create(context, abstractToolContext));

        /* test */
        assertTrue(new File(uploadDirectory + "/sourcecode.zip").exists());
    }

}