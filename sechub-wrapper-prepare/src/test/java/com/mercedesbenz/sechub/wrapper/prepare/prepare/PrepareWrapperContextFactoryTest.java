package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.RemoteCredentialContainerFactory;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

class PrepareWrapperContextFactoryTest {

    private static final String PROJECT1 = "project1";

    private PrepareWrapperContextFactory factoryToTest;

    private PrepareWrapperEnvironment environment;

    private RemoteCredentialContainerFactory remoteCredentialContainerFactory;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new PrepareWrapperContextFactory();

        environment = mock(PrepareWrapperEnvironment.class);
        remoteCredentialContainerFactory = new RemoteCredentialContainerFactory();

        factoryToTest.remoteCredentialContainerFactory = remoteCredentialContainerFactory;

    }

    @Test
    void when_sechub_model_and_credential_model_exists_created_context_is_not_null() {
        /* prepare */
        String secHubModelJson = createValidSecHubModel();
        String remoteCredentialModel = createValidCredentialModel();

        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);
        when(environment.getRemoteCredentialConfigurationAsJSON()).thenReturn(remoteCredentialModel);

        /* execute */
        PrepareWrapperContext result = factoryToTest.create(environment);

        /* test */
        assertNotNull(result);
    }

    @Test
    void when_sechub_model_and_credential_model_exists_created_context_has_fields() {
        /* prepare */
        String secHubModelJson = createValidSecHubModel();
        String remoteCredentialModel = createValidCredentialModel();

        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);
        when(environment.getRemoteCredentialConfigurationAsJSON()).thenReturn(remoteCredentialModel);

        /* execute */
        PrepareWrapperContext result = factoryToTest.create(environment);

        /* test */
        assertNotNull(result.getEnvironment());
        assertNotNull(result.getRemoteCredentialContainer());
        assertNotNull(result.getSecHubConfiguration());

        assertEquals(environment, result.getEnvironment());

        assertEquals(2, result.getRemoteCredentialContainer().getConfiguration().getCredentials().size());
    }

    @Test
    void when_no_sechub_model_json_exists_an_illegal_state_exception_is_thrown() {
        /* prepare */
        String remoteCredentialModel = createValidCredentialModel();
        when(environment.getRemoteCredentialConfigurationAsJSON()).thenReturn(remoteCredentialModel);

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create(environment));

        /* test */
        assertTrue(exception.getMessage().contains("No SecHub model"));
    }

    @Test
    void when_no_remote_credential_model_json_exists_an_illegal_state_exception_is_thrown() {
        /* prepare */
        String secHubModelJson = createValidSecHubModel();
        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);

        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create(environment));

        /* test */
        assertTrue(exception.getMessage().contains("No Remote Data Configuration model"));
    }

    private String createValidSecHubModel() {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setProjectId(PROJECT1);

        String secHubModelJson = JSONConverter.get().toJSON(model);
        return secHubModelJson;
    }

    private String createValidCredentialModel() {
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
        return json;
    }

}