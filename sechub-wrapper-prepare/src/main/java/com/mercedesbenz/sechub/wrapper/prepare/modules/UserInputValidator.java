package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class UserInputValidator {

    public void validateUsername(String username, Pattern pattern) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Defined username must not be null or empty.");
        }

        if (!pattern.matcher(username).matches()) {
            throw new IllegalStateException("Defined username must match the modules pattern.");
        }
    }

    public void validatePassword(String password, Pattern pattern) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Defined password must not be null or empty.");
        }

        if (!pattern.matcher(password).matches()) {
            throw new IllegalStateException("Defined password must match the modules pattern.");
        }
    }

    public boolean validateLocation(String location, Pattern pattern) {
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Defined location must not be null or empty.");
        }
        return pattern.matcher(location).matches();
    }

    public void validateLocationURL(String url, List<String> forbiddenCharacters) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Defined URL must not be null or empty.");
        }
        if (!url.startsWith("https://")) {
            throw new IllegalArgumentException("Defined URL must start with https://.");
        }
        if (url.contains(" ")) {
            throw new IllegalArgumentException("Defined URL must not contain whitespaces.");
        }
        for (String forbiddenCharacter : forbiddenCharacters) {
            if (url.contains(forbiddenCharacter)) {
                throw new IllegalArgumentException("Defined URL must not contain forbidden characters: " + forbiddenCharacter);
            }
        }

        try {
            new java.net.URL(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Defined URL is not valid.");
        }
    }
}
