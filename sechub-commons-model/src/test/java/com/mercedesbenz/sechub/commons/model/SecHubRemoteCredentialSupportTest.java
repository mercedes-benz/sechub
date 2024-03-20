package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.test.TestFileReader;

class SecHubRemoteCredentialSupportTest {

    private SecHubRemoteCredentialSupport supportToTest;

    @BeforeEach
    void beforeEach() {
        supportToTest = new SecHubRemoteCredentialSupport();
    }

    @Test
    void read_simple_credential_configuration_from_json_string() {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_simple_config.json"));

        /* execute */
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);

        /* test */
        List<SecHubRemoteCredentialContainer> credentials = configuration.getCredentials();
        assertEquals(1, credentials.size());

        SecHubRemoteCredentialContainer container = credentials.get(0);
        assertNotNull(container);
        assertEquals("example-user", container.getUser());
        assertEquals("example-api-token-or-password", container.getPassword());
        assertEquals(".*github.com/*", container.getRemotePattern());
        assertEquals("example-type", container.getType());
    }

    @Test
    void read_empty_credential_configuration_from_json_string() {
        /* prepare */
        String json = "{}";

        /* execute */
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);

        /* test */
        assertEquals(0, configuration.getCredentials().size());
    }

    @Test
    void read_incomplete_credential_configuration_and_match_location_and_type_where_possible() {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_incomplete_config.json"));
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);
        String location = "example";
        String type = "git";

        /* execute */
        List<SecHubRemoteCredentialContainer> credentialsLocation = supportToTest.resolveCredentialsForLocation(configuration, location);
        List<SecHubRemoteCredentialContainer> credentialsType = supportToTest.resolveCredentialsForLocation(configuration, location, type);

        /* test */
        assertEquals(1, credentialsLocation.size());
        assertEquals(0, credentialsType.size());

        SecHubRemoteCredentialContainer container1 = credentialsLocation.get(0);
        assertNotNull(container1);
        assertEquals("userX", container1.getUser());
        assertEquals("tokenX", container1.getPassword());

    }

    @Test
    void match_remote_credentials_pattern_by_location() {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_complex_config.json"));
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);
        String location1 = "https://github.com/username/project";
        String location2 = "https://example.url.com/examples";
        String location3 = "unknown.pattern";

        /* execute */
        List<SecHubRemoteCredentialContainer> credentials1 = supportToTest.resolveCredentialsForLocation(configuration, location1);
        List<SecHubRemoteCredentialContainer> credentials2 = supportToTest.resolveCredentialsForLocation(configuration, location2);
        List<SecHubRemoteCredentialContainer> credentials3 = supportToTest.resolveCredentialsForLocation(configuration, location3);

        /* test */
        assertEquals(1, credentials1.size());
        assertEquals(3, credentials2.size());

        SecHubRemoteCredentialContainer container1 = credentials1.get(0);
        assertNotNull(container1);
        assertEquals("example-user", container1.getUser());

        SecHubRemoteCredentialContainer container2 = credentials2.get(0);
        assertNotNull(container2);
        assertEquals("user2", container2.getUser());

        SecHubRemoteCredentialContainer container2b = credentials2.get(1);
        assertNotNull(container2b);
        assertEquals("user3", container2b.getUser());

        SecHubRemoteCredentialContainer container2c = credentials2.get(2);
        assertNotNull(container2c);
        assertEquals("userX", container2c.getUser());

        assertTrue(credentials3.isEmpty());
    }

    @Test
    void match_remote_credentials_pattern_by_type() {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_complex_config.json"));
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);
        String type1 = "docker";
        String type2 = "git";
        String type3 = "unknown";
        String location = "https://example.url.com";

        /* execute */
        List<SecHubRemoteCredentialContainer> credentials1 = supportToTest.resolveCredentialsForLocation(configuration, location, type1);
        List<SecHubRemoteCredentialContainer> credentials2 = supportToTest.resolveCredentialsForLocation(configuration, location, type2);
        List<SecHubRemoteCredentialContainer> credentials3 = supportToTest.resolveCredentialsForLocation(configuration, location, type3);

        /* test */
        assertEquals(1, credentials1.size());
        assertEquals(2, credentials2.size());

        SecHubRemoteCredentialContainer container1 = credentials1.get(0);
        assertNotNull(container1);
        assertEquals("user3", container1.getUser());
        assertEquals("token3", container1.getPassword());

        SecHubRemoteCredentialContainer container2 = credentials2.get(0);
        assertNotNull(container2);
        assertEquals("user2", container2.getUser());

        SecHubRemoteCredentialContainer container2b = credentials2.get(1);
        assertNotNull(container2b);
        assertEquals("user3", container2b.getUser());

        assertTrue(credentials3.isEmpty());
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void match_remote_credential_pattern_by_type_with_empty_or_null_type(String string) {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_complex_config.json"));
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);

        /* execute */
        List<SecHubRemoteCredentialContainer> credentials = supportToTest.resolveCredentialsForLocation(configuration, string);

        /* test */
        assertTrue(credentials.isEmpty());
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void match_remote_credential_pattern_by_location_with_empty_or_null_location(String string) {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_complex_config.json"));
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);

        /* execute */
        List<SecHubRemoteCredentialContainer> credentials = supportToTest.resolveCredentialsForLocation(configuration, string);

        /* test */
        assertTrue(credentials.isEmpty());
    }
}