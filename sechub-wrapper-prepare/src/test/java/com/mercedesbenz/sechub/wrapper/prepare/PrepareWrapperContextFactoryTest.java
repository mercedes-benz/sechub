// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

class PrepareWrapperContextFactoryTest {

    private static final String PROJECT1 = "project1";
    private PrepareWrapperContextFactory factoryToTest;
    private PrepareWrapperEnvironment environment;
    private PrepareWrapperRemoteConfigurationExtractor extractor;

    @BeforeEach
    void beforeEach() {
        environment = mock(PrepareWrapperEnvironment.class);
        extractor = mock(PrepareWrapperRemoteConfigurationExtractor.class);
        factoryToTest = new PrepareWrapperContextFactory(extractor);
    }

    @Test
    void when_sechub_model_exists_created_context_is_not_null() {
        /* prepare */
        String secHubModelJson = createValidSecHubModel();
        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);

        /* execute */
        PrepareWrapperContext result = factoryToTest.create(environment);

        /* test */
        assertNotNull(result);
    }

    @Test
    void when_sechub_model_exists_created_context_has_fields() {
        /* prepare */
        String secHubModelJson = createValidSecHubModel();

        when(environment.getSechubConfigurationModelAsJson()).thenReturn(secHubModelJson);

        /* execute */
        PrepareWrapperContext result = factoryToTest.create(environment);

        /* test */
        assertNotNull(result.getEnvironment());
        assertNotNull(result.getSecHubConfiguration());

        assertEquals(environment, result.getEnvironment());

    }

    @Test
    void when_no_sechub_model_json_exists_an_illegal_state_exception_is_thrown() {
        /* execute + test */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factoryToTest.create(environment));

        /* test */
        assertTrue(exception.getMessage().contains("No SecHub model"));
    }

    private String createValidSecHubModel() {
        SecHubConfigurationModel model = new SecHubConfigurationModel();
        model.setProjectId(PROJECT1);

        return JSONConverter.get().toJSON(model);
    }

}