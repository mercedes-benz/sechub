// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationTypeListParser;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;

class PDSConfigurationAutoFixTest {

    private PDSConfigurationAutoFix autoFixToTest;
    private SecHubDataConfigurationTypeListParser typeListParser;
    private PDSServerConfiguration configuration;
    private PDSProductSetup productSetup1;

    @BeforeEach
    void beforeEach() {

        typeListParser = mock(SecHubDataConfigurationTypeListParser.class);

        autoFixToTest = new PDSConfigurationAutoFix();
        autoFixToTest.typeListParser = typeListParser;

        productSetup1 = new PDSProductSetup();
        productSetup1.setId("product1");

        configuration = new PDSServerConfiguration();
        configuration.getProducts().add(productSetup1);
    }

    @Test
    void when_all_autofixed_parameters_are_not_defined_all_are_added_as_mandatory_parameters_with_defaults() {
        /* check precondition */
        List<PDSProductParameterDefinition> mandatoryParams = productSetup1.getParameters().getMandatory();
        List<PDSProductParameterDefinition> optionalParams = productSetup1.getParameters().getOptional();

        assertEquals(0, mandatoryParams.size());
        assertEquals(0, optionalParams.size());

        /* execute */
        autoFixToTest.autofixWhenNecessary(configuration);

        /* test */

        assertDefinition(PARAM_KEY_PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRIES_MAX, mandatoryParams).hasDefault("3");
        assertDefinition(PARAM_KEY_PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRY_WAIT_SECONDS, mandatoryParams).hasDefault("10");
        assertDefinition(PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES, mandatoryParams).hasDefault("SOURCE,BINARY,NONE");

        assertEquals(3, mandatoryParams.size());
        assertEquals(0, optionalParams.size());

    }

    private static class AssertPDSProductParameterDefinition {
        private PDSProductParameterDefinition definition;

        private AssertPDSProductParameterDefinition(PDSProductParameterDefinition definition) {
            this.definition = definition;
        }

        public void hasDefault(String value) {
            assertEquals(value, definition.getDefault());
        }
    }

    private static AssertPDSProductParameterDefinition assertDefinition(String key, List<PDSProductParameterDefinition> list) {
        for (PDSProductParameterDefinition definition : list) {
            if (definition.getKey().equals(key)) {
                return new AssertPDSProductParameterDefinition(definition);
            }
        }

        fail("Key:" + key + " not found inside list!");
        return null;
    }

}
