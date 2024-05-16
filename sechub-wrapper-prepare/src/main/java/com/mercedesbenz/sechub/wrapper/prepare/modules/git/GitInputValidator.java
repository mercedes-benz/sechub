package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.mercedesbenz.sechub.wrapper.prepare.modules.InputValidator;
import org.springframework.stereotype.Component;

@Component
public class GitInputValidator implements InputValidator {
    private static final String GIT_LOCATION_REGEX = "((git|ssh|http(s)?)|(git@[\\w\\.]+))(:(//)?)([\\w\\.@\\:/\\-~]+)(\\.git)(/)?$";
    private static final Pattern GIT_LOCATION_PATTERN = Pattern.compile(GIT_LOCATION_REGEX);

    private static final String GIT_USERNAME_REGEX = "^[a-zA-Z0-9-_\\d](?:[a-zA-Z0-9-_\\d]|(?=[a-zA-Z0-9-_\\d])){0,38}$";
    private static final Pattern GIT_USERNAME_PATTERN = Pattern.compile(GIT_USERNAME_REGEX);

    private static final String GIT_PASSWORD_REGEX = "^(gh[ps]_[a-zA-Z0-9]{36}|github_pat_[a-zA-Z0-9]{22}_[a-zA-Z0-9]{59})$";
    private static final Pattern GIT_PASSWORD_PATTERN = Pattern.compile(GIT_PASSWORD_REGEX);

    private final List<String> defaultForbiddenCharacters = Arrays.asList(">", "<", "!", "?", "*", "'", "\"", ";", "&", "|", "`", "$", "{", "}");

    public void validateLocationCharacters(String url, List<String> forbiddenCharacters) {
        if (forbiddenCharacters == null) {
            forbiddenCharacters = defaultForbiddenCharacters;
        }
        if (url.contains(" ")) {
            throw new IllegalArgumentException("Defined URL must not contain whitespaces.");
        }
        for (String forbiddenCharacter : forbiddenCharacters) {
            if (url.contains(forbiddenCharacter)) {
                throw new IllegalArgumentException("Defined URL must not contain forbidden characters: " + forbiddenCharacter);
            }
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Defined username must not be null or empty.");
        }

        if (!GIT_USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalStateException("Defined username must match the modules pattern.");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Defined password must not be null or empty.");
        }

        if (!GIT_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalStateException("Defined password must match the Git Api token pattern.");
        }
    }

    public boolean validateLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Defined location must not be null or empty.");
        }
        return GIT_LOCATION_PATTERN.matcher(location).matches();
    }
}
