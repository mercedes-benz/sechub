package com.mercedesbenz.sechub.wrapper.prepare;

import static com.mercedesbenz.sechub.wrapper.prepare.InputValidatorExitcode.*;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;

public class PrepareInputValidationSupport {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareInputValidationSupport.class);
    private String type;

    private Pattern locationPattern;
    private Pattern usernamePattern;
    private Pattern passwordPattern;

    private final List<String> forbiddenCharacters = Collections
            .unmodifiableList(Arrays.asList(">", "<", "!", "?", "*", "'", "\"", ";", "&", "|", "`", "$", "{", "}"));

    private LogSanitizerProvider logSanitizerProvider;

    public static InputValidationSupportBuilder builder() {
        return new InputValidationSupportBuilder();
    }

    private static boolean isTypeNullOrEmpty(String type) {
        return type == null || type.isEmpty();
    }

    private static void assertPatternNotNull(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern must not be null.");
        }
    }

    public void validate(PrepareWrapperContext context) throws PrepareWrapperInputValidatorException {
        validateModuleData(context);
        validateCredentials(context);
    }

    private void validateModuleData(PrepareWrapperContext context) throws PrepareWrapperInputValidatorException {
        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();
        String location = secHubRemoteDataConfiguration.getLocation();
        String type = secHubRemoteDataConfiguration.getType();

        if (isTypeNullOrEmpty(type)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No type was defined for location {}.", logSanitizerProvider.getLogSanitizer().sanitize(location, 1024));
            }
            validateLocation(location);
            return;
        } else if (isTypeMatching(type)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Type is matching type {}. Location is: {}", type, logSanitizerProvider.getLogSanitizer().sanitize(location, 1024));
            }
            validateLocation(location);
            return;
        }
        throw new PrepareWrapperInputValidatorException("Defined type " + type + " was not modules type " + type + ".", TYPE_NOT_MATCHING_PATTERN);
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

    private void validateUsername(String username) throws PrepareWrapperInputValidatorException {
        if (isLocationUndefined(username)) {
            throw new PrepareWrapperUsageException("Defined username must not be null or empty. Username is required for login.",
                    CREDENTIAL_USER_NAME_NOT_DEFINED);
        }

        if (!usernamePattern.matcher(username).matches()) {
            throw new PrepareWrapperInputValidatorException("Defined username must match the " + type + " pattern.", CREDENTIALS_USERNAME_NOT_MATCHING_PATTERN);
        }
    }

    private void validatePassword(String password) throws PrepareWrapperInputValidatorException {
        if (isLocationUndefined(password)) {
            throw new PrepareWrapperUsageException("Defined password must not be null or empty. Password is required for login.",
                    CREDENTIAL_USER_PASSWORD_NOT_DEFINED);
        }

        if (!passwordPattern.matcher(password).matches()) {
            throw new PrepareWrapperInputValidatorException("Defined password must match the " + type + " Api token pattern.",
                    CREDENTIALS_PASSWORD_NOT_MATCHING_PATTERN);
        }
    }

    private void validateLocation(String location) throws PrepareWrapperInputValidatorException {
        if (isLocationUndefined(location)) {
            throw new PrepareWrapperUsageException("Defined location must not be null or empty. Location is required for download remote data.",
                    LOCATION_NOT_DEFINED);
        }
        validateLocationCharacters(location);
        if (!isLocationPatternMatching(location)) {
            throw new PrepareWrapperInputValidatorException("Defined location must match the " + type + " pattern.", LOCATION_NOT_MATCHING_PATTERN);
        }
    }

    private boolean isLocationPatternMatching(String location) {
        return locationPattern.matcher(location).matches();
    }

    private boolean isLocationUndefined(String location) {
        return location == null || location.isBlank();
    }

    private boolean isTypeMatching(String type) {
        return type.equalsIgnoreCase(this.type);
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

    public static class InputValidationSupportBuilder {
        private String type;

        private Pattern locationPattern;
        private Pattern usernamePattern;
        private Pattern passwordPattern;

        private LogSanitizerProvider logSanitizerProvider;

        private InputValidationSupportBuilder() {

        }

        public InputValidationSupportBuilder setLocationPattern(Pattern locationPattern) {
            this.locationPattern = locationPattern;
            return this;
        }

        public InputValidationSupportBuilder setUserNamePattern(Pattern usernamePattern) {
            this.usernamePattern = usernamePattern;
            return this;
        }

        public InputValidationSupportBuilder setPasswordPattern(Pattern passwordPattern) {
            this.passwordPattern = passwordPattern;
            return this;
        }

        public InputValidationSupportBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public InputValidationSupportBuilder setLogSanitizerProvider(LogSanitizerProvider logSanitizerProvider) {
            this.logSanitizerProvider = logSanitizerProvider;
            return this;
        }

        public PrepareInputValidationSupport build() {
            assertPatternNotNull(locationPattern);
            assertPatternNotNull(usernamePattern);
            assertPatternNotNull(passwordPattern);

            if (isTypeNullOrEmpty(type)) {
                throw new IllegalArgumentException("Type must not be null or empty.");
            }

            if (logSanitizerProvider == null) {
                throw new IllegalArgumentException("Log sanitizer provider not defined");
            }

            if (type == null) {
                type = "";
            }
            PrepareInputValidationSupport result = new PrepareInputValidationSupport();

            result.type = type.trim();
            result.locationPattern = locationPattern;
            result.usernamePattern = usernamePattern;
            result.passwordPattern = passwordPattern;
            result.logSanitizerProvider = logSanitizerProvider;

            return result;
        }
    }

    public boolean isAccepting(PrepareWrapperContext context) {

        SecHubRemoteDataConfiguration secHubRemoteDataConfiguration = context.getRemoteDataConfiguration();
        if (secHubRemoteDataConfiguration == null) {
            return false;
        }

        String type = secHubRemoteDataConfiguration.getType();

        if (isTypeNullOrEmpty(type)) {
            /* no explicit type set - we must check by location */
            String location = secHubRemoteDataConfiguration.getLocation();
            if (isLocationUndefined(location)) {
                return false;
            }

            return isLocationPatternMatching(location);
        } else {
            return isTypeMatching(type);
        }
    }

}
