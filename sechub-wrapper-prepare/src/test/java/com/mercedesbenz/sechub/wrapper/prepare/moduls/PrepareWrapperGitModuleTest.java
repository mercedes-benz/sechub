package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

class PrepareWrapperGitModuleTest {

    private PrepareWrapperGitModule moduleToTest;

    private PrepareWrapperGIT git;

    @BeforeEach
    void beforeEach() {
        moduleToTest = new PrepareWrapperGitModule();
        git = mock(PrepareWrapperGIT.class);
        moduleToTest.git = git;
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/notARepo/", "http://my.eval.com", "example.org" })
    void isAbleToPrepare_returns_false_when_no_git_remote_data_was_configured(String location) {
        /* prepare */
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "name": "remote_example_name",
                        "remote": {
                          "location": "$location"
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
        PrepareWrapperContext context = new PrepareWrapperContext(model, environment);

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertFalse(ableToPrepare);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/notARepo/", "http://my.eval.com", "example.org" })
    void isAbleToPrepare_returns_true_when_git_remote_data_type_was_configured(String location) {
        /* prepare */
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), mock(PrepareWrapperEnvironment.class));
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation(location);
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);

        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertTrue(ableToPrepare);
    }

    @Test
    void isAbleToPrepare_returns_false_when_configuration_is_empty() {
        /* prepare */
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), mock(PrepareWrapperEnvironment.class));
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertFalse(ableToPrepare);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo.git/", "http://host.xz/path/to/repo.git/", "git://host.xz/path/to/repo.git/",
            "git@host.com:my-repo/example.git" })
    void isAbleToPrepare_returns_true_when_git_remote_location_was_configured(String location) {
        /* prepare */
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), mock(PrepareWrapperEnvironment.class));
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation(location);
        remoteDataConfiguration.setType("");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(context);

        /* test */
        assertTrue(ableToPrepare);
    }

    @Test
    void prepare_throws_exception_when_credentials_are_empty() {
        /* prepare */
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), mock(PrepareWrapperEnvironment.class));
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
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
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), mock(PrepareWrapperEnvironment.class));
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setPassword("my-example-password");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(context));

        /* test */
        assertTrue(exception.getMessage().contains("No username found"));
    }

    @Test
    void prepare_throws_exception_when_no_password_found() {
        /* prepare */
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), mock(PrepareWrapperEnvironment.class));
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
        assertTrue(exception.getMessage().contains("No password found"));
    }

    @Test
    void prepare_successful_when_user_credentials_are_configured() throws IOException {
        /* prepare */
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), mock(PrepareWrapperEnvironment.class));
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        SecHubRemoteCredentialConfiguration credentials = new SecHubRemoteCredentialConfiguration();
        SecHubRemoteCredentialUserData user = new SecHubRemoteCredentialUserData();
        user.setName("my-example-name");
        user.setPassword("my-example-password");
        credentials.setUser(user);
        remoteDataConfiguration.setCredentials(credentials);
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(git).setEnvironmentVariables("PDS_PREPARE_CREDENTIAL_USERNAME", "my-example-name");
        verify(git).setEnvironmentVariables("PDS_PREPARE_CREDENTIAL_PASSWORD", "my-example-password");
        verify(git).cloneRepository("my-example-location");
    }

    @Test
    void prepare_successful_when_no_credentials_are_configured() throws IOException {
        /* prepare */
        PrepareWrapperContext context = new PrepareWrapperContext(createFromJSON("{}"), mock(PrepareWrapperEnvironment.class));
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
        SecHubRemoteDataConfiguration remoteDataConfiguration = new SecHubRemoteDataConfiguration();
        remoteDataConfiguration.setLocation("my-example-location");
        remoteDataConfiguration.setType("git");
        remoteDataConfigurationList.add(remoteDataConfiguration);
        context.setRemoteDataConfigurationList(remoteDataConfigurationList);

        /* execute */
        moduleToTest.prepare(context);

        /* test */
        verify(git).cloneRepository("my-example-location");
        verify(git, never()).setEnvironmentVariables(anyString(), anyString());
    }

}