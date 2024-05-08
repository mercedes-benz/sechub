package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
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
        gitInputValidator = new GitInputValidator();
        git = mock(WrapperGit.class);

        moduleToTest.git = git;
        moduleToTest.gitInputValidator = gitInputValidator;
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/notARepo/", "http://my.eval.com", "example.org" })
    void isAbleToPrepare_returns_false_when_no_git_remote_data_was_configured(String location) {
        /* prepare */
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "name": "remote_example_name",
                        "remote": {
                          "location": "$location"
                        }
                      }
                    ]
                  },
                  "codeScan": {
                    "use": [
                      "remote_example_name"
                    ]
                  }
                }
                """.replace("$location", location);
        SecHubConfigurationModel model = createFromJSON(json);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        PrepareWrapperContext context = new PrepareWrapperContext(model, environment);

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertFalse(ableToPrepare);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/notARepo/", "http://my.eval.com", "example.org", " " })
    void isAbleToPrepare_returns_false_when_git_remote_data_type_was_configured_but_is_not_git_location(String location) {
        /* prepare */
        PrepareWrapperContext context = createContext();
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation(location);
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);

        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertFalse(ableToPrepare);
    }

    @Test
    void isAbleToPrepare_returns_false_when_configuration_is_empty() {
        /* prepare */
        PrepareWrapperContext context = createContext();
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertFalse(ableToPrepare);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo.git/", "http://host.xz/path/to/repo.git/", "git://host.xz/path/to/repo.git/",
            "git@host.com:my-repo/example.git" })
    void isAbleToPrepare_returns_true_when_git_remote_location_was_configured(String location) {
        /* prepare */
        PrepareWrapperContext context = createContext();
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation(location);
        remoteDataConfiguration.setType("");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);
        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleGitEnabled", true);

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertTrue(ableToPrepare);
    }

    @Test
    void prepare_throws_exception_when_credentials_are_empty() {
        /* prepare */
        PrepareWrapperContext context = createContext();
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined credentials have no credential"));
    }

    @Test
    void prepare_throws_exception_when_no_username_found() {
        /* prepare */
        PrepareWrapperContext context = createContext();
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setPassword("my-example-password");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined username must not be null or empty."));
    }

    @Test
    void prepare_throws_exception_when_no_password_found() {
        /* prepare */
        PrepareWrapperContext context = createContext();
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined password must not be null or empty."));
    }

    @Test
    void prepare_successful_when_user_credentials_are_configured_correctly() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = ".git";
        writer.save(new File(tempDir, filename), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        user.setPassword("ghp_exampleAPITOKEN8ffne3l6g9f393r8fbcsf");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleGitEnabled", true);

        /* execute */
        moduleToTest.prepare(context);

        /* test */
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

        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(git).downloadRemoteData(any(GitContext.class));
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

    @ParameterizedTest
    @ValueSource(strings = { "git", "GIT", "gIT" })
    void isMatchingGitType_returns_true_when_git_is_configured(String type) {
        /* execute */
        boolean result = moduleToTest.isMatchingGitType(type);

        /* test */
        assertTrue(result);
    }

    private PrepareWrapperContext createContext() {
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn("test-upload-folder");
        return new PrepareWrapperContext(createFromJSON("{}"), environment);
    }

}