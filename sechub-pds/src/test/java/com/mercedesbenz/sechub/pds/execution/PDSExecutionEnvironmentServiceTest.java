// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.job.PDSJobConfiguration;

class PDSExecutionEnvironmentServiceTest {

    private PDSExecutionEnvironmentService serviceToTest;
    private PDSServerConfigurationService serverConfigService;
    private PDSKeyToEnvConverter converter;
    private PDSScriptEnvironmentCleaner cleaner;

    @BeforeEach
    void beforeEach() throws Exception {
        converter = mock(PDSKeyToEnvConverter.class);
        serverConfigService = mock(PDSServerConfigurationService.class);
        cleaner = mock(PDSScriptEnvironmentCleaner.class);

        serviceToTest = new PDSExecutionEnvironmentService();
        serviceToTest.converter = converter;
        serviceToTest.serverConfigService = serverConfigService;
        serviceToTest.cleaner = cleaner;
    }

    @Test
    void sechub_job_is_injected_as_environment_variable_SECHUB_JOB_UUID() {
        /* prepare */
        PDSJobConfiguration config = new PDSJobConfiguration();

        UUID uuid = UUID.randomUUID();
        config.setSechubJobUUID(uuid);

        /* execute */
        Map<String, String> result = serviceToTest.buildEnvironmentMap(config);

        /* test */
        assertEquals(uuid.toString(), result.get("SECHUB_JOB_UUID"));
    }

    @Test
    void when_no_sechub_job_available_environment_variable_SECHUB_JOB_UUID_is_empty() {
        /* prepare */
        PDSJobConfiguration config = new PDSJobConfiguration();

        /* execute */
        Map<String, String> result = serviceToTest.buildEnvironmentMap(config);

        /* test */
        assertEquals("", result.get("SECHUB_JOB_UUID"));
    }

    @Test
    void even_for_an_empty_product_setup_execution_environment_service_accepts_default_parameter_key_target_url() {
        /* prepare */
        // create job configuration
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setProductId("productid1");
        PDSExecutionParameterEntry entry1 = new PDSExecutionParameterEntry();
        entry1.setKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_URL);
        entry1.setValue("https://testurl.example.com/app1");

        config.getParameters().add(entry1);

        // create product setup configuration
        PDSProductSetup setup = new PDSProductSetup();
        setup.setId("productid1");

        when(serverConfigService.getProductSetupOrNull("productid1")).thenReturn(setup);

        // fake key conversion
        when(converter.convertKeyToEnv(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_URL)).thenReturn("PDS_SCAN_TARGET_URL");

        /* execute */
        Map<String, String> result = serviceToTest.buildEnvironmentMap(config);

        /* test */
        assertEquals("https://testurl.example.com/app1", result.get("PDS_SCAN_TARGET_URL"));
    }

    @Test
    void a_job_with_two_configured_keys_is_is_handling_them_but_third_one_is_ignored() {
        /* prepare */
        // create job configuration
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setProductId("productid1");
        PDSExecutionParameterEntry entry1 = new PDSExecutionParameterEntry();
        entry1.setKey("p1.keya");
        entry1.setValue("value1");
        PDSExecutionParameterEntry entry2 = new PDSExecutionParameterEntry();
        entry2.setKey("p1.keyb");
        entry2.setValue("value2");

        config.getParameters().add(entry1);
        config.getParameters().add(entry2);

        // create product setup configuration
        PDSProductSetup setup = new PDSProductSetup();
        setup.setId("productid1");
        PDSProductParameterDefinition def1 = new PDSProductParameterDefinition();
        def1.setKey("p1.keya");
        PDSProductParameterDefinition def2 = new PDSProductParameterDefinition();
        def2.setKey("p1.keyb");

        setup.getParameters().getMandatory().add(def1);
        setup.getParameters().getOptional().add(def2);

        when(serverConfigService.getProductSetupOrNull("productid1")).thenReturn(setup);

        // fake key conversion
        when(converter.convertKeyToEnv("p1.keya")).thenReturn("KEY_A");
        when(converter.convertKeyToEnv("p1.keyb")).thenReturn("KEY_B");

        /* execute */
        Map<String, String> result = serviceToTest.buildEnvironmentMap(config);

        /* test */
        assertEquals("value1", result.get("KEY_A"));
        assertEquals("value2", result.get("KEY_B"));
        assertEquals(null, result.get("KEY_C"));

    }

    @Test
    void a_mandatory_parameter_with_default_will_have_default_in_environmen_when_not_set() {
        /* prepare */
        // create job configuration
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setProductId("productid1");
        PDSExecutionParameterEntry entry1 = new PDSExecutionParameterEntry();
        entry1.setKey("p1.keya");
        entry1.setValue("value1");

        config.getParameters().add(entry1);

        // create product setup configuration
        PDSProductSetup setup = new PDSProductSetup();
        setup.setId("productid1");
        PDSProductParameterDefinition def1 = new PDSProductParameterDefinition();
        def1.setKey("p1.keya");
        def1.setDefault("p1.defaulta");

        PDSProductParameterDefinition def2 = new PDSProductParameterDefinition();
        def2.setKey("p1.keyb");
        def2.setDefault("p1.defaultb");

        List<PDSProductParameterDefinition> mandatoryList = setup.getParameters().getMandatory();
        mandatoryList.add(def1);
        mandatoryList.add(def2);

        when(serverConfigService.getProductSetupOrNull("productid1")).thenReturn(setup);

        // fake key conversion
        when(converter.convertKeyToEnv("p1.keya")).thenReturn("KEY_A");
        when(converter.convertKeyToEnv("p1.keyb")).thenReturn("KEY_B");

        /* execute */
        Map<String, String> result = serviceToTest.buildEnvironmentMap(config);

        /* test */
        assertEquals("value1", result.get("KEY_A")); // value set, default is ignored
        assertEquals("p1.defaultb", result.get("KEY_B")); // default used because no value set
        assertEquals(null, result.get("KEY_UNKNOWN"));

    }

    @Test
    void an_optional_parameter_with_default_will_have_default_in_environmen_when_not_set() {
        /* prepare */
        // create job configuration
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setProductId("productid1");
        PDSExecutionParameterEntry entry1 = new PDSExecutionParameterEntry();
        entry1.setKey("p1.keya");
        entry1.setValue("value1");

        config.getParameters().add(entry1);

        // create product setup configuration
        PDSProductSetup setup = new PDSProductSetup();
        setup.setId("productid1");
        PDSProductParameterDefinition def1 = new PDSProductParameterDefinition();
        def1.setKey("p1.keya");
        def1.setDefault("p1.defaulta");

        PDSProductParameterDefinition def2 = new PDSProductParameterDefinition();
        def2.setKey("p1.keyb");
        def2.setDefault("p1.defaultb");

        List<PDSProductParameterDefinition> optionalList = setup.getParameters().getOptional();
        optionalList.add(def1);
        optionalList.add(def2);

        when(serverConfigService.getProductSetupOrNull("productid1")).thenReturn(setup);

        // fake key conversion
        when(converter.convertKeyToEnv("p1.keya")).thenReturn("KEY_A");
        when(converter.convertKeyToEnv("p1.keyb")).thenReturn("KEY_B");

        /* execute */
        Map<String, String> result = serviceToTest.buildEnvironmentMap(config);

        /* test */
        assertEquals("value1", result.get("KEY_A")); // value set, default is ignored
        assertEquals("p1.defaultb", result.get("KEY_B")); // default used because no value set
        assertEquals(null, result.get("KEY_UNKNOWN"));

    }

    @Test
    void removeAllNonWhitelistedEnvironmentVariables_calls_env_cleaner() {
        /* prepare */
        Map<String, String> map = Collections.emptyMap();

        /* execute */
        serviceToTest.removeAllNonWhitelistedEnvironmentVariables(map);

        /* test */
        verify(cleaner).clean(map);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "a,b,c", "", "a" })
    void env_cleaner_is_setup_with_spring_value_via_post_construct_method(String whiteList) {
        /* prepare */
        serviceToTest.pdsScriptEnvWhitelist = whiteList;

        /* execute */
        serviceToTest.postConstruct();

        /* test */
        verify(cleaner).setWhiteListCommaSeparated(whiteList);
    }

}
