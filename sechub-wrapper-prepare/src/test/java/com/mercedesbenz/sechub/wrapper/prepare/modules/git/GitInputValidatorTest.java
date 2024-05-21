package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.InputValidatorExitcode.LOCATION_NOT_MATCHING_PATTERN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

class GitInputValidatorTest {

    GitInputValidator gitInputValidatorToTest;

    TestFileWriter writer;

    @BeforeEach
    void beforeEach() {
        writer = new TestFileWriter();
        gitInputValidatorToTest = new GitInputValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com;echoMalicious", "https://example.com.git>text.txt", "https://example.com/some-git-repo.git&&cd.." })
    void validateLocation_throws_exception_when_url_does_contain_forbidden_characters(String repositoryUrl) {
        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> gitInputValidatorToTest.validateLocation(repositoryUrl));

        /* test */
        assertTrue(exception.getMessage().contains("Defined URL must not contain forbidden characters: "));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://y-git-host/my-git-user/my-git-repo.git", "https://example.org/some-git-repo.git", "git@gitrepo:my-repo.git" })
    void validateLocation_does_not_throw_exception_when_url_is_valid(String repositoryUrl) {
        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validateLocation(repositoryUrl));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://my-repo.com.git", "http://my-repo.com.git", "git://host.xz/~user/path/to/repo.git", "git@github.com:my/repo.git",
            "https://host.xz/path/to/repo.git/", "http://host.xz/path/to/repo.git/", "git://host.xz/path/to/repo.git/", "git@host.com:my-repo/example.git" })
    void validateLocation_does_not_throw_exception_when_git_pattern_is_configured(String location) {
        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validateLocation(location));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://my-repo.com.notAgit_repo", "http://my-repo.com.git./bin/bash", "git://host.xz/~user/path/to/repo.gitsomeevalexecution",
            "git@github.com:my/repo.git\nexecuteMalicious" })
    void validateLocation_throws_exception_when_invalid_git_location_is_configured(String location) {
        /* execute */
        PrepareWrapperInputValidatorException e = assertThrows(PrepareWrapperInputValidatorException.class,
                () -> gitInputValidatorToTest.validateLocation(location));

        /* test */
        assertEquals(LOCATION_NOT_MATCHING_PATTERN, e.getExitCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { "user", "user-name", "user_name", "user-name-123", "user-name-123-456", "user-name_23" })
    void validateUsername_does_not_throw_exception_when_username_is_valid(String username) {
        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validateUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = { "user name", "user name 123", "./bin/bash" })
    void validateUsername_throws_exception_when_username_is_invalid(String username) {
        /* execute + test */
        PrepareWrapperInputValidatorException exception = assertThrows(PrepareWrapperInputValidatorException.class,
                () -> gitInputValidatorToTest.validateUsername(username));
        assertEquals("Defined username must match the git pattern.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "ghp_123456789012345678901234567890123456", "ghs_123456789012345678901234567890123456",
            "github_pat_1234567890123456789012_1234567890123456789012345678901234567890123456789012example" })
    void validatePassword_does_not_throw_exception_when_password_is_valid(String password) {
        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validatePassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = { "./bin/bash", "ghp_1234567890123456789012345678901234567", "ghs_1234567890123456789012345678901234567",
            "github_pat_123456789012345678901234567890123456_;echo 'malicious'" })
    void validatePassword_throws_exception_when_password_is_invalid(String password) {
        /* execute + test */
        PrepareWrapperInputValidatorException exception = assertThrows(PrepareWrapperInputValidatorException.class,
                () -> gitInputValidatorToTest.validatePassword(password));
        assertEquals("Defined password must match the git Api token pattern.", exception.getMessage());
    }

    @Test
    void validate_throws_exception_when_credentials_are_empty() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("https://example.com/my-repo.git");
        remoteDataConfiguration.setType("git");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> gitInputValidatorToTest.validate(context));

        /* test */
        assertEquals("Defined credentials must not be null.", exception.getMessage());
    }

    @Test
    void validate_throws_exception_when_no_username_found() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setPassword("password");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("https://example.com/my-repo.git");
        remoteDataConfiguration.setType("git");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> gitInputValidatorToTest.validate(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined username must not be null or empty."));
    }

    @Test
    void validate_throws_exception_when_no_password_found() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("https://example.com/my-repo.git");
        remoteDataConfiguration.setType("git");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> gitInputValidatorToTest.validate(context));

        /* test */
        assertEquals("Defined password must not be null or empty.", exception.getMessage());

    }

    @Test
    void validate_successful_when_user_credentials_are_configured_correctly() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = "test";
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
        remoteDataConfiguration.setLocation("https://example.com/my-repo.git");
        remoteDataConfiguration.setType("git");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validate(context));
    }

    @Test
    void validate_successful_when_no_credentials_are_configured() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = "test";
        writer.save(new File(tempDir, filename), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("https://example.com/my-repo.git");
        remoteDataConfiguration.setType("git");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validate(context));

    }

    private PrepareWrapperContext createContextEmptyConfig() {
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn("test-upload-folder");
        return new PrepareWrapperContext(createFromJSON("{}"), environment);
    }

}