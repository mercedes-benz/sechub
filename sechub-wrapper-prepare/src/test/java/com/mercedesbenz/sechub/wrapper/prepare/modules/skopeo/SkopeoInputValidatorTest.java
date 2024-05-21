package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperInputValidatorException;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

class SkopeoInputValidatorTest {

    SkopeoInputValidator validatorToTest;

    TestFileWriter writer;

    @BeforeEach
    void beforeEach() {
        writer = new TestFileWriter();
        validatorToTest = new SkopeoInputValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = { "ubuntu:22.04", "ubuntu", "docker://ubuntu:22.04", "docker://ubuntu", "oci:busybox_ocilayout:latest", "https://hub.docker.com",
            "docker://docker.io/library/busybox:latest", "ubuntu@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d",
            "ghcr.io/owner/repo:tag" })
    void validateLocation_returns_true_for_valid_docker_urls(String location) {
        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validateLocation(location));
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid-registry ubuntu:22.04", "docker://registry/ubuntu:invalid tag", "docker://ubuntu:tag$maliciousCode",
            "docker://ubuntu:tag|maliciousCode", "my-registry/oci:busybox_ocilayout;latest", })
    void validateLocation_throws_IllegalArgumentException_for_invalid_docker_urls(String location) {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> validatorToTest.validateLocation(location));
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
        PrepareWrapperInputValidatorException exception = assertThrows(PrepareWrapperInputValidatorException.class,
                () -> validatorToTest.validateUsername(username));

        /* test */
        assertEquals("Defined username must match the docker pattern.", exception.getMessage());
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
        PrepareWrapperInputValidatorException exception = assertThrows(PrepareWrapperInputValidatorException.class,
                () -> validatorToTest.validatePassword(password));

        /* test */
        assertEquals("Defined password must match the docker Api token pattern.", exception.getMessage());
    }

    @Test
    void validate_throws_exception_when_credentials_are_empty() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validatorToTest.validate(context));

        /* test */
        assertEquals("Defined credentials must not be null.", exception.getMessage());
    }

    @Test
    void prepare_throws_exception_when_no_username_found() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setPassword("my-example-password");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validatorToTest.validate(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined username must not be null or empty."));
    }

    @Test
    void prepare_throws_exception_when_no_password_found() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validatorToTest.validate(context));

        /* test */
        assertEquals("Defined password must not be null or empty.", exception.getMessage());

    }

    @Test
    void prepare_successful_when_user_credentials_are_configured_correctly() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = "testimage.tar";
        writer.save(new File(tempDir, filename), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        user.setPassword("ghp_exampleAPITOKEN8ffne3l6g9f393r8fbcsf");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validate(context));
    }

    @Test
    void prepare_successful_when_no_credentials_are_configured() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = "testimage.tar";
        writer.save(new File(tempDir, filename), "some text", true);

        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), environment);

        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        context.setRemoteDataConfiguration(remoteDataConfiguration);

        /* execute + test */
        assertDoesNotThrow(() -> validatorToTest.validate(context));

    }

    private PrepareWrapperContext createContextEmptyConfig() {
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn("test-upload-folder");
        return new PrepareWrapperContext(createFromJSON("{}"), environment);
    }
}
