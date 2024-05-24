package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.InputValidatorExitcode;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.upload.FileNameSupport;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadService;

class PrepareWrapperModuleSkopeoTest {

    PrepareWrapperModuleSkopeo moduleToTest;
    SkopeoInputValidator skopeoInputValidator;
    WrapperSkopeo skopeo;
    TestFileWriter writer;
    FileNameSupport fileNameSupport;
    PrepareWrapperUploadService uploadService;
    private Path skopeDownloadFolder = Path.of(SkopeoContext.DOWNLOAD_DIRECTORY_NAME);

    @BeforeEach
    void beforeEach() {
        moduleToTest = new PrepareWrapperModuleSkopeo();
        skopeoInputValidator = mock(SkopeoInputValidator.class);
        writer = new TestFileWriter();
        skopeo = mock(WrapperSkopeo.class);
        fileNameSupport = mock(FileNameSupport.class);
        uploadService = mock(PrepareWrapperUploadService.class);

        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleSkopeoEnabled", true);

        moduleToTest.skopeoInputValidator = skopeoInputValidator;
        moduleToTest.skopeo = skopeo;
        moduleToTest.filesSupport = fileNameSupport;
        moduleToTest.uploadService = uploadService;
    }

    @Test
    void when_inputValidator_throws_InputValidatorException_prepare_return_false() throws IOException, PrepareWrapperInputValidatorException {
        /* prepare */
        PrepareWrapperContext context = createContext();
        doThrow(new PrepareWrapperInputValidatorException("test", InputValidatorExitcode.LOCATION_NOT_MATCHING_PATTERN)).when(skopeoInputValidator)
                .validate(context);

        /* execute */
        boolean result = moduleToTest.prepare(context);

        /* test */
        assertFalse(result);
    }

    @Test
    void when_inputvalidator_throws_exception_prepare_throws_exception() throws PrepareWrapperInputValidatorException {
        /* prepare */
        PrepareWrapperContext context = createContext();
        doThrow(new IllegalStateException("test")).when(skopeoInputValidator).validate(context);

        /* execute + test */
        assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));
    }

    @Test
    void prepare_successful_when_user_credentials_are_configured_correctly() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();

        Path testFile = Path.of("testimage.tar");
        Path downloadDirectory = tempDir.toPath().resolve(skopeDownloadFolder);
        writer.save(downloadDirectory.resolve(testFile).toFile(), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        user.setPassword("ghp_exampleAPITOKEN8ffne3l6g9f393r8fbcsf");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        when(fileNameSupport.getTarFilesFromDirectory(downloadDirectory)).thenReturn(List.of(testFile));

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(skopeo).download(any(SkopeoContext.class));
        verify(skopeo).cleanUploadDirectory(tempDir.toPath().resolve(skopeDownloadFolder));
    }

    @Test
    void prepare_successful_when_no_credentials_are_configured() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();

        Path testFile = Path.of("testimage.tar");
        Path downloadDirectory = tempDir.toPath().resolve(skopeDownloadFolder);
        writer.save(downloadDirectory.resolve(testFile).toFile(), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        when(fileNameSupport.getTarFilesFromDirectory(downloadDirectory)).thenReturn(List.of(testFile));

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(skopeo).download(any(SkopeoContext.class));
        verify(skopeo).cleanUploadDirectory(tempDir.toPath().resolve(skopeDownloadFolder));
    }

    @Test
    void prepare_returns_false_when_modul_is_disabled() throws IOException {
        /* prepare */

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn("temp");
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleSkopeoEnabled", false);

        /* execute */
        boolean result = moduleToTest.prepare(context);

        /* test */
        assertFalse(result);
    }

    private PrepareWrapperContext createContext() {
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn("test-upload-folder");
        return new PrepareWrapperContext(createFromJSON("{}"), environment);
    }

}