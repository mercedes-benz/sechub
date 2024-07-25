// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.test.TestUtil;
import com.mercedesbenz.sechub.wrapper.prepare.InputValidatorExitcode;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.upload.FileNameSupport;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadService;

class SkopeoPrepareWrapperModuleTest {

    private SkopeoPrepareWrapperModule moduleToTest;
    private SkopeoPrepareInputValidator inputValidator;
    private SkopeoWrapper skopeoWrapper;
    private TestFileWriter writer;
    private FileNameSupport fileNameSupport;

    @BeforeEach
    void beforeEach() {
        moduleToTest = new SkopeoPrepareWrapperModule();
        inputValidator = mock(SkopeoPrepareInputValidator.class);
        writer = new TestFileWriter();
        skopeoWrapper = mock(SkopeoWrapper.class);
        fileNameSupport = mock(FileNameSupport.class);
        PrepareWrapperUploadService uploadService = mock(PrepareWrapperUploadService.class);

        ReflectionTestUtils.setField(moduleToTest, "enabled", true);

        moduleToTest.inputValidator = inputValidator;
        moduleToTest.skopeoWrapper = skopeoWrapper;
        moduleToTest.filesSupport = fileNameSupport;
        moduleToTest.uploadService = uploadService;
    }

    @Test
    void when_inputValidator_throws_InputValidatorException_prepare_rethrows_it() throws IOException, PrepareWrapperInputValidatorException {
        /* prepare */
        PrepareWrapperContext context = createContext();

        /* @formatter:off */
        doThrow(new PrepareWrapperInputValidatorException("test", InputValidatorExitcode.LOCATION_NOT_MATCHING_PATTERN)).
            when(inputValidator).validate(context);
        /* @formatter:on */

        /* execute + test */
        assertThrows(PrepareWrapperInputValidatorException.class, () -> moduleToTest.prepare(context));

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void module_isResponsible_returns_result_from_input_validator_is_accepted(boolean accepting) throws IOException {
        /* prepare */
        PrepareWrapperContext context = createContext();

        when(inputValidator.isAccepting(context)).thenReturn(accepting);

        /* execute */
        boolean result = moduleToTest.isResponsibleToPrepare(context);

        /* test */
        assertEquals(accepting, result);

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void module_isEnabled_returns_result_from_field(boolean enabled) throws IOException {
        /* prepare */
        moduleToTest.enabled = enabled;

        /* execute */
        boolean result = moduleToTest.isEnabled();

        /* test */
        assertEquals(enabled, result);

    }

    @Test
    void when_inputvalidator_throws_exception_prepare_throws_exception() throws PrepareWrapperInputValidatorException {
        /* prepare */
        PrepareWrapperContext context = createContext();
        doThrow(new IllegalStateException("test")).when(inputValidator).validate(context);

        /* execute + test */
        assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));
        verify(inputValidator).validate(context); // validator was called
    }

    @Test
    void prepare_successful_when_user_credentials_are_configured_correctly() throws IOException {
        /* prepare */
        Path tempDir = TestUtil.createTempDirectoryInBuildFolder("skopeo-upload-folder");

        Path testFile = Path.of("testimage.tar");
        Path downloadDirectory = tempDir.resolve(SkopeoWrapperConstants.DOWNLOAD_DIRECTORY_NAME);
        writer.save(downloadDirectory.resolve(testFile).toFile(), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.toString());

        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        initRemoteDataConfiguration("my-example-location", "my-example-name", "ghp_exampleAPITOKEN8ffne3l6g9f393r8fbcsf", context);

        when(fileNameSupport.getTarFilesFromDirectory(downloadDirectory)).thenReturn(List.of(testFile));

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(inputValidator).validate(context); // validator was called
        verify(skopeoWrapper).download(any(SkopeoContext.class));
    }

    private void initRemoteDataConfigurationAnonymous(String location, PrepareWrapperContext context) {
        initRemoteDataConfiguration(location, null, null, context);
    }

    private void initRemoteDataConfiguration(String location, String username, String password, PrepareWrapperContext context) {
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        remoteDataConfiguration.setType("docker");
        remoteDataConfiguration.setLocation(location);

        if (username == null && password == null) {
            return;
        }
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName(username);
        user.setPassword(password);
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
    }

    @Test
    void prepare_does_validate_download_and_cleanup_docker_image_when_no_credentials_configured() throws IOException {
        /* prepare */
        Path tempDir = TestUtil.createTempDirectoryInBuildFolder("skopeo-upload-folder");

        Path downloadDirectory = tempDir.resolve(SkopeoWrapperConstants.DOWNLOAD_DIRECTORY_NAME);
        Path testFile = downloadDirectory.resolve("testimage.tar");
        writer.save(testFile.toFile(), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        initRemoteDataConfigurationAnonymous("my-example-locatin", context);

        when(fileNameSupport.getTarFilesFromDirectory(downloadDirectory)).thenReturn(List.of(testFile));

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(inputValidator).validate(context); // validator was called
        verify(skopeoWrapper).download(any(SkopeoContext.class));
    }

    private PrepareWrapperContext createContext() {
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn("test-upload-folder");
        return new PrepareWrapperContext(createFromJSON("{}"), environment);
    }

}