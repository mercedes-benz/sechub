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
    void read_incomplete_credential_configuration_and_match_location_and_type() {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_incomplete_config.json"));
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);
        String location = "example";
        String type = "git";

        /* execute */
        List<SecHubRemoteCredentialContainer> credentialsLocation = supportToTest.matchRemoteCredentialsFromLocation(configuration, location);
        List<SecHubRemoteCredentialContainer> credentialsType = supportToTest.matchRemoteCredentialsFromType(configuration, type);

        /* test */
        assertEquals(1, credentialsLocation.size());
        assertEquals(2, credentialsType.size());

        SecHubRemoteCredentialContainer container1 = credentialsLocation.get(0);
        assertNotNull(container1);
        assertEquals("userX", container1.getUser());
        assertEquals("tokenX", container1.getPassword());

        SecHubRemoteCredentialContainer container2 = credentialsType.get(0);
        assertNotNull(container2);
        assertEquals("example-user", container2.getUser());

        SecHubRemoteCredentialContainer container2b = credentialsType.get(1);
        assertNotNull(container2b);
        assertEquals("user2", container2b.getUser());
    }

    @Test
    void match_remote_credential_pattern_by_location() {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_complex_config.json"));
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);
        String exampleURL1 = "https://github.com/username/project";
        String exampleURL2 = "https://example.url.com/examples";
        String unknownURL = "unknown.pattern";

        /* execute */
        List<SecHubRemoteCredentialContainer> credentialsURL1 = supportToTest.matchRemoteCredentialsFromLocation(configuration, exampleURL1);
        List<SecHubRemoteCredentialContainer> credentialsURL2 = supportToTest.matchRemoteCredentialsFromLocation(configuration, exampleURL2);
        List<SecHubRemoteCredentialContainer> credentialsURL3 = supportToTest.matchRemoteCredentialsFromLocation(configuration, unknownURL);

        /* test */
        assertEquals(1, credentialsURL1.size());
        assertEquals(2, credentialsURL2.size());

        SecHubRemoteCredentialContainer container1 = credentialsURL1.get(0);
        assertNotNull(container1);
        assertEquals("example-user", container1.getUser());

        SecHubRemoteCredentialContainer container2 = credentialsURL2.get(0);
        assertNotNull(container2);
        assertEquals("user3", container2.getUser());

        SecHubRemoteCredentialContainer container2b = credentialsURL2.get(1);
        assertNotNull(container2b);
        assertEquals("userX", container2b.getUser());

        assertTrue(credentialsURL3.isEmpty());
    }

    @Test
    void match_remote_credential_pattern_by_type() {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_complex_config.json"));
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);
        String type1 = "docker";
        String type2 = "git";
        String type3 = "unknown";

        /* execute */
        List<SecHubRemoteCredentialContainer> credentialsURL1 = supportToTest.matchRemoteCredentialsFromType(configuration, type1);
        List<SecHubRemoteCredentialContainer> credentialsURL2 = supportToTest.matchRemoteCredentialsFromType(configuration, type2);
        List<SecHubRemoteCredentialContainer> credentialsURL3 = supportToTest.matchRemoteCredentialsFromType(configuration, type3);

        /* test */
        assertEquals(1, credentialsURL1.size());
        assertEquals(2, credentialsURL2.size());

        SecHubRemoteCredentialContainer container1 = credentialsURL1.get(0);
        assertNotNull(container1);
        assertEquals("user3", container1.getUser());
        assertEquals("token3", container1.getPassword());

        SecHubRemoteCredentialContainer container2 = credentialsURL2.get(0);
        assertNotNull(container2);
        assertEquals("user2", container2.getUser());

        SecHubRemoteCredentialContainer container2b = credentialsURL2.get(1);
        assertNotNull(container2b);
        assertEquals("user3", container2b.getUser());

        assertTrue(credentialsURL3.isEmpty());
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void match_remote_credential_pattern_by_type_with_empty_or_null_type(String string) {
        /* prepare */
        String json = TestFileReader.loadTextFile(new File("./src/test/resources/sechub_remote_credentials_complex_config.json"));
        SecHubRemoteCredentialConfiguration configuration = supportToTest.getRemoteCredentialConfigurationFromJSONString(json);

        /* execute */
        List<SecHubRemoteCredentialContainer> credentials = supportToTest.matchRemoteCredentialsFromType(configuration, string);

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
        List<SecHubRemoteCredentialContainer> credentials = supportToTest.matchRemoteCredentialsFromLocation(configuration, string);

        /* test */
        assertTrue(credentials.isEmpty());
    }
}