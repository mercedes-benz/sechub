// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperUsageException;

class SkopeoInputValidatorTest {

    private static final String VALID_PWD = "ghp_exampleAPITOKEN8ffne3l6g9f393r8fbcsf";
    private static final String VALID_DOCKER_URL = "my-example-location";
    private static final String VALID_USERNAME = "my-example-name";
    private SkopeoPrepareInputValidator validatorToTest;
    private PrepareWrapperContext context;
    private SecHubRemoteDataConfiguration remoteDataConfiguration;

    @BeforeEach
    void beforeEach() {
        validatorToTest = new SkopeoPrepareInputValidator();

        validatorToTest.logSanitizer = mock(PDSLogSanitizer.class);

        context = mock(PrepareWrapperContext.class);
        remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        when(context.getRemoteDataConfiguration()).thenReturn(remoteDataConfiguration);
    }

    @ParameterizedTest
    @ValueSource(strings = { "ubuntu:22.04", "ubuntu", "docker://ubuntu:22.04", "docker://ubuntu", "oci:busybox_ocilayout:latest", "https://hub.docker.com",
            "docker://docker.io/library/busybox:latest", "ubuntu@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d",
            "ghcr.io/owner/repo:tag" })
    void validate_throws_no_exception_for_valid_docker_urls(String dockerUrl) {
        /* prepare */
        initRemoteDataWithOutCredentials(dockerUrl);

        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validate(context));
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid-registry ubuntu:22.04", "docker://registry/ubuntu:invalid tag", "docker://ubuntu:tag$maliciousCode",
            "docker://ubuntu:tag|maliciousCode", "my-registry/oci:busybox_ocilayout;latest", })
    void validate_throws_usage_exception_for_invalid_docker_urls(String dockerUrl) {
        /* prepare */
        initRemoteDataWithOutCredentials(dockerUrl);

        /* execute + test */
        assertThrows(PrepareWrapperUsageException.class, () -> validatorToTest.validate(context));
    }

    @ParameterizedTest
    @ValueSource(strings = { "username", "username123", "username_123", "username-123", "username-123_456", "username1234567890123456789003890123456",
            "user_user_user" })
    void validate_does_not_throw_exception_for_valid_usernames(String username) {

        /* prepare */
        initRemoteDataWithCredentials(VALID_DOCKER_URL, username, VALID_PWD);

        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validate(context));
    }

    @ParameterizedTest
    @ValueSource(strings = { "user name", "username?", "username!", "username>", "username<", "username'", "username\"", "username;", "username&", "username|",
            "username`", "username$", "username{", "username}" })
    void validate_throws_exception_for_invalid_usernames(String username) {
        /* prepare */
        initRemoteDataWithCredentials(VALID_DOCKER_URL, username, VALID_PWD);

        /* execute */
        PrepareWrapperInputValidatorException exception = assertThrows(PrepareWrapperInputValidatorException.class, () -> validatorToTest.validate(context));

        /* test */
        assertEquals("Defined username must match the docker pattern.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "password", "password123", "password_123", "password-123", "password-123_456", "password1234567890123456789003890123456",
            "dXNlckBleGFtcGxlLmNvbTpzZWNyZXQexample", "Z2hjcl9wczpzc2VjcmV0example" })
    void validate_does_not_throw_exception_for_valid_passwords(String password) {
        /* prepare */
        initRemoteDataWithCredentials(VALID_DOCKER_URL, VALID_USERNAME, password);

        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validate(context));
    }

    @ParameterizedTest
    @ValueSource(strings = { "password?", "password!", "password>", "password<", "password'", "password\"", "password;", "password&", "password|", "password`",
            "password$", "password{", "password}", "password;echo 'malicious'" })
    void validate_does_throw_exception_for_invalid_passwords(String password) {
        /* prepare */
        initRemoteDataWithCredentials(VALID_DOCKER_URL, VALID_USERNAME, password);

        /* execute */
        PrepareWrapperInputValidatorException exception = assertThrows(PrepareWrapperInputValidatorException.class, () -> validatorToTest.validate(context));

        /* test */
        assertEquals("Defined password must match the docker Api token pattern.", exception.getMessage());
    }

    @Test
    void validate_throws_exception_when_credentials_are_empty() {
        /* prepare */
        initRemoteDataWithCredentials(VALID_DOCKER_URL, VALID_USERNAME, VALID_PWD);
        remoteDataConfiguration.getCredentials().get().setUser(null); // this makes it invalid...

        /* execute */
        PrepareWrapperUsageException exception = assertThrows(PrepareWrapperUsageException.class, () -> validatorToTest.validate(context));

        /* test */
        assertEquals("Defined credentials must contain credential user and can not be empty.", exception.getMessage());
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void validate_throws_exception_when_no_username_found(String username) {
        /* prepare */
        initRemoteDataWithCredentials(VALID_DOCKER_URL, username, VALID_PWD);

        /* execute */
        PrepareWrapperUsageException exception = assertThrows(PrepareWrapperUsageException.class, () -> validatorToTest.validate(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined username must not be null or empty."));
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void validate_throws_exception_when_no_password_found(String pwd) {
        /* prepare */
        initRemoteDataWithCredentials(VALID_DOCKER_URL, VALID_USERNAME, pwd);

        /* execute */
        PrepareWrapperUsageException exception = assertThrows(PrepareWrapperUsageException.class, () -> validatorToTest.validate(context));

        /* test */
        assertEquals("Defined password must not be null or empty. Password is required for login.", exception.getMessage());

    }

    @Test
    void validate_does_not_throw_exception_when_user_credentials_are_configured_correctly() throws IOException {
        /* prepare */
        initRemoteDataWithCredentials(VALID_DOCKER_URL, VALID_USERNAME, VALID_PWD);
        remoteDataConfiguration.setType("docker");

        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validate(context));
    }

    @Test
    void validate_does_not_throw_exception_when_no_credentials_are_configured() throws IOException {
        /* prepare */
        initRemoteDataWithOutCredentials(VALID_DOCKER_URL);
        remoteDataConfiguration.setType("docker");

        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validate(context));

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
