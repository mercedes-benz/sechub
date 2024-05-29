package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
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

class PrepareWrapperModuleGitTest {

    private PrepareWrapperModuleGit moduleToTest;

    private WrapperGit git;

    TestFileWriter writer;

    GitInputValidator gitInputValidator;

    @BeforeEach
    void beforeEach() {
        moduleToTest = new PrepareWrapperModuleGit();
        writer = new TestFileWriter();
        gitInputValidator = mock(GitInputValidator.class);
        git = mock(WrapperGit.class);
        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleGitEnabled", true);

        moduleToTest.git = git;
        moduleToTest.gitInputValidator = gitInputValidator;
    }

    @Test
    void when_inputValidator_throws_InputValidatorException_prepare_return_false() throws IOException, PrepareWrapperInputValidatorException {
        /* prepare */
        PrepareWrapperContext context = createContext();
        doThrow(new PrepareWrapperInputValidatorException("test", InputValidatorExitcode.LOCATION_NOT_MATCHING_PATTERN)).when(gitInputValidator)
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
        doThrow(new IllegalStateException("test")).when(gitInputValidator).validate(context);

        /* execute + test */
        assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));
    }

    @Test
    void prepare_successful_with_user_credentials_configured() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = ".git";
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
        remoteDataConfiguration.setType("git");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleGitEnabled", true);

        /* execute */
        boolean result = moduleToTest.prepare(context);

        /* test */
        assertTrue(result);
        verify(git).downloadRemoteData(any(GitContext.class));
    }

    @Test
    void prepare_successful_when_no_credentials_are_configured() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = ".git";
        writer.save(new File(tempDir, filename), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(git).downloadRemoteData(any(GitContext.class));
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

        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleGitEnabled", false);

        /* execute */
        boolean result = moduleToTest.prepare(context);

        /* test */
        assertFalse(result);
    }

    @Test
    void isDownloadSuccessful_returns_true_when_git_file_in_directory() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = ".git";
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
    void isDownloadSuccessful_returns_false_when_no_git_file_in_directory() throws IOException {
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