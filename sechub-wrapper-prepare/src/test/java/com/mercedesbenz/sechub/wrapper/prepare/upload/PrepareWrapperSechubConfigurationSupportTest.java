// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubBinaryDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubSourceDataConfiguration;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareToolContext;

class PrepareWrapperSechubConfigurationSupportTest {

    private PrepareWrapperSechubConfigurationSupport supportToTest;
    private TestFileWriter writer;
    private PrepareWrapperContext prepareContext;
    private PrepareToolContext toolContext;
    private PrepareWrapperEnvironment environment;
    private Path uploadDirectory;
    private Path testDownload;
    private Path testTarFilename;
    private Path testRepoName;
    private File workspaceDirectory;

    @BeforeEach
    void beforeEach() throws Exception {
        supportToTest = new PrepareWrapperSechubConfigurationSupport();
        supportToTest.fileNameSupport = new FileNameSupport();
        writer = new TestFileWriter();

        prepareContext = mock(PrepareWrapperContext.class);
        toolContext = mock(PrepareToolContext.class);
        environment = mock(PrepareWrapperEnvironment.class);

        workspaceDirectory = Files.createTempDirectory("test-sechub-prepare_sechubConfigurationSupport").toFile();
        workspaceDirectory.deleteOnExit();

        uploadDirectory = workspaceDirectory.toPath().resolve(Path.of("upload"));
        testDownload = workspaceDirectory.toPath().resolve(Path.of("test-download"));
        testTarFilename = Path.of("test-tar.tar");
        testRepoName = Path.of("git-repo-name");

        when(environment.getPdsJobWorkspaceLocation()).thenReturn(workspaceDirectory.getAbsolutePath());
        when(prepareContext.getEnvironment()).thenReturn(environment);

        when(toolContext.getUploadDirectory()).thenReturn(uploadDirectory);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testDownload);
    }

    @Test
    void replaceRemoteDataWithFilesystem_throws_IllegalArgumentException_when_SecHubConfigurationModel_is_null() {
        /* prepare */

        when(prepareContext.getSecHubConfiguration()).thenReturn(null);


        Path testPath = Path.of("path");
        when(toolContext.getUploadDirectory()).thenReturn(testPath);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testPath);

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> supportToTest.replaceRemoteDataWithFilesystem(prepareContext, toolContext));

        /* test */
        assertEquals("SecHubConfigurationModel cannot be null", exception.getMessage());
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_SecHubConfigurationModel_when_SecHubConfigurationModel_data_is_empty() {
        /* prepare */

        SecHubConfigurationModel model = new SecHubConfigurationModel();
        when(prepareContext.getSecHubConfiguration()).thenReturn(model);
        SecHubDataConfiguration data = new SecHubDataConfiguration();
        model.setData(data);
        when(prepareContext.getSecHubConfiguration()).thenReturn(model);

        Path testPath = Path.of("path");
        when(toolContext.getUploadDirectory()).thenReturn(testPath);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testPath);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(prepareContext, toolContext);

        /* test */
        assertEquals(model, result);
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_SecHubConfigurationModel_when_SecHubConfigurationModel_data_binaries_is_not_empty() throws IOException {
        /* prepare */
        writer.writeTextToFile(testDownload.resolve(testTarFilename).toFile(), "testText", true);

        SecHubConfigurationModel model = loadModelFromTestFile("sechub_remote_data_config_binary_code_scan_example.json");

        when(prepareContext.getSecHubConfiguration()).thenReturn(model);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(prepareContext, toolContext);

        /* test */
        assertNotNull(result);
        Optional<SecHubDataConfiguration> data = result.getData();
        assertTrue(data.isPresent());
        List<SecHubSourceDataConfiguration> sources = data.get().getSources();
        assertTrue(sources.isEmpty());
        List<SecHubBinaryDataConfiguration> binaries = data.get().getBinaries();
        assertFalse(binaries.isEmpty());
        SecHubBinaryDataConfiguration binary = binaries.get(0);
        assertFalse(binary.getRemote().isPresent());
        assertNotNull(binary.getFileSystem());
        List<String> folders = binary.getFileSystem().get().getFolders();
        assertEquals(1, folders.size());
        assertEquals(testTarFilename.getFileName().toString(), folders.get(0));
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_SecHubConfigurationModel_when_SecHubConfigurationModel_data_sources_is_not_empty() throws IOException {
        /* prepare */
        writer.writeTextToFile(testDownload.resolve(testRepoName).resolve(Path.of(".git")).toFile(), "testText", true);

        SecHubConfigurationModel model = loadModelFromTestFile("sechub_remote_data_config_source_code_scan_example.json");

        when(prepareContext.getSecHubConfiguration()).thenReturn(model);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(prepareContext, toolContext);

        /* test */
        assertNotNull(result);
        Optional<SecHubDataConfiguration> data = result.getData();
        assertTrue(data.isPresent());
        List<SecHubBinaryDataConfiguration> binaries = data.get().getBinaries();
        assertTrue(binaries.isEmpty());
        List<SecHubSourceDataConfiguration> sources = data.get().getSources();
        assertFalse(sources.isEmpty());
        SecHubSourceDataConfiguration source = sources.get(0);
        assertFalse(source.getRemote().isPresent());
        assertNotNull(source.getFileSystem());
        List<String> folders = source.getFileSystem().get().getFolders();
        assertEquals(1, folders.size());
        assertEquals("git-repo-name", folders.get(0));
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_expected_SecHubConfigurationModel_for_remote_binaries() throws IOException {
        /* prepare */
        writer.writeTextToFile(testDownload.resolve(testTarFilename).toFile(), "testText", true);

        SecHubConfigurationModel model = loadModelFromTestFile("sechub_remote_data_config_binary_code_scan_example.json");

        when(prepareContext.getSecHubConfiguration()).thenReturn(model);

        SecHubConfigurationModel expectedModel = new SecHubConfigurationModel();
        expectedModel.setApiVersion(model.getApiVersion());
        expectedModel.setCodeScan(model.getCodeScan().get());
        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubBinaryDataConfiguration binary = new SecHubBinaryDataConfiguration();
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        fileSystemConfiguration.getFolders().add(testTarFilename.getFileName().toString());
        binary.setFileSystem(fileSystemConfiguration);
        binary.setUniqueName("remote_example_name");
        data.getBinaries().add(binary);
        expectedModel.setData(data);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(prepareContext, toolContext);

        /* test */
        String stringResult = JSONConverter.get().toJSON(result);
        String stringExpected = JSONConverter.get().toJSON(expectedModel);
        assertEquals(stringExpected, stringResult);
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_expected_SecHubConfigurationModel_for_remote_sources() throws IOException {
        /* prepare */
        writer.writeTextToFile(testDownload.resolve(testRepoName).resolve(Path.of(".git")).toFile(), "testText", true);

        SecHubConfigurationModel model = loadModelFromTestFile("sechub_remote_data_config_source_code_scan_example.json");
        when(prepareContext.getSecHubConfiguration()).thenReturn(model);

        SecHubConfigurationModel expectedModel = new SecHubConfigurationModel();
        expectedModel.setApiVersion(model.getApiVersion());
        expectedModel.setCodeScan(model.getCodeScan().get());
        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubSourceDataConfiguration source = new SecHubSourceDataConfiguration();
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        fileSystemConfiguration.getFolders().add("git-repo-name");
        source.setFileSystem(fileSystemConfiguration);
        source.setUniqueName("remote_example_name");
        data.getSources().add(source);
        expectedModel.setData(data);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(prepareContext, toolContext);

        /* test */
        String stringResult = JSONConverter.get().toJSON(result);
        String stringExpected = JSONConverter.get().toJSON(expectedModel);
        assertEquals(stringExpected, stringResult);
    }

    private SecHubConfigurationModel loadModelFromTestFile(String fileName) {
        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/" + fileName));
        return createFromJSON(json);
    }
}