// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.wrapper.prepare.InputValidatorExitcode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperUsageException;

class GitInputValidatorTest {

    private static final String VALID_USERNAME = "user1";
    private static final String VALID_PWD = "ghp_123456789012345678901234567890123456";
    private static final String VALID_REPO_URL = "https://my-repo.com.git";

    private GitPrepareInputValidator gitInputValidatorToTest;
    private PrepareWrapperContext context;
    private SecHubRemoteDataConfiguration remoteDataConfiguration;

    @BeforeEach
    void beforeEach() {
        gitInputValidatorToTest = new GitPrepareInputValidator();

        gitInputValidatorToTest.logSanitizer = mock(PDSLogSanitizer.class);

        context = mock(PrepareWrapperContext.class);
        remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        when(context.getRemoteDataConfiguration()).thenReturn(remoteDataConfiguration);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com;echoMalicious", "https://example.com.git>text.txt", "https://example.com/some-git-repo.git&&cd.." })
    void validate_throws_exception_when_url_does_contain_forbidden_characters(String repositoryUrl) {

        /* prepare */
        initRemoteDataWithOutCredentials(repositoryUrl);

        /* execute */
        PrepareWrapperUsageException exception = assertThrows(PrepareWrapperUsageException.class, () -> gitInputValidatorToTest.validate(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined location URL must not contain forbidden characters: "));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://y-git-host/my-git-user/my-git-repo.git", "https://example.org/some-git-repo.git", "git@gitrepo:my-repo.git" })
    void validate_does_not_throw_exception_when_url_is_valid_and_no_credentials(String repositoryUrl) {
        /* prepare */
        initRemoteDataWithOutCredentials(repositoryUrl);

        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validate(context));
    }

    @ParameterizedTest
    @ValueSource(strings = { VALID_REPO_URL, "http://my-repo.com.git", "git://host.xz/~user/path/to/repo.git", "git@github.com:my/repo.git",
            "https://host.xz/path/to/repo.git/", "http://host.xz/path/to/repo.git/", "git://host.xz/path/to/repo.git/", "git@host.com:my-repo/example.git" })
    void validate_does_not_throw_exception_when_git_pattern_is_configured_and_no_credentials(String repositoryUrl) {
        /* prepare */
        initRemoteDataWithOutCredentials(repositoryUrl);
        remoteDataConfiguration.setType("git");

        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validate(context));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://my-repo.com.notAgit_repo", "http://my-repo.com.git./bin/bash", "git://host.xz/~user/path/to/repo.gitsomeevalexecution",
            "git@github.com:my/repo.git\nexecuteMalicious" })
    void validate_throws_exception_when_invalid_git_location_is_configured(String repositoryUrl) {
        /* prepare */
        initRemoteDataWithOutCredentials(repositoryUrl);

        /* execute */
        PrepareWrapperInputValidatorException e = assertThrows(PrepareWrapperInputValidatorException.class, () -> gitInputValidatorToTest.validate(context));

        /* test */
        assertEquals(LOCATION_NOT_MATCHING_PATTERN, e.getExitCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { "user", "user-name", "user_name", "user-name-123", "user-name-123-456", "user-name_23" })
    void validate_does_not_throw_exception_when_username_is_valid(String username) {
        /* prepare */
        initRemoteDataWithCredentials(VALID_REPO_URL, username, VALID_PWD);

        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validate(context));
    }

    @ParameterizedTest
    @ValueSource(strings = { "user name", "user name 123", "./bin/bash" })
    void validate_throws_exception_when_username_is_invalid(String username) {
        /* prepare */
        initRemoteDataWithCredentials(VALID_REPO_URL, username, VALID_PWD);

        /* execute + test */
        PrepareWrapperInputValidatorException exception = assertThrows(PrepareWrapperInputValidatorException.class,
                () -> gitInputValidatorToTest.validate(context));
        assertEquals("Defined username must match the git pattern.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "ghp_123456789012345678901234567890123456", "ghs_123456789012345678901234567890123456",
            "github_pat_1234567890123456789012_1234567890123456789012345678901234567890123456789012example" })
    void validate_does_not_throw_exception_when_password_is_valid(String password) {
        /* prepare */
        initRemoteDataWithCredentials(VALID_REPO_URL, VALID_USERNAME, password);

        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validate(context));
    }

    @ParameterizedTest
    @ValueSource(strings = { "./bin/bash", "ghp_1234567890123456789012345678901234567", "ghs_1234567890123456789012345678901234567",
            "github_pat_123456789012345678901234567890123456_;echo 'malicious'" })
    void validate_throws_exception_when_password_is_invalid(String password) {
        /* prepare */
        initRemoteDataWithCredentials(VALID_REPO_URL, VALID_USERNAME, password);

        /* execute + test */
        PrepareWrapperInputValidatorException exception = assertThrows(PrepareWrapperInputValidatorException.class,
                () -> gitInputValidatorToTest.validate(context));
        assertEquals("Defined password must match the git Api token pattern.", exception.getMessage());
    }

    @Test
    void validate_throws_exception_when_credentials_not_null_but_user_is_null() {
        /* prepare */
        initRemoteDataWithCredentials(VALID_REPO_URL, "", "");
        remoteDataConfiguration.getCredentials().get().setUser(null); // set user to null

        /* execute */
        PrepareWrapperUsageException exception = assertThrows(PrepareWrapperUsageException.class, () -> gitInputValidatorToTest.validate(context));

        /* test */
        assertEquals("Defined credentials must contain credential user and can not be empty.", exception.getMessage());
    }

    @Test
    void validate_throws_exception_when_no_username_found() {
        /* prepare */
        initRemoteDataWithCredentials(VALID_REPO_URL, null, "pwd1");

        /* execute */
        PrepareWrapperUsageException exception = assertThrows(PrepareWrapperUsageException.class, () -> gitInputValidatorToTest.validate(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined username must not be null or empty."));
    }

    @Test
    void validate_throws_exception_when_no_password_found() {
        /* prepare */
        initRemoteDataWithCredentials(VALID_REPO_URL, "example-name", null);

        /* execute */
        PrepareWrapperUsageException exception = assertThrows(PrepareWrapperUsageException.class, () -> gitInputValidatorToTest.validate(context));

        /* test */
        assertEquals("Defined password must not be null or empty. Password is required for login.", exception.getMessage());

    }

    @Test
    void validate_successful_when_user_credentials_are_configured_correctly() throws IOException {
        /* prepare */
        /* prepare */
        initRemoteDataWithCredentials(VALID_REPO_URL, VALID_USERNAME, VALID_PWD);
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validate(context));
    }

    private void initRemoteDataWithOutCredentials(String location) {
        remoteDataConfiguration.setLocation(location);
    }

    private void initRemoteDataWithCredentials(String location, String username, String pwd) {
        remoteDataConfiguration.setLocation(location);
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName(username);
        user.setPassword(pwd);

        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
    }

}