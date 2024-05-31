// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.wrapper.prepare.modules.InputValidatorExitcode.*;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

public class AbstractInputValidator implements InputValidator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractInputValidator.class);
    private final String TYPE;
    private final Pattern LOCATION_PATTERN;
    private final Pattern USERNAME_PATTERN;
    private final Pattern PASSWORD_PATTERN;
    private final List<String> forbiddenCharacters = Collections
            .unmodifiableList(Arrays.asList(">", "<", "!", "?", "*", "'", "\"", ";", "&", "|", "`", "$", "{", "}"));

    @Autowired
    public PDSLogSanitizer pdsLogSanitizer;

    public AbstractInputValidator(String type, Pattern locationPattern, Pattern usernamePattern, Pattern passwordPattern) {
        assertPatternNotNull(locationPattern);
        assertPatternNotNull(usernamePattern);
        assertPatternNotNull(passwordPattern);
        if (isTypeNullOrEmpty(type)) {
            throw new IllegalArgumentException("Type must not be null or empty.");
        }

        this.TYPE = type;
        this.LOCATION_PATTERN = locationPattern;
        this.USERNAME_PATTERN = usernamePattern;
        this.PASSWORD_PATTERN = passwordPattern;
    }

    public void validate(PrepareWrapperContext context) throws PrepareWrapperInputValidatorException {
        validateModule(context);
        validateCredentials(context);
    }

    private void validateModule(PrepareWrapperContext context) throws PrepareWrapperInputValidatorException {
        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();
        String location = secHubRemoteDataConfiguration.getLocation();
        String type = secHubRemoteDataConfiguration.getType();

        if (isTypeNullOrEmpty(type)) {
            LOG.debug("No type was defined for location {}.", pdsLogSanitizer.sanitize(location, 1024));
            validateLocation(location);
            return;
        } else if (isMatchingType(type)) {
            LOG.debug("Type is matching type {}. Location is: {}", TYPE, pdsLogSanitizer.sanitize(location, 1024));
            validateLocation(location);
            return;
        }
        throw new PrepareWrapperInputValidatorException("Defined type " + type + " was not modules type " + TYPE + ".", TYPE_NOT_MATCHING_PATTERN);
    }

    private void validateCredentials(PrepareWrapperContext context) throws PrepareWrapperInputValidatorException {
        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();

        if (secHubRemoteDataConfiguration.getCredentials().isPresent()) {
            SecHubRemoteCredentialConfiguration remoteCredentialConfiguration = secHubRemoteDataConfiguration.getCredentials().get();
            if (remoteCredentialConfiguration.getUser().isPresent()) {
                SecHubRemoteCredentialUserData user = remoteCredentialConfiguration.getUser().get();
                validateUsername(user.getName());
                validatePassword(user.getPassword());
                return;
            }
            // credentials object was empty
            throw new PrepareWrapperUsageException("Defined credentials must contain credential user and can not be empty.", CREDENTIALS_NOT_DEFINED);
        }
    }

    public void validateUsername(String username) throws PrepareWrapperInputValidatorException {
        if (username == null || username.isBlank()) {
            throw new PrepareWrapperUsageException("Defined username must not be null or empty. Username is required for login.",
                    CREDENTIAL_USER_NAME_NOT_DEFINED);
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new PrepareWrapperInputValidatorException("Defined username must match the " + TYPE + " pattern.", CREDENTIALS_USERNAME_NOT_MATCHING_PATTERN);
        }
    }

    public void validatePassword(String password) throws PrepareWrapperInputValidatorException {
        if (password == null || password.isBlank()) {
            throw new PrepareWrapperUsageException("Defined password must not be null or empty. Password is required for login.",
                    CREDENTIAL_USER_PASSWORD_NOT_DEFINED);
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new PrepareWrapperInputValidatorException("Defined password must match the " + TYPE + " Api token pattern.",
                    CREDENTIALS_PASSWORD_NOT_MATCHING_PATTERN);
        }
    }

    public void validateLocation(String location) throws PrepareWrapperInputValidatorException {
        if (location == null || location.isBlank()) {
            throw new PrepareWrapperUsageException("Defined location must not be null or empty. Location is required for download remote data.",
                    LOCATION_NOT_DEFINED);
        }
        validateLocationCharacters(location);
        if (!LOCATION_PATTERN.matcher(location).matches()) {
            throw new PrepareWrapperInputValidatorException("Defined location must match the " + TYPE + " pattern.", LOCATION_NOT_MATCHING_PATTERN);
        }
    }

    private boolean isTypeNullOrEmpty(String type) {
        return type == null || type.isEmpty();
    }

    private boolean isMatchingType(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

    private void validateLocationCharacters(String url) {
        if (url.contains(" ")) {
            throw new PrepareWrapperUsageException("Defined location URL must not contain whitespaces.", LOCATION_CONTAINS_FORBIDDEN_CHARACTER);
        }
        for (String forbiddenCharacter : forbiddenCharacters) {
            if (url.contains(forbiddenCharacter)) {
                throw new PrepareWrapperUsageException("Defined location URL must not contain forbidden characters: " + forbiddenCharacter,
                        LOCATION_CONTAINS_FORBIDDEN_CHARACTER);
            }
        }
    }

    private void assertPatternNotNull(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern must not be null.");
        }
    }
}
