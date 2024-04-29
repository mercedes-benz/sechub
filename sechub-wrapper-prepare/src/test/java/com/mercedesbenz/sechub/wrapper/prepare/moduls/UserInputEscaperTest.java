package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserInputEscaperTest {

    UserInputEscaper escaperToTest;

    PrepareWrapperModuleGit gitModule;

    private Pattern locationPattern;
    private Pattern usernamePattern;
    private Pattern passwordPattern;

    List<String> forbiddenCharacters;

    @BeforeEach
    void beforeEach() {
        escaperToTest = new UserInputEscaper();
        gitModule = new PrepareWrapperModuleGit();
        forbiddenCharacters = new ArrayList<>();
    }

    @Test
    void validateLocationURL_throws_exception_when_url_is_null() {
        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> escaperToTest.escapeLocationURL(null, forbiddenCharacters));

        /* test */
        assertEquals("Defined URL must not be null or empty.", exception.getMessage());
    }

    @Test
    void validateLocationURL_throws_exception_when_url_is_empty() {
        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> escaperToTest.escapeLocationURL("", forbiddenCharacters));

        /* test */
        assertEquals("Defined URL must not be null or empty.", exception.getMessage());
    }

    @Test
    void validateURL_throws_exception_when_url_does_not_start_with_https() {
        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> escaperToTest.escapeLocationURL("http://example.com", forbiddenCharacters));

        /* test */
        assertEquals("Defined URL must start with https://.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://example.com;echoMalicious", "https://example.com.git>text.txt", "https://example.com/some-git-repo.git&&cd.." })
    void validateURL_throws_exception_when_url_does_contain_forbidden_characters(String repositoryUrl) {
        /* prepare */
        forbiddenCharacters.add(";");
        forbiddenCharacters.add("&");
        forbiddenCharacters.add("|");
        forbiddenCharacters.add(">");
        forbiddenCharacters.add("<");
        forbiddenCharacters.add("!");
        forbiddenCharacters.add("?");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> escaperToTest.escapeLocationURL(repositoryUrl, forbiddenCharacters));

        /* test */
        assertTrue(exception.getMessage().contains("Defined URL must not contain forbidden characters: "));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://$USERNAME:$PASSWORD@example.com.git", "https://$USERNAME:$PASSWORD@my-git-host/my-git-user/my-git-repo.git",
            "https://example.org/some-git-repo.git" })
    void validateURL_does_not_throw_exception_when_url_is_valid(String repositoryUrl) {
        /* execute + test */
        escaperToTest.escapeLocationURL(repositoryUrl, forbiddenCharacters);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://my-repo.com.git", "http://my-repo.com.git", "git://host.xz/~user/path/to/repo.git", "git@github.com:my/repo.git",
            "https://host.xz/path/to/repo.git/", "http://host.xz/path/to/repo.git/", "git://host.xz/path/to/repo.git/", "git@host.com:my-repo/example.git" })
    void escapeLocation_returns_true_when_git_pattern_is_configured(String location) {
        /* prepare */
        setUpForGit();

        /* execute */
        boolean result = escaperToTest.escapeLocation(location, locationPattern);

        /* test */
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://my-repo.com.notAgit_repo", "http://my-repo.com.git;./bin/bash",
            "git://host.xz/~user/path/to/repo.git some eval execution", "git@github.com:my/repo.git\nexecuteMalicious" })
    void escapeLocation__returns_false_when_invalid_git_pattern_is_configured(String location) {
        /* prepare */
        setUpForGit();

        /* execute */
        boolean result = escaperToTest.escapeLocation(location, locationPattern);

        /* test */
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "user", "user-name", "user_name", "user-name-123", "user-name-123-456", "user-name_23" })
    void escapeUsername_does_not_throw_exception_when_username_is_valid(String username) {
        /* prepare */
        setUpForGit();

        /* execute + test */
        escaperToTest.escapeUsername(username, usernamePattern);
    }

    @ParameterizedTest
    @ValueSource(strings = { "user name", "user name 123", "./bin/bash" })
    void escapeUsername_throws_exception_when_username_is_invalid(String username) {
        /* prepare */
        setUpForGit();

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> escaperToTest.escapeUsername(username, usernamePattern));
        assertEquals("Defined username must match the modules pattern.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "ghp_123456789012345678901234567890123456", "ghs_123456789012345678901234567890123456",
            "github_pat_1234567890123456789012_1234567890123456789012345678901234567890123456789012example" })
    void escapePassword_does_not_throw_exception_when_password_is_valid(String password) {
        /* prepare */
        setUpForGit();

        /* execute + test */
        escaperToTest.escapePassword(password, passwordPattern);
    }

    @ParameterizedTest
    @ValueSource(strings = { "./bin/bash", "ghp_1234567890123456789012345678901234567", "ghs_1234567890123456789012345678901234567",
            "github_pat_123456789012345678901234567890123456_;echo 'malicious'" })
    void escapePassword_throws_exception_when_password_is_invalid(String password) {
        /* prepare */
        setUpForGit();

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> escaperToTest.escapePassword(password, passwordPattern));
        assertEquals("Defined password must match the modules pattern.", exception.getMessage());
    }

    private void setUpForGit() {
        locationPattern = Pattern.compile(gitModule.getGitLocationPattern());
        usernamePattern = Pattern.compile(gitModule.getGitUsernamePattern());
        passwordPattern = Pattern.compile(gitModule.getGitPasswordPattern());
    }
}