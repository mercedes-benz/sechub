package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SkopeoInputValidatorTest {

    SkopeoInputValidator validatorToTest;

    @BeforeEach
    void beforeEach() {
        validatorToTest = new SkopeoInputValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = { "ubuntu:22.04", "ubuntu", "docker://ubuntu:22.04", "docker://ubuntu", "oci:busybox_ocilayout:latest", "https://hub.docker.com",
            "docker://docker.io/library/busybox:latest", "ubuntu@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d",
            "ghcr.io/owner/repo:tag" })
    void validateLocation_returns_true_for_valid_docker_urls(String location) {
        /* execute + test */
        assertTrue(validatorToTest.validateLocation(location));
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid-registry ubuntu:22.04", "docker://registry/ubuntu:invalid tag", "docker://ubuntu:tag$maliciousCode",
            "docker://ubuntu:tag|maliciousCode", "my-registry/oci:busybox_ocilayout;latest", })
    void validateLocation_returns_false_for_invalid_docker_urls(String location) {
        /* execute + test */
        assertFalse(validatorToTest.validateLocation(location));
    }

    @ParameterizedTest
    @ValueSource(strings = { "username", "username123", "username_123", "username-123", "username-123_456", "username1234567890123456789003890123456",
            "user_user_user" })
    void validateUsername_returns_true_for_valid_usernames(String username) {
        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validateUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = { "user name", "username?", "username!", "username>", "username<", "username'", "username\"", "username;", "username&", "username|",
            "username`", "username$", "username{", "username}" })
    void validateUsername_throws_exception_for_invalid_usernames(String username) {
        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validatorToTest.validateUsername(username));

        /* test */
        assertEquals("Defined username must match the modules pattern.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "password", "password123", "password_123", "password-123", "password-123_456", "password1234567890123456789003890123456",
            "dXNlckBleGFtcGxlLmNvbTpzZWNyZXQexample", "Z2hjcl9wczpzc2VjcmV0example" })
    void validatePassword_returns_true_for_valid_passwords(String password) {
        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validatePassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = { "password?", "password!", "password>", "password<", "password'", "password\"", "password;", "password&", "password|", "password`",
            "password$", "password{", "password}", "password;echo 'malicious'" })
    void validatePassword_throws_exception_for_invalid_passwords(String password) {
        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validatorToTest.validatePassword(password));

        /* test */
        assertEquals("Defined password must match the Skopeo Api token pattern.", exception.getMessage());
    }
}
