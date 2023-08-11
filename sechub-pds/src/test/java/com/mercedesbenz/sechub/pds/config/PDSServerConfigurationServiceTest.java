// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterValueConstants;
import com.mercedesbenz.sechub.pds.PDSShutdownService;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;

public class PDSServerConfigurationServiceTest {

    private PDSServerConfigurationService serviceToTest;
    private PDSServerConfigurationValidator serverConfigurationValidator;

    private PDSShutdownService shutdownService;
    private PDSConfigurationAutoFix serverConfigurationAutoFix;

    @BeforeEach
    public void before() throws Exception {

        serverConfigurationValidator = mock(PDSServerConfigurationValidator.class);
        serverConfigurationAutoFix = mock(PDSConfigurationAutoFix.class);

        shutdownService = mock(PDSShutdownService.class);

        serviceToTest = new PDSServerConfigurationService();
        serviceToTest.serverConfigurationValidator = serverConfigurationValidator;
        serviceToTest.serverConfigurationAutoFix = serverConfigurationAutoFix;
        serviceToTest.shutdownService = shutdownService;
    }

    @Test
    void getParameterOrNull_returns_mandatory_default_value_when_available() {

        /* prepare */
        PDSProductParameterDefinition parameterDefinition = new PDSProductParameterDefinition();
        parameterDefinition.setKey("key");
        parameterDefinition.setDefault("mandatory-default");

        PDSServerConfiguration configuration = new PDSServerConfiguration();
        serviceToTest.configuration = configuration;

        PDSProductSetup productSetup = new PDSProductSetup();
        productSetup.setId("productid");
        configuration.getProducts().add(productSetup);

        PDSProductParameterSetup parameters = productSetup.getParameters();
        List<PDSProductParameterDefinition> mandatories = parameters.getMandatory();

        mandatories.add(parameterDefinition);

        /* execute */
        String result = serviceToTest.getProductParameterDefaultValueOrNull("productid", "key");

        /* test */
        assertEquals("mandatory-default", result);

    }

    @Test
    void getParameterOrNull_returns_optional_default_value_when_available() {

        /* prepare */
        PDSProductParameterDefinition parameterDefinition = new PDSProductParameterDefinition();
        parameterDefinition.setKey("key");
        parameterDefinition.setDefault("optional-default");

        PDSServerConfiguration configuration = new PDSServerConfiguration();
        serviceToTest.configuration = configuration;

        PDSProductSetup productSetup = new PDSProductSetup();
        productSetup.setId("productid");
        configuration.getProducts().add(productSetup);

        PDSProductParameterSetup parameters = productSetup.getParameters();
        List<PDSProductParameterDefinition> optionals = parameters.getOptional();

        optionals.add(parameterDefinition);

        /* execute */
        String result = serviceToTest.getProductParameterDefaultValueOrNull("productid", "key");

        /* test */
        assertEquals("optional-default", result);

    }

    @Test
    void getParameterOrNull_returns_mandatory_default_value_when_optional_and_mandatory_defaults_available() {

        /* prepare */
        PDSProductParameterDefinition optionalParameterDefinition = new PDSProductParameterDefinition();
        optionalParameterDefinition.setKey("key");
        optionalParameterDefinition.setDefault("optional-default");

        PDSProductParameterDefinition mandatoryParameterDefinition = new PDSProductParameterDefinition();
        mandatoryParameterDefinition.setKey("key");
        mandatoryParameterDefinition.setDefault("mandatory-default");

        PDSServerConfiguration configuration = new PDSServerConfiguration();
        serviceToTest.configuration = configuration;

        PDSProductSetup productSetup = new PDSProductSetup();
        productSetup.setId("productid");
        configuration.getProducts().add(productSetup);

        PDSProductParameterSetup parameters = productSetup.getParameters();
        List<PDSProductParameterDefinition> optionals = parameters.getOptional();
        List<PDSProductParameterDefinition> mandatories = parameters.getMandatory();

        optionals.add(optionalParameterDefinition);
        mandatories.add(mandatoryParameterDefinition);

        /* execute */
        String result = serviceToTest.getProductParameterDefaultValueOrNull("productid", "key");

        /* test */
        assertEquals("mandatory-default", result);

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    void when_minutesToWaitForProduct_is_lower_than_1_systemwide_productTimeOutInMinutes_is_1_after_postconstruct(int configuredMinutes) {
        /* prepare */
        serviceToTest.pathToConfigFile = "./src/test/resources/config/pds-config-example1.json";
        serviceToTest.minutesToWaitForProduct = configuredMinutes;

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        assertEquals(1, serviceToTest.getMinutesToWaitForProduct());
    }

    @ParameterizedTest
    @ValueSource(ints = { PDSDefaultParameterValueConstants.MAXIMUM_CONFIGURABLE_TIME_TO_WAIT_FOR_PRODUCT_IN_MINUTES + 1,
            PDSDefaultParameterValueConstants.MAXIMUM_CONFIGURABLE_TIME_TO_WAIT_FOR_PRODUCT_IN_MINUTES + 2000 })
    void when_minutesToWaitForProduct_is_greater_than_max_value_systemwide_productTimeOutInMinutes_is_default_maxvalue_after_postconstruct(
            int configuredMinutes) {
        /* prepare */
        serviceToTest.pathToConfigFile = "./src/test/resources/config/pds-config-example1.json";
        serviceToTest.minutesToWaitForProduct = configuredMinutes;

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        assertEquals(PDSDefaultParameterValueConstants.MAXIMUM_CONFIGURABLE_TIME_TO_WAIT_FOR_PRODUCT_IN_MINUTES, serviceToTest.getMinutesToWaitForProduct());
    }

    @ParameterizedTest
    @ValueSource(ints = { 3, 4, 10 })
    void when_minutesToWaitForProduct_is_greater_than_custom_max_value_2_systemwide_productTimeOutInMinutes_is_custom_maxvalue_after_postconstruct(
            int configuredMinutes) {
        /* prepare */
        serviceToTest.pathToConfigFile = "./src/test/resources/config/pds-config-example1.json";
        serviceToTest.minutesToWaitForProduct = configuredMinutes;
        serviceToTest.maximumConfigurableMinutesToWaitForProduct = 2;

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        assertEquals(2, serviceToTest.getMinutesToWaitForProduct());
        ;
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 3000, PDSDefaultParameterValueConstants.MAXIMUM_CONFIGURABLE_TIME_TO_WAIT_FOR_PRODUCT_IN_MINUTES, })
    void when_minutesToWaitForProduct_is_in_range_between_min_and_max_the_value_is_systemwide_productTimeOutInMinutes_after_postconstruct(
            int configuredMinutes) {
        /* prepare */
        serviceToTest.pathToConfigFile = "./src/test/resources/config/pds-config-example1.json";
        serviceToTest.minutesToWaitForProduct = configuredMinutes;

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        assertEquals(configuredMinutes, serviceToTest.getMinutesToWaitForProduct());
    }

    @Test
    void pds_config_example1_can_be_loaded_and_contains_expected_data() {
        /* prepare */
        serviceToTest.pathToConfigFile = "./src/test/resources/config/pds-config-example1.json";

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        PDSServerConfiguration serverConfiguration = serviceToTest.getServerConfiguration();
        assertNotNull(serverConfiguration);

        List<PDSProductSetup> products = serverConfiguration.getProducts();
        assertEquals(2, products.size());
        Iterator<PDSProductSetup> it = products.iterator();

        PDSProductSetup product1 = it.next();
        assertEquals("PRODUCT_1", product1.getId());
        assertEquals("/srv/security/scanner1.sh", product1.getPath());
        assertEquals(ScanType.CODE_SCAN, product1.getScanType());
        assertEquals("codescanner script needs environment variable ENV_CODESCAN_LEVEL set containing 1,2,3", product1.getDescription());

        PDSProductParameterSetup paramSetup = product1.getParameters();
        assertNotNull(paramSetup);
        List<PDSProductParameterDefinition> mandatory = paramSetup.getMandatory();
        assertEquals(2, mandatory.size());
        Iterator<PDSProductParameterDefinition> mit = mandatory.iterator();
        PDSProductParameterDefinition m1 = mit.next();
        PDSProductParameterDefinition m2 = mit.next();

        assertEquals("product1.qualititycheck.enabled", m1.getKey());
        assertEquals("when 'true' quality scan results are added as well", m1.getDescription());
        assertEquals("product1.level", m2.getKey());
        assertEquals("numeric, 1-gets all, 2-only critical,fatal and medium, 3- only critical and fatal", m2.getDescription());
        assertEquals("1", m2.getDefault());

        List<PDSProductParameterDefinition> optional = paramSetup.getOptional();
        assertEquals(1, optional.size());
        Iterator<PDSProductParameterDefinition> oit = optional.iterator();
        PDSProductParameterDefinition o1 = oit.next();
        assertEquals("product1.add.tipoftheday", o1.getKey());
        assertEquals("add tip of the day as info", o1.getDescription());
        assertEquals("Don't add any secrets/credentials to your repository!", o1.getDefault());

        PDSProductSetup product2 = it.next();
        assertEquals("PRODUCT_2", product2.getId());
        assertEquals(ScanType.INFRA_SCAN, product2.getScanType());
        assertEquals("/srv/security/scanner2.sh", product2.getPath());

    }

    @Test
    public void before_config_validator_is_started_the_autofix_is_called() {
        /* prepare */
        serviceToTest.pathToConfigFile = "./src/test/resources/config/pds-config-example1.json";
        when(serverConfigurationValidator.createValidationErrorMessage(any())).thenReturn("reason");

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        verify(serverConfigurationAutoFix).autofixWhenNecessary(any());

    }

    void when_config_file_loaded_but_server_configuration_validator_returns_validator_error_message_shutdown_service_called() {
        /* prepare */
        serviceToTest.pathToConfigFile = "./src/test/resources/config/pds-config-example1.json";
        when(serverConfigurationValidator.createValidationErrorMessage(any())).thenReturn("reason");

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        verify(shutdownService).shutdownApplication();

    }

    @Test
    void when_config_file_loaded_and_server_configuration_validator_returns_NO_validator_error_message_shutdown_service_is_NOT_called() {
        /* prepare */
        serviceToTest.pathToConfigFile = "./src/test/resources/config/pds-config-example1.json";
        // no serverConfigurationValidator mock setup means null returned...

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        verify(shutdownService, never()).shutdownApplication();

    }

    @Test
    void when_config_file_NOT_exists_shutdown_service_is_called() {
        /* prepare */
        serviceToTest.pathToConfigFile = "./src/test/resources/config/pds-config-example-NOT_EXISTING.json";

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        verify(shutdownService).shutdownApplication();

    }

}
