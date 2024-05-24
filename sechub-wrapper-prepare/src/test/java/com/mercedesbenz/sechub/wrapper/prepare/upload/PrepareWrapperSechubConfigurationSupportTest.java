package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
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

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.ToolContext;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

class PrepareWrapperSechubConfigurationSupportTest {

    PrepareWrapperSechubConfigurationSupport supportToTest;

    TestFileWriter writer;

    @BeforeEach
    void beforeEach() {
        supportToTest = new PrepareWrapperSechubConfigurationSupport();
        supportToTest.fileNameSupport = new FileNameSupport();
        writer = new TestFileWriter();
    }

    @Test
    void replaceRemoteDataWithFilesystem_throws_IllegalArgumentException_when_SecHubConfigurationModel_is_null() {
        /* prepare */
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getSecHubConfiguration()).thenReturn(null);

        ToolContext toolContext = mock(ToolContext.class);
        Path testPath = Path.of("path");
        when(toolContext.getUploadDirectory()).thenReturn(testPath);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testPath);

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> supportToTest.replaceRemoteDataWithFilesystem(context, toolContext));

        /* test */
        assertEquals("SecHubConfigurationModel cannot be null", exception.getMessage());
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_SecHubConfigurationModel_when_SecHubConfigurationModel_data_is_empty() {
        /* prepare */
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        when(context.getSecHubConfiguration()).thenReturn(model);
        SecHubDataConfiguration data = new SecHubDataConfiguration();
        model.setData(data);
        when(context.getSecHubConfiguration()).thenReturn(model);

        ToolContext toolContext = mock(ToolContext.class);
        Path testPath = Path.of("path");
        when(toolContext.getUploadDirectory()).thenReturn(testPath);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testPath);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context, toolContext);

        /* test */
        assertEquals(model, result);
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_SecHubConfigurationModel_when_SecHubConfigurationModel_data_binaries_is_not_empty() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-sechub-prepare_sechubConfigurationSupport").toFile();
        tempDir.deleteOnExit();

        Path uploadDirectory = tempDir.toPath().resolve(Path.of("upload"));
        Path testDownload = tempDir.toPath().resolve(Path.of("test-download"));
        Path testTarFilename = Path.of("test-tar.tar");
        writer.save(testDownload.resolve(testTarFilename).toFile(), "testText", true);

        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_binary_code_scan_example.json"));

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.getAbsolutePath());
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);

        ToolContext toolContext = mock(ToolContext.class);
        when(toolContext.getUploadDirectory()).thenReturn(uploadDirectory);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testDownload);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context, toolContext);

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
        File tempDir = Files.createTempDirectory("test-sechub-prepare_sechubConfigurationSupport").toFile();
        tempDir.deleteOnExit();

        Path uploadDirectory = tempDir.toPath().resolve(Path.of("upload"));
        Path testDownload = tempDir.toPath().resolve(Path.of("test-download"));
        Path testRepoName = Path.of("git-repo-name");
        writer.save(testDownload.resolve(testRepoName).resolve(Path.of(".git")).toFile(), "testText", true);

        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_source_code_scan_example.json"));

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.getAbsolutePath());
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);

        ToolContext toolContext = mock(ToolContext.class);
        when(toolContext.getUploadDirectory()).thenReturn(uploadDirectory);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testDownload);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context, toolContext);

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
        File tempDir = Files.createTempDirectory("test-sechub-prepare_sechubConfigurationSupport").toFile();
        tempDir.deleteOnExit();

        Path uploadDirectory = tempDir.toPath().resolve(Path.of("upload"));
        Path testDownload = tempDir.toPath().resolve(Path.of("test-download"));
        Path testTarFilename = Path.of("test-tar.tar");
        writer.save(testDownload.resolve(testTarFilename).toFile(), "testText", true);

        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_binary_code_scan_example.json"));

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.getAbsolutePath());
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);

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

        ToolContext toolContext = mock(ToolContext.class);
        when(toolContext.getUploadDirectory()).thenReturn(uploadDirectory);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testDownload);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context, toolContext);

        /* test */
        String stringResult = JSONConverter.get().toJSON(result);
        String stringExpected = JSONConverter.get().toJSON(expectedModel);
        assertEquals(stringExpected, stringResult);
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_expected_SecHubConfigurationModel_for_remote_sources() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("test-sechub-prepare_sechubConfigurationSupport").toFile();
        tempDir.deleteOnExit();

        Path uploadDirectory = tempDir.toPath().resolve(Path.of("upload"));
        Path testDownload = tempDir.toPath().resolve(Path.of("test-download"));
        Path testRepoName = Path.of("git-repo-name");
        writer.save(testDownload.resolve(testRepoName).resolve(Path.of(".git")).toFile(), "testText", true);

        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_source_code_scan_example.json"));

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.getAbsolutePath());
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);

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

        ToolContext toolContext = mock(ToolContext.class);
        when(toolContext.getUploadDirectory()).thenReturn(uploadDirectory);
        when(toolContext.getToolDownloadDirectory()).thenReturn(testDownload);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context, toolContext);

        /* test */
        String stringResult = JSONConverter.get().toJSON(result);
        String stringExpected = JSONConverter.get().toJSON(expectedModel);
        assertEquals(stringExpected, stringResult);
    }
}