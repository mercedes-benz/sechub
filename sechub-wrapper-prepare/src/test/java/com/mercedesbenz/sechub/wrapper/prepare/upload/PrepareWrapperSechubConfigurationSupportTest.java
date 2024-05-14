package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

class PrepareWrapperSechubConfigurationSupportTest {

    PrepareWrapperSechubConfigurationSupport supportToTest;

    TestFileWriter writer;

    TarFileSupport tarFileSupport;

    @BeforeEach
    void beforeEach() {
        supportToTest = new PrepareWrapperSechubConfigurationSupport();
        supportToTest.tarFileSupport = new TarFileSupport();
        writer = new TestFileWriter();
    }

    @Test
    void replaceRemoteDataWithFilesystem_throws_IllegalArgumentException_when_SecHubConfigurationModel_is_null() {
        /* prepare */
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getSecHubConfiguration()).thenReturn(null);

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> supportToTest.replaceRemoteDataWithFilesystem(context));

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

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context);

        /* test */
        assertEquals(model, result);
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_SecHubConfigurationModel_when_SecHubConfigurationModel_data_binaries_is_not_empty() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("sechub_config_support").toFile();
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

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context);

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
        assertEquals(path + testTarFilename, folders.get(0));
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_SecHubConfigurationModel_when_SecHubConfigurationModel_data_sources_is_not_empty() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("sechub_config_support").toFile();
        tempDir.deleteOnExit();

        String path = tempDir.getAbsolutePath();
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_source_code_scan_example.json"));

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(path);
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context);

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
        assertEquals(path, folders.get(0));
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_expected_SecHubConfigurationModel_for_remote_binaries() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("sechub_config_support").toFile();
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

        SecHubConfigurationModel expectedModel = new SecHubConfigurationModel();
        expectedModel.setApiVersion(model.getApiVersion());
        expectedModel.setCodeScan(model.getCodeScan().get());
        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubBinaryDataConfiguration binary = new SecHubBinaryDataConfiguration();
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        fileSystemConfiguration.getFolders().add(path + testTarFilename);
        binary.setFileSystem(fileSystemConfiguration);
        binary.setUniqueName("remote_example_name");
        data.getBinaries().add(binary);
        expectedModel.setData(data);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context);

        /* test */
        String stringResult = JSONConverter.get().toJSON(result);
        String stringExpected = JSONConverter.get().toJSON(expectedModel);
        assertEquals(stringExpected, stringResult);
    }

    @Test
    void replaceRemoteDataWithFilesystem_returns_expected_SecHubConfigurationModel_for_remote_sources() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("sechub_config_support").toFile();
        tempDir.deleteOnExit();

        String path = tempDir.getAbsolutePath();
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_data_config_source_code_scan_example.json"));

        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        SecHubConfigurationModel model = createFromJSON(json);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(path);
        when(context.getSecHubConfiguration()).thenReturn(model);
        when(context.getEnvironment()).thenReturn(environment);

        SecHubConfigurationModel expectedModel = new SecHubConfigurationModel();
        expectedModel.setApiVersion(model.getApiVersion());
        expectedModel.setCodeScan(model.getCodeScan().get());
        SecHubDataConfiguration data = new SecHubDataConfiguration();
        SecHubSourceDataConfiguration source = new SecHubSourceDataConfiguration();
        SecHubFileSystemConfiguration fileSystemConfiguration = new SecHubFileSystemConfiguration();
        fileSystemConfiguration.getFolders().add(path);
        source.setFileSystem(fileSystemConfiguration);
        source.setUniqueName("remote_example_name");
        data.getSources().add(source);
        expectedModel.setData(data);

        /* execute */
        SecHubConfigurationModel result = supportToTest.replaceRemoteDataWithFilesystem(context);

        /* test */
        String stringResult = JSONConverter.get().toJSON(result);
        String stringExpected = JSONConverter.get().toJSON(expectedModel);
        assertEquals(stringExpected, stringResult);
    }
}