package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class SkopeoInputValidator implements InputValidator {

    private static final String SKOPEO_LOCATION_REGEX = "((docker://|https://)?([a-zA-Z0-9-_.].[a-zA-Z0-9-_.]/)?[a-zA-Z0-9-_.]+(:[a-zA-Z0-9-_.]+)?(/)?)+(@sha256:[a-f0-9]{64})?";
    private static final Pattern SKOPEO_LOCATION_PATTERN = Pattern.compile(SKOPEO_LOCATION_REGEX);
    private static final String SKOPEO_USERNAME_REGEX = "^[a-zA-Z0-9-_\\d](?:[a-zA-Z0-9-_\\d]|(?=[a-zA-Z0-9-_\\d])){0,38}$";
    private static final Pattern SKOPEO_USERNAME_PATTERN = Pattern.compile(SKOPEO_USERNAME_REGEX);
    private static final String SKOPEO_PASSWORD_REGEX = "^[a-zA-Z0-9-_\\d]{0,72}$";
    private static final Pattern SKOPEO_PASSWORD_PATTERN = Pattern.compile(SKOPEO_PASSWORD_REGEX);
    private final List<String> defaultForbiddenCharacters = Arrays.asList(">", "<", "!", "?", "*", "'", "\"", ";", "&", "|", "`", "$", "{", "}");

    @Override
    public boolean validateLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Defined location must not be null or empty.");
        }
        return SKOPEO_LOCATION_PATTERN.matcher(location).matches();
    }

    @Override
    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Defined username must not be null or empty.");
        }
        if (!SKOPEO_USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalStateException("Defined username must match the modules pattern.");
        }
    }

    @Override
    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Defined password must not be null or empty.");
        }
        if (!SKOPEO_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalStateException("Defined password must match the Skopeo Api token pattern.");
        }
    }

    @Override
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
}
