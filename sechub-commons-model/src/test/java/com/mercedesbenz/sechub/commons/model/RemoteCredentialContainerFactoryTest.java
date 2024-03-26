package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RemoteCredentialContainerFactoryTest {

    @ParameterizedTest
    @ValueSource(strings = { "notA [ remote pattern", "( alsoNoPattern", "§[}" })
    void read_invalid_pattern_from_configuration_throws_illegal_state_exception(String stringPattern) {
        /* prepare */
        String json = """
                {
                  "credentials": [
                  {
                      "user" : "user2",
                      "password" : "password2",
                      // invalid regex pattern
                      "remotePattern" : "$pattern"
                    }
                  ]
                }
                """.replace("$pattern", stringPattern);
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new RemoteCredentialContainerFactory().create(configuration));

        /* test */
        assertEquals("Was not able to parse remote credential configuration.", exception.getMessage());
    }

    @Test
    void read_empty_credential_configuration_from_string() {
        /* prepare */
        String json = """
                {}
                """;
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);

        /* execute */
        RemoteCredentialContainer container = new RemoteCredentialContainerFactory().create(configuration);

        /* test */
        assertTrue(container.getPatternMap().isEmpty());
        assertTrue(container.getConfiguration().getCredentials().isEmpty());
    }

    @Test
    void read_configuration_with_one_entry_without_remote_pattern_returns_empty_pattern_map() {
        /* prepare */
        String json = """
                {
                    "credentials": [
                    {
                        "user" : "user2",
                        "password" : "password2",
                        "types" : ["git"]
                      }
                    ]
                  }
                  """;
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);

        /* execute */
        RemoteCredentialContainer container = new RemoteCredentialContainerFactory().create(configuration);

        /* test */
        assertTrue(container.getPatternMap().isEmpty());
        assertEquals(1, container.getConfiguration().getCredentials().size());
    }

    @Test
    void read_configuration_with_two_complete_entries_returns_two_credentials_and_patterns() {
        /* prepare */
        String json = """
                {
                    "credentials": [
                    {
                        "user" : "user1",
                        "password" : "password1",
                        "remotePattern" : "pattern1",
                        "types" : ["git"]
                      },
                      {
                        "user" : "user2",
                        "password" : "password2",
                        "remotePattern" : "pattern2",
                        "types" : ["git"]
                      }
                    ]
                  }
                  """;
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);

        /* execute */
        RemoteCredentialContainer container = new RemoteCredentialContainerFactory().create(configuration);

        /* test */
        assertEquals(2, container.getPatternMap().size());
        assertEquals(2, container.getConfiguration().getCredentials().size());

        RemoteCredentialData data1 = container.getConfiguration().getCredentials().get(0);
        assertEquals("user1", data1.getUser());

        RemoteCredentialData data2 = container.getConfiguration().getCredentials().get(1);
        assertEquals("user2", data2.getUser());
    }

}