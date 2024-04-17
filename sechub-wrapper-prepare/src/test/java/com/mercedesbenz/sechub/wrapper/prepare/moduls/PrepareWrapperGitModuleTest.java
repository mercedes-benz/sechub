package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperRemoteConfigurationExtractor;

class PrepareWrapperGitModuleTest {

    private PrepareWrapperGitModule moduleToTest;

    private PrepareWrapperRemoteConfigurationExtractor extractor;

    @BeforeEach
    void beforeEach() {
        extractor = new PrepareWrapperRemoteConfigurationExtractor();
        moduleToTest = new PrepareWrapperGitModule();
        moduleToTest.extractor = extractor;
    }

    // TODO: 17.04.24 laura currently using spy and package private methods to mock
    // runtime.exec
    // alternatively would powerMock be an option to mock the runtime executions?
    // https://stackoverflow.com/questions/37928042/runtime-getruntime-exec-junit-test

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

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(model);

        /* test */
        assertFalse(ableToPrepare);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/notARepo/", "http://my.eval.com", "example.org" })
    void isAbleToPrepare_returns_true_when_git_remote_data_type_was_configured(String location) {
        /* prepare */
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "name": "remote_example_name",
                        "remote": {
                          "location": "$location",
                          // type is git
                          "type": "git"
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

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(model);

        /* test */
        assertTrue(ableToPrepare);
    }

    @Test
    void isAbleToPrepare_returns_false_when_configuration_is_empty() {
        /* prepare */
        String json = """
                { }
                  """;
        SecHubConfigurationModel model = createFromJSON(json);

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(model);

        /* test */
        assertFalse(ableToPrepare);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo.git/", "http://host.xz/path/to/repo.git/", "git://host.xz/path/to/repo.git/",
            "ssh://host.xz/path/to/repo.git/" })
    void isAbleToPrepare_returns_true_when_git_remote_location_was_configured(String location) {
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

        /* execute */
        boolean ableToPrepare = moduleToTest.isAbleToPrepare(model);

        /* test */
        assertTrue(ableToPrepare);
    }

    @Test
    void prepare_throws_exception_when_credentials_are_empty() {
        /* prepare */
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "name": "remote_example_name",
                        "remote": {
                          "location": "https://host.xz/path/to/repo.git/",
                          "type": "git",
                          // empty credentials throw exception
                          "credentials": {
                          }
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
                """;
        SecHubConfigurationModel model = createFromJSON(json);
        String folder = "folder";

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(model, folder));

        /* test */
        assertTrue(exception.getMessage().contains("Defined credentials were empty"));
    }

    @Test
    void prepare_throws_exception_when_no_username_or_password_found() {
        /* prepare */
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "name": "remote_example_name",
                        "remote": {
                          "location": "https://host.xz/path/to/repo.git/",
                          "type": "git",
                          "credentials": {
                            "user": {
                              // no username or password
                            }
                          }
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
                """;
        SecHubConfigurationModel model = createFromJSON(json);
        String folder = "folder";

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.prepare(model, folder));

        /* test */
        assertTrue(exception.getMessage().contains("No username or password found"));
    }

    @Test
    void prepare_successful_when_user_credentials_are_configured() throws IOException {
        /* prepare */
        PrepareWrapperGitModule spyModuleToTest = spy(moduleToTest);
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "name": "remote_example_name",
                        "remote": {
                          "location": "https://host.xz/path/to/repo.git/",
                          "type": "git",
                           "credentials": {
                           "user": {
                             "name": "my-example-user",
                             "password": "my-example-password"
                           }
                         }
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
                """;
        SecHubConfigurationModel model = createFromJSON(json);
        String folder = "folder";
        // mock cloneRepository to prevent executing git clone command
        doNothing().when(spyModuleToTest).cloneRepository(anyString(), anyString());

        /* execute */
        spyModuleToTest.prepare(model, folder);

        /* test */
        verify(spyModuleToTest, times(1)).cloneRepository(anyString(), anyString());
        verify(spyModuleToTest, times(1)).prepareLocationForPrivateRepo(anyString(), anyString(), anyString());
    }

    @Test
    void prepare_successful_when_no_credentials_are_configured() throws IOException {
        /* prepare */
        PrepareWrapperGitModule spyModuleToTest = spy(moduleToTest);
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "name": "remote_example_name",
                        "remote": {
                          "location": "https://host.xz/path/to/repo.git/",
                          "type": "git"
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
                """;
        SecHubConfigurationModel model = createFromJSON(json);
        String folder = "folder";
        // mock cloneRepository to prevent executing git clone command
        doNothing().when(spyModuleToTest).cloneRepository(anyString(), anyString());

        /* execute */
        spyModuleToTest.prepare(model, folder);

        /* test */
        verify(spyModuleToTest, times(1)).cloneRepository(anyString(), anyString());
        verify(spyModuleToTest, times(0)).prepareLocationForPrivateRepo(anyString(), anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git", "http://myrepo/here/.git", "example.org.git" })
    void prepareLocationForPrivateRepo_returns_prepared_location(String location) {
        /* execute */
        String preparedLocation = moduleToTest.prepareLocationForPrivateRepo(location, "my-example-user", "my-example-password");

        /* test */
        assertTrue(preparedLocation.contains("https://my-example-user:my-example-password@"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://host.xz/path/to/repo/.git rm -rf * some", "rm https://myrepo/here/.git", "example.org.git" })
    void validateLocationURL_throws_exception_when_location_is_invalid_URL(String location) {
        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> moduleToTest.validateLocationURL(location));

        /* test */
        assertTrue(exception.getMessage().contains("Invalid URL,"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://my-example-user:my-example-password@host.xz/path/to/repo/.git",
            "https://my-example-user:my-example-password@myrepo/here/.git", "https://my-example-user:my-example-password@example.org.git" })
    void validateLocationURL_does_not_throw_exception_when_location_is_valid_URL(String location) {
        /* execute + test */
        moduleToTest.validateLocationURL(location);
    }
}