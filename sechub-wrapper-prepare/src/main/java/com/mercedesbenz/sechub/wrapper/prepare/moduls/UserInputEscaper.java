package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class UserInputEscaper {

    public void escapeUsername(String username, Pattern pattern) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Defined username must not be null or empty.");
        }

        if (!pattern.matcher(username).matches()) {
            throw new IllegalStateException("Defined username must match the modules pattern.");
        }
    }

    public void escapePassword(String password, Pattern pattern) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Defined password must not be null or empty.");
        }

        if (!pattern.matcher(password).matches()) {
            throw new IllegalStateException("Defined password must match the modules pattern.");
        }
    }

    public boolean escapeLocation(String location, Pattern pattern) {
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Defined location must not be null or empty.");
        }
        return pattern.matcher(location).matches();
    }

    public void escapeLocationURL(String url, List<String> forbiddenCharacters) {
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
            new java.net.URL(url).toURI();
        } catch (Exception e) {
            throw new IllegalArgumentException("Defined URL is not valid.");
        }
    }
}
