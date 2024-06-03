// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.InputValidatorExitcode;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperUsageException;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.upload.FileNameSupport;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadService;

class GitPrepareWrapperModuleTest {

    private GitPrepareWrapperModule moduleToTest;
    private GitWrapper gitWrapper;
    private TestFileWriter writer;
    private GitPrepareInputValidator gitPrepareInputValidator;
    private FileNameSupport filesSupport;
    private final Path gitDownloadFolder = Path.of(GitContext.DOWNLOAD_DIRECTORY_NAME);
    private final Path testRepo = Path.of("test-repo");

    @BeforeEach
    void beforeEach() {
        moduleToTest = new GitPrepareWrapperModule();
        writer = new TestFileWriter();
        gitPrepareInputValidator = mock(GitPrepareInputValidator.class);
        gitWrapper = mock(GitWrapper.class);
        filesSupport = mock(FileNameSupport.class);
        PDSLogSanitizer pdsLogSanitizer = mock(PDSLogSanitizer.class);

        PrepareWrapperUploadService uploadService = mock(PrepareWrapperUploadService.class);

        ReflectionTestUtils.setField(moduleToTest, "enabled", true);

        moduleToTest.uploadService = uploadService;
        moduleToTest.filesSupport = filesSupport;
        moduleToTest.gitWrapper = gitWrapper;
        moduleToTest.gitPrepareInputValidator = gitPrepareInputValidator;
        moduleToTest.pdsLogSanitizer = pdsLogSanitizer;
    }

    @Test
    void when_inputValidator_throws_InputValidatorException_it_is_thrown_by_module_as_well() throws Exception {
        /* prepare */
        PrepareWrapperContext context = createContext();

        /* @formatter:off */
        PrepareWrapperInputValidatorException exception = new PrepareWrapperInputValidatorException("test", InputValidatorExitcode.LOCATION_NOT_MATCHING_PATTERN);
        doThrow(exception).
           when(gitPrepareInputValidator).validate(context);
        /* @formatter:on */

        /* execute + test */
        assertThrows(PrepareWrapperInputValidatorException.class, () -> moduleToTest.prepare(context));

    }

    @Test
    void when_inputValidator_throws_exception_prepare_rethrows_such_exception() throws PrepareWrapperInputValidatorException {
        /* prepare */
        PrepareWrapperContext context = createContext();
        doThrow(new IllegalStateException("test")).when(gitPrepareInputValidator).validate(context);

        /* execute + test */
        assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));
    }

    @Test
    void prepare_successful_with_user_credentials_configured() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("download-folder").toFile();
        tempDir.deleteOnExit();

        Path repository = tempDir.toPath().resolve(gitDownloadFolder).resolve(testRepo);
        Path gitFile = repository.resolve(".git");
        writer.save(gitFile.toFile(), "some text", true);

        List<Path> subfolder = new ArrayList<>();
        subfolder.add(repository);
        when(filesSupport.getRepositoriesFromDirectory(any())).thenReturn(subfolder);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();

        String testUsername = "my-example-name";
        String testPassword = "ghp_exampleAPITOKEN8ffne3l6g9f393r8fbcsf";

        user.setName(testUsername);
        user.setPassword(testPassword);
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        moduleToTest.enabled = true;

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        ArgumentCaptor<GitContext> gitContextCaptor = ArgumentCaptor.forClass(GitContext.class);
        verify(gitWrapper).downloadRemoteData(gitContextCaptor.capture());

        GitContext gitContext = gitContextCaptor.getValue();
        assertEquals("my-example-location", gitContext.getLocation());
        assertEquals("my-example-name", gitContext.getUnsealedUsername());
        assertEquals("ghp_exampleAPITOKEN8ffne3l6g9f393r8fbcsf", gitContext.getUnsealedPassword());
    }

    @Test
    void prepare_successful_when_no_credentials_are_configured() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("download-folder").toFile();
        tempDir.deleteOnExit();

        Path repository = tempDir.toPath().resolve(gitDownloadFolder).resolve(testRepo);
        Path gitFile = repository.resolve(".git");
        writer.save(gitFile.toFile(), "some text", true);

        List<Path> subfolder = new ArrayList<>();
        subfolder.add(repository);
        when(filesSupport.getRepositoriesFromDirectory(any())).thenReturn(subfolder);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(gitWrapper).downloadRemoteData(any(GitContext.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void prepare_module_is_enabled_by_parameter_pdsPrepareModuleGitEnabled(boolean param) throws IOException {
        /* prepare */
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn("temp");
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        moduleToTest.enabled = param;

        /* execute */
        boolean result = moduleToTest.isEnabled();

        /* test */
        assertEquals(result, param);
    }

    @Test
    void isDownloadSuccessful_returns_true_when_git_file_in_directory() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("download-folder").toFile();
        tempDir.deleteOnExit();

        List<Path> subfolder = new ArrayList<>();
        subfolder.add(tempDir.toPath().resolve(testRepo));
        when(filesSupport.getRepositoriesFromDirectory(any())).thenReturn(subfolder);

        Path repository = tempDir.toPath().resolve(testRepo);
        Path gitFile = repository.resolve(".git");
        writer.save(gitFile.toFile(), "some text", true);

        GitContext context = mock(GitContext.class);
        when(context.getToolDownloadDirectory()).thenReturn(tempDir.toPath());

        /* execute + test */
        assertDoesNotThrow(() -> moduleToTest.assertDownloadSuccessful(context));
    }

    @Test
    void isDownloadSuccessful_returns_false_when_no_git_file_in_directory() throws IOException {
        /* prepare */
        List<Path> subfolder = new ArrayList<>();
        subfolder.add(testRepo);
        when(filesSupport.getRepositoriesFromDirectory(any())).thenReturn(subfolder);

        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();

        Path repository = tempDir.toPath().resolve(testRepo);
        Path javaFile = repository.resolve("class.java");

        writer.save(javaFile.toFile(), "some text", true);
        GitContext context = mock(GitContext.class);
        when(context.getToolDownloadDirectory()).thenReturn(tempDir.toPath());

        /* execute */
        PrepareWrapperUsageException exception = assertThrows(PrepareWrapperUsageException.class, () -> moduleToTest.assertDownloadSuccessful(context));

        /* test */
        assertEquals("Download of git repository was not successful. Git folder (.git) not found.", exception.getMessage());
    }

    private PrepareWrapperContext createContext() {
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn("test-upload-folder");
        return new PrepareWrapperContext(createFromJSON("{}"), environment);
    }

}