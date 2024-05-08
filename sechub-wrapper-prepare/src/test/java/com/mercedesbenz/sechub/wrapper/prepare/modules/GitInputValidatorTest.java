package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GitInputValidatorTest {

    GitInputValidator gitInputValidatorToTest;

    PrepareWrapperModuleGit gitModule;

    @BeforeEach
    void beforeEach() {
        gitInputValidatorToTest = new GitInputValidator();
        gitModule = new PrepareWrapperModuleGit();
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com;echoMalicious", "https://example.com.git>text.txt", "https://example.com/some-git-repo.git&&cd.." })
    void validateURL_throws_exception_when_url_does_contain_forbidden_characters(String repositoryUrl) {
        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gitInputValidatorToTest.validateLocationCharacters(repositoryUrl, null));

        /* test */
        assertTrue(exception.getMessage().contains("Defined URL must not contain forbidden characters: "));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://y-git-host/my-git-user/my-git-repo.git", "https://example.org/some-git-repo.git", "git@gitrepo:my-repo" })
    void validateURL_does_not_throw_exception_when_url_is_valid(String repositoryUrl) {
        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validateLocationCharacters(repositoryUrl, null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://my-repo.com.git", "http://my-repo.com.git", "git://host.xz/~user/path/to/repo.git", "git@github.com:my/repo.git",
            "https://host.xz/path/to/repo.git/", "http://host.xz/path/to/repo.git/", "git://host.xz/path/to/repo.git/", "git@host.com:my-repo/example.git" })
    void escapeLocation_returns_true_when_git_pattern_is_configured(String location) {
        /* execute */
        boolean result = gitInputValidatorToTest.validateLocation(location);

        /* test */
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://my-repo.com.notAgit_repo", "http://my-repo.com.git;./bin/bash",
            "git://host.xz/~user/path/to/repo.git some eval execution", "git@github.com:my/repo.git\nexecuteMalicious" })
    void escapeLocation__returns_false_when_invalid_git_pattern_is_configured(String location) {
        /* execute */
        boolean result = gitInputValidatorToTest.validateLocation(location);

        /* test */
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "user", "user-name", "user_name", "user-name-123", "user-name-123-456", "user-name_23" })
    void escapeUsername_does_not_throw_exception_when_username_is_valid(String username) {
        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validateUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = { "user name", "user name 123", "./bin/bash" })
    void escapeUsername_throws_exception_when_username_is_invalid(String username) {
        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> gitInputValidatorToTest.validateUsername(username));
        assertEquals("Defined username must match the modules pattern.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "ghp_123456789012345678901234567890123456", "ghs_123456789012345678901234567890123456",
            "github_pat_1234567890123456789012_1234567890123456789012345678901234567890123456789012example" })
    void escapePassword_does_not_throw_exception_when_password_is_valid(String password) {
        /* execute + test */
        assertDoesNotThrow(() -> gitInputValidatorToTest.validatePassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = { "./bin/bash", "ghp_1234567890123456789012345678901234567", "ghs_1234567890123456789012345678901234567",
            "github_pat_123456789012345678901234567890123456_;echo 'malicious'" })
    void escapePassword_throws_exception_when_password_is_invalid(String password) {
        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> gitInputValidatorToTest.validatePassword(password));
        assertEquals("Defined password must match the Git Api token pattern.", exception.getMessage());
    }

}