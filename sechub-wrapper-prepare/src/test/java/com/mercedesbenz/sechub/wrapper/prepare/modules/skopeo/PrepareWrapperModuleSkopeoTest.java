package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

class PrepareWrapperModuleSkopeoTest {

    PrepareWrapperModuleSkopeo moduleToTest;
    SkopeoInputValidator skopeoInputValidator;
    WrapperSkopeo skopeo;
    TestFileWriter writer;

    @BeforeEach
    void beforeEach() {
        moduleToTest = new PrepareWrapperModuleSkopeo();
        skopeoInputValidator = mock(SkopeoInputValidator.class);
        writer = new TestFileWriter();
        skopeo = mock(WrapperSkopeo.class);

        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleSkopeoEnabled", true);

        moduleToTest.skopeoInputValidator = skopeoInputValidator;
        moduleToTest.skopeo = skopeo;
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
        String filename = "testimage.tar";
        writer.save(new File(tempDir, filename), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());
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

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(skopeo).download(any(SkopeoContext.class));
        verify(skopeo).cleanUploadDirectory(tempDir.toString());
    }

    @Test
    void prepare_successful_when_no_credentials_are_configured() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = "testimage.tar";
        writer.save(new File(tempDir, filename), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);
        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(skopeo).download(any(SkopeoContext.class));
        verify(skopeo).cleanUploadDirectory(tempDir.toString());
    }

    @Test
    void prepare_returns_false_when_modul_is_disabled() throws IOException {
        /* prepare */

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn("temp");
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

    @Test
    void isDownloadSuccessful_returns_true_when_tar_file_in_directory() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = "testimage.tar";
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getEnvironment()).thenReturn(mock(PrepareWrapperEnvironment.class));
        writer.save(new File(tempDir, filename), "some text", true);
        when(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());

        /* execute */
        boolean result = moduleToTest.isDownloadSuccessful(context);

        /* test */
        assertTrue(result);
    }

    @Test
    void isDownloadSuccessful_returns_false_when_no_tar_file_in_directory() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        writer.save(tempDir, "some text", true);
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getEnvironment()).thenReturn(mock(PrepareWrapperEnvironment.class));
        when(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());

        /* execute */
        boolean result = moduleToTest.isDownloadSuccessful(context);

        /* test */
        assertFalse(result);
    }

    private PrepareWrapperContext createContext() {
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn("test-upload-folder");
        return new PrepareWrapperContext(createFromJSON("{}"), environment);
    }

}