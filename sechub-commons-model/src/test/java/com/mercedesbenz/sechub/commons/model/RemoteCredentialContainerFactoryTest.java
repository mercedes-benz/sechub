package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RemoteCredentialContainerFactoryTest {

    @Test
    void throws_illegal_state_exception_invalid_pattern() {
        /* prepare */
        String json = """
                {
                  "credentials": [
                  {
                      "user" : "user2",
                      "password" : "password2",
                      "remotePattern" : "notA [ remote pattern"
                    }
                  ]
                }
                """;
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);

        /* test + execute */
        assertThrows(IllegalStateException.class, () -> new RemoteCredentialContainerFactory().create(configuration));
    }

}