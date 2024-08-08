// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.test.TestFileReader;

class RemoteCredentialContainerTest {

    private RemoteCredentialContainer containerToTest;

    @Test
    void resolve_remote_credentials_by_location_and_accept_all_types() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/sechub_remote_credentials_config.json"));
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);
        containerToTest = new RemoteCredentialContainerFactory().create(configuration);
        String location1 = "https://github.com/username/project";
        String location2 = "https://example.url.com/examples";
        String location3 = "unknown.pattern";

        /* execute */
        List<RemoteCredentialData> credentials1 = containerToTest.resolveCredentialsForLocation(location1);
        List<RemoteCredentialData> credentials2 = containerToTest.resolveCredentialsForLocation(location2);
        List<RemoteCredentialData> credentials3 = containerToTest.resolveCredentialsForLocation(location3);

        /* test */
        assertEquals(1, credentials1.size());
        assertEquals(3, credentials2.size());

        RemoteCredentialData container1 = credentials1.get(0);
        assertNotNull(container1);
        assertEquals("example-user", container1.getUser());

        RemoteCredentialData container2 = credentials2.get(0);
        assertNotNull(container2);
        assertEquals("user2", container2.getUser());

        RemoteCredentialData container2b = credentials2.get(1);
        assertNotNull(container2b);
        assertEquals("user3", container2b.getUser());

        RemoteCredentialData container2c = credentials2.get(2);
        assertNotNull(container2c);
        assertEquals("userX", container2c.getUser());

        assertTrue(credentials3.isEmpty());
    }

    @Test
    void resolve_remote_credentials_by_location_and_accept_specific_types() {
        /* prepare */
        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/sechub_remote_credentials_config.json"));
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);
        containerToTest = new RemoteCredentialContainerFactory().create(configuration);
        String type1 = "docker";
        String type2 = "git";
        String type3 = "unknown";
        String type4 = "";
        String location = "https://example.url.com";

        /* execute */
        List<RemoteCredentialData> credentials1 = containerToTest.resolveCredentialsForLocation(location, type1);
        List<RemoteCredentialData> credentials2 = containerToTest.resolveCredentialsForLocation(location, type2);
        List<RemoteCredentialData> credentials3 = containerToTest.resolveCredentialsForLocation(location, type3);
        List<RemoteCredentialData> credentials4 = containerToTest.resolveCredentialsForLocation(location, type4);

        /* test */
        assertEquals(1, credentials1.size());
        assertEquals(2, credentials2.size());

        RemoteCredentialData container1 = credentials1.get(0);
        assertNotNull(container1);
        assertEquals("user3", container1.getUser());
        assertEquals("token3", container1.getPassword());

        RemoteCredentialData container2 = credentials2.get(0);
        assertNotNull(container2);
        assertEquals("user2", container2.getUser());

        RemoteCredentialData container2b = credentials2.get(1);
        assertNotNull(container2b);
        assertEquals("user3", container2b.getUser());

        assertTrue(credentials3.isEmpty());

        assertTrue(credentials4.isEmpty());
    }

    @Test
    void unable_to_resolve_credentials_from_configuration_where_location_is_not_set_but_type_matches() {
        /* prepare */
        String json = """
                {
                  "credentials": [
                  {
                      "user" : "user2",
                      "password" : "password2",
                      "types" : ["git"]
                      // missing entry for remotePattern
                    }
                  ]
                }
                """;
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);
        containerToTest = new RemoteCredentialContainerFactory().create(configuration);
        String location = "example";
        String type = "git";

        /* execute */
        List<RemoteCredentialData> credentialsLocation = containerToTest.resolveCredentialsForLocation(location, type);

        /* test */
        assertEquals(0, credentialsLocation.size());
    }

    @Test
    void resolve_credentials_from_configuration_where_no_types_are_set_but_all_types_are_accepted() {
        /* prepare */
        String json = """
                {
                  "credentials": [
                  {
                      "user" : "user2",
                      "password" : "password2",
                      "remotePattern" : ".*example.*"
                      // missing entry for types
                    }
                  ]
                }
                """;
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);
        containerToTest = new RemoteCredentialContainerFactory().create(configuration);
        String location = "example";

        /* execute */
        List<RemoteCredentialData> credentialsLocation = containerToTest.resolveCredentialsForLocation(location);

        /* test */
        assertEquals(1, credentialsLocation.size());

        RemoteCredentialData container1 = credentialsLocation.get(0);
        assertNotNull(container1);
        assertEquals("user2", container1.getUser());
        assertEquals("password2", container1.getPassword());
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void resolve_remote_credential_pattern_by_location_with_empty_or_null_location(String location) {
        /* prepare */
        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/sechub_remote_credentials_config.json"));
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);
        containerToTest = new RemoteCredentialContainerFactory().create(configuration);

        /* execute */
        List<RemoteCredentialData> credentials = containerToTest.resolveCredentialsForLocation(location);

        /* test */
        assertTrue(credentials.isEmpty());
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void resolve_remote_credential_by_unknown_location_with_empty_or_null_type(String type) {
        /* prepare */
        String json = TestFileReader.readTextFromFile(new File("./src/test/resources/sechub_remote_credentials_config.json"));
        RemoteCredentialConfiguration configuration = RemoteCredentialConfiguration.fromJSONString(json);
        containerToTest = new RemoteCredentialContainerFactory().create(configuration);
        String location = "unknown-location";

        /* execute */
        List<RemoteCredentialData> credentials = containerToTest.resolveCredentialsForLocation(location, type);

        /* test */
        assertTrue(credentials.isEmpty());
    }

}