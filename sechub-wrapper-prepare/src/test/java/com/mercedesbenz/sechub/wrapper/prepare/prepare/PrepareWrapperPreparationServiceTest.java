package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import static com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration.createFromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperRemoteConfigurationExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

import java.io.IOException;

class PrepareWrapperPreparationServiceTest {

    private PrepareWrapperPreparationService serviceToTest;

    PrepareWrapperEnvironment environment;

    PrepareWrapperContextFactory factory;

    PrepareWrapperContext context;

    PrepareWrapperRemoteConfigurationExtractor extractor;

    @BeforeEach
    void beforeEach() {

        environment = mock(PrepareWrapperEnvironment.class);
        factory = mock(PrepareWrapperContextFactory.class);
        context = mock(PrepareWrapperContext.class);
        extractor = new PrepareWrapperRemoteConfigurationExtractor();
        when(factory.create(environment)).thenReturn(context);

        serviceToTest = new PrepareWrapperPreparationService();
        serviceToTest.environment = environment;
        serviceToTest.factory = factory;
        serviceToTest.extractor = extractor;

    }

    @Test
    void when_no_remote_data_was_configured_return_preparation_success_with_warn_message() throws IOException {
        /* prepare */
        when(context.getSecHubConfiguration()).thenReturn(new SecHubConfigurationModel());

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=OK", result.getProductResult());
        assertEquals(1, result.getProductMessages().size());
        assertEquals("No Remote Configuration found", result.getProductMessages().get(0).getText());
    }

    @Test
    void when_remote_data_was_configured_return_preparation_success_without_message() throws IOException {
        /* prepare */
        String json = """
                {
                  "apiVersion": "1.0",
                  "data": {
                    "sources": [
                      {
                        "name": "remote_example_name",
                        "remote": {
                          "location": "remote_example_location",
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
        when(context.getSecHubConfiguration()).thenReturn(model);

        /* execute */
        AdapterExecutionResult result = serviceToTest.startPreparation();

        /* test */
        assertEquals("SECHUB_PREPARE_RESULT;status=OK", result.getProductResult());
        assertEquals(0, result.getProductMessages().size());
    }

    @Test
    void when_context_was_not_filled_correctly_throws_exception() {
        /* execution + test */
        assertThrows(IllegalStateException.class, () -> serviceToTest.startPreparation());
    }

}