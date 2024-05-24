package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

class PrepareWrapperModuleGitTest {

    PrepareWrapperModuleGit moduleToTest;

    WrapperGit git;

    TestFileWriter writer;

    GitInputValidator gitInputValidator;

    PrepareWrapperUploadService uploadService;

    FileNameSupport filesSupport;

    Path gitDownloadFolder = Path.of(GitContext.DOWNLOAD_DIRECTORY_NAME);

    private Path testRepo = Path.of("test-repo");

    @BeforeEach
    void beforeEach() {
        moduleToTest = new PrepareWrapperModuleGit();
        writer = new TestFileWriter();
        gitInputValidator = mock(GitInputValidator.class);
        git = mock(WrapperGit.class);
        uploadService = mock(PrepareWrapperUploadService.class);
        filesSupport = mock(FileNameSupport.class);

        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleGitEnabled", true);

        moduleToTest.uploadService = uploadService;
        moduleToTest.filesSupport = filesSupport;
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
    void when_inputValidator_throws_exception_prepare_throws_exception() throws PrepareWrapperInputValidatorException {
        /* prepare */
        PrepareWrapperContext context = createContext();
        doThrow(new IllegalStateException("test")).when(gitInputValidator).validate(context);

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
        verify(git).downloadRemoteData(any(GitContext.class));
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

        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleGitEnabled", false);

        /* execute */
        boolean result = moduleToTest.prepare(context);

        /* test */
        assertFalse(result);
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
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.assertDownloadSuccessful(context));

        /* test */
        assertEquals("Download of git repository: " + repository.getFileName() + " was not successful. Git folder not found.", exception.getMessage());
    }

    private PrepareWrapperContext createContext() {
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsJobWorkspaceLocation()).thenReturn("test-upload-folder");
        return new PrepareWrapperContext(createFromJSON("{}"), environment);
    }

}