package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperRemoteConfigurationExtractor;

class PrepareWrapperModuleSkopeoTest {

    PrepareWrapperModuleSkopeo moduleToTest;
    SkopeoInputValidator skopeoInputValidator;
    WrapperSkopeo skopeo;

    TestFileWriter writer;

    @BeforeEach
    void beforeEach() {
        moduleToTest = new PrepareWrapperModuleSkopeo();
        skopeoInputValidator = new SkopeoInputValidator();
        writer = new TestFileWriter();
        skopeo = mock(WrapperSkopeo.class);

        moduleToTest.skopeoInputValidator = skopeoInputValidator;
        moduleToTest.skopeo = skopeo;
    }

    // TODO: 07.05.24 laura add tests

    @ParameterizedTest
    @ValueSource(strings = { "ubuntu:22.04", "ubuntu", "docker://ubuntu:22.04", "docker://ubuntu", "oci:busybox_ocilayout:latest", "https://hub.docker.com",
            "docker://docker.io/library/busybox:latest", "ubuntu@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d",
            "ghcr.io/owner/repo:tag" })
    void isAbleToPrepare_returnsFalse_whenSkopeoModuleIsDisabled(String location) {
        /* prepare */
        PrepareWrapperContext context = createContextWithRemoteDataConfig(location);
        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleSkopeoEnabled", false);

        /* execute */
        boolean result = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertFalse(result);
    }

    @Test
    void isAbleToPrepare_returnsFalse_whenNoRemoteDataConfigurationIsAvailable() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleSkopeoEnabled", true);

        /* execute */
        boolean result = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "http://host.xz/path/to/repo.git/", "git://host.xz/path/to/repo.git/", "git@host.com:my-repo/example.git" })
    void isAbleToPrepare_returnsFalse_whenRemoteDataConfigurationIsNotSkopeo(String location) {
        /* prepare */
        PrepareWrapperContext context = createContextWithRemoteDataConfig(location);
        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleSkopeoEnabled", true);

        /* execute */
        boolean result = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "ubuntu:22.04", "ubuntu", "docker://ubuntu:22.04", "docker://ubuntu", "oci:busybox_ocilayout:latest", "https://hub.docker.com",
            "docker://docker.io/library/busybox:latest", "ubuntu@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d",
            "ghcr.io/owner/repo:tag" })
    void isAbleToPrepare_returnsTrue_whenRemoteDataConfigurationIsSkopeo(String location) {
        /* prepare */
        PrepareWrapperContext context = createContextWithRemoteDataConfig(location);
        ReflectionTestUtils.setField(moduleToTest, "pdsPrepareModuleSkopeoEnabled", true);

        /* execute */
        boolean result = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertTrue(result);
    }

    @Test
    void prepare_throws_exception_when_credentials_are_empty() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined credentials have no credential"));
    }

    @Test
    void prepare_throws_exception_when_no_username_found() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setPassword("my-example-password");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined username must not be null or empty."));
    }

    @Test
    void prepare_throws_exception_when_no_password_found() {
        /* prepare */
        PrepareWrapperContext context = createContextEmptyConfig();
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));

        /* test */
        assertTrue(exception.getMessage().contains("Defined password must not be null or empty."));
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

        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        user.setPassword("ghp_exampleAPITOKEN8ffne3l6g9f393r8fbcsf");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(skopeo).download(any(SkopeoContext.class));
        verify(skopeo).cleanUploadDirectory(tempDir.toString());
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

        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        user.setPassword("ghp_exampleAPITOKEN8ffne3l6g9f393r8fbcsf");
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("docker");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(skopeo).download(any(SkopeoContext.class));
        verify(skopeo).cleanUploadDirectory(tempDir.toString());
    }

    @Test
    void isDownloadSuccessful_returns_true_when_tar_file_in_directory() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        String filename = "testimage.tar";
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getEnvironment()).thenReturn(mock(PrepareWrapperEnvironment.class));
        writer.save(new File(tempDir, filename), "some text", true);
        when(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());

        /* execute */
        boolean result = moduleToTest.isDownloadSuccessful(context);

        /* test */
        assertTrue(result);
    }

    @Test
    void isDownloadSuccessful_returns_false_when_no_tar_file_in_directory() throws IOException {
        /* prepare */
        File tempDir = Files.createTempDirectory("upload-folder").toFile();
        tempDir.deleteOnExit();
        writer.save(tempDir, "some text", true);
        PrepareWrapperContext context = mock(PrepareWrapperContext.class);
        when(context.getEnvironment()).thenReturn(mock(PrepareWrapperEnvironment.class));
        when(context.getEnvironment().getPdsPrepareUploadFolderDirectory()).thenReturn(tempDir.toString());

        /* execute */
        boolean result = moduleToTest.isDownloadSuccessful(context);

        /* test */
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "docker", "DOCKER", "DockEr" })
    void isMatchingSkopeoType_returns_true_when_docker_is_configured(String type) {
        /* execute */
        boolean result = moduleToTest.isMatchingSkopeoType(type);

        /* test */
        assertTrue(result);
    }

    private PrepareWrapperContext createContextEmptyConfig() {
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn("test-upload-folder");
        return new PrepareWrapperContext(createFromJSON("{}"), environment);
    }

    private PrepareWrapperContext createContextWithRemoteDataConfig(String location) {
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "name": "remote_example_name",
                        "remote": {
                          "location": "$location",
                            "type": "docker"
                        }
                      }
                    ]
                  },
                  "codeScan": {
                    "use": [
                      "remote_example_name"
                    ]
                  }
                }
                """.replace("$location", location);
        SecHubConfigurationModel model = createFromJSON(json);
        PrepareWrapperEnvironment environment = mock(PrepareWrapperEnvironment.class);
        PrepareWrapperRemoteConfigurationExtractor extractor = new PrepareWrapperRemoteConfigurationExtractor();
        List<SecHubRemoteDataConfiguration> creds = extractor.extract(model);
        when(environment.getPdsPrepareUploadFolderDirectory()).thenReturn("test-upload-folder");
        PrepareWrapperContext context = new PrepareWrapperContext(model, environment);
        context.setRemoteDataConfigurationList(creds);
        return context;
    }
}