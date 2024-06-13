// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static com.mercedesbenz.sechub.commons.pds.PDSLauncherScriptEnvironmentConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;
import com.mercedesbenz.sechub.pds.job.WorkspaceLocationData;

class PDSExecutionEnvironmentServiceTest {

    private static final String PRODUCT_ID_1 = "product_1";

    private PDSExecutionEnvironmentService serviceToTest;
    private PDSServerConfigurationService serverConfigService;
    private PDSKeyToEnvConverter converter;
    private PDSScriptEnvironmentCleaner environmentCleaner;
    private UUID pdsJobUUID;
    private ProcessBuilder processBuilder;
    private Map<String, String> environmentMap;
    private PDSWorkspaceService workspaceService;
    private PDSProductSetup productSetup1;

    private PDSJobConfiguration configForProduct1;

    private WorkspaceLocationData locationData;

    private UUID sechubJobUUID;

    @BeforeEach
    void beforeEach() throws Exception {
        converter = mock(PDSKeyToEnvConverter.class);
        serverConfigService = mock(PDSServerConfigurationService.class);
        environmentCleaner = mock(PDSScriptEnvironmentCleaner.class);
        workspaceService = mock(PDSWorkspaceService.class);
        locationData = mock(WorkspaceLocationData.class);

        serviceToTest = new PDSExecutionEnvironmentService();
        serviceToTest.converter = converter;
        serviceToTest.serverConfigService = serverConfigService;
        serviceToTest.environmentCleaner = environmentCleaner;
        serviceToTest.workspaceService = workspaceService;

        pdsJobUUID = UUID.randomUUID();
        sechubJobUUID = UUID.randomUUID();
        processBuilder = mock(ProcessBuilder.class);
        environmentMap = new HashMap<>();

        productSetup1 = new PDSProductSetup();
        productSetup1.setId(PRODUCT_ID_1);

        when(serverConfigService.getProductSetupOrNull(PRODUCT_ID_1)).thenReturn(productSetup1);
        when(processBuilder.environment()).thenReturn(environmentMap);
        when(workspaceService.createLocationData(pdsJobUUID)).thenReturn(locationData);

        configForProduct1 = new PDSJobConfiguration();
        configForProduct1.setProductId(PRODUCT_ID_1);
        configForProduct1.setSechubJobUUID(sechubJobUUID);
    }

    @Test
    void sechub_job_is_injected_as_environment_variable_SECHUB_JOB_UUID() {
        /* execute */
        serviceToTest.initProcessBuilderEnvironmentMap(pdsJobUUID, configForProduct1, processBuilder);

        /* test */
        assertEquals(sechubJobUUID.toString(), environmentMap.get("SECHUB_JOB_UUID"));
    }

    @Test
    void when_no_sechub_job_available_environment_variable_SECHUB_JOB_UUID_is_empty() {
        /* prepare */
        configForProduct1.setSechubJobUUID(null);

        /* execute */
        serviceToTest.initProcessBuilderEnvironmentMap(pdsJobUUID, configForProduct1, processBuilder);

        /* test */
        assertEquals("", environmentMap.get("SECHUB_JOB_UUID"));
    }

    @Test
    void even_for_an_empty_product_setup_execution_environment_service_accepts_default_parameter_key_target_url() {
        /* prepare */
        PDSExecutionParameterEntry entry1 = new PDSExecutionParameterEntry();
        entry1.setKey(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_URL);
        entry1.setValue("https://testurl.example.com/app1");

        configForProduct1.getParameters().add(entry1);

        // fake key conversion
        when(converter.convertKeyToEnv(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_URL)).thenReturn("PDS_SCAN_TARGET_URL");

        /* execute */
        serviceToTest.initProcessBuilderEnvironmentMap(pdsJobUUID, configForProduct1, processBuilder);

        /* test */
        assertEquals("https://testurl.example.com/app1", environmentMap.get("PDS_SCAN_TARGET_URL"));
    }

    @Test
    void a_job_with_two_configured_keys_is_is_handling_them_but_third_one_is_ignored() {
        /* prepare */
        // create job configuration
        PDSExecutionParameterEntry entry1 = new PDSExecutionParameterEntry();
        entry1.setKey("p1.keya");
        entry1.setValue("value1");
        PDSExecutionParameterEntry entry2 = new PDSExecutionParameterEntry();
        entry2.setKey("p1.keyb");
        entry2.setValue("value2");

        configForProduct1.getParameters().add(entry1);
        configForProduct1.getParameters().add(entry2);

        // create product setup configuration
        PDSProductParameterDefinition def1 = new PDSProductParameterDefinition();
        def1.setKey("p1.keya");
        PDSProductParameterDefinition def2 = new PDSProductParameterDefinition();
        def2.setKey("p1.keyb");

        productSetup1.getParameters().getMandatory().add(def1);
        productSetup1.getParameters().getOptional().add(def2);

        // fake key conversion
        when(converter.convertKeyToEnv("p1.keya")).thenReturn("KEY_A");
        when(converter.convertKeyToEnv("p1.keyb")).thenReturn("KEY_B");

        /* execute */
        serviceToTest.initProcessBuilderEnvironmentMap(pdsJobUUID, configForProduct1, processBuilder);

        /* test */
        assertEquals("value1", environmentMap.get("KEY_A"));
        assertEquals("value2", environmentMap.get("KEY_B"));
        assertEquals(null, environmentMap.get("KEY_C"));

    }

    @Test
    void a_mandatory_parameter_with_default_will_have_default_in_environmen_when_not_set() {
        /* prepare */
        // create job configuration
        PDSExecutionParameterEntry entry1 = new PDSExecutionParameterEntry();
        entry1.setKey("p1.keya");
        entry1.setValue("value1");
        configForProduct1.getParameters().add(entry1);

        // create product setup configuration
        PDSProductParameterDefinition def1 = new PDSProductParameterDefinition();
        def1.setKey("p1.keya");
        def1.setDefault("p1.defaulta");

        PDSProductParameterDefinition def2 = new PDSProductParameterDefinition();
        def2.setKey("p1.keyb");
        def2.setDefault("p1.defaultb");

        List<PDSProductParameterDefinition> mandatoryList = productSetup1.getParameters().getMandatory();
        mandatoryList.add(def1);
        mandatoryList.add(def2);

        // fake key conversion
        when(converter.convertKeyToEnv("p1.keya")).thenReturn("KEY_A");
        when(converter.convertKeyToEnv("p1.keyb")).thenReturn("KEY_B");

        /* execute */
        serviceToTest.initProcessBuilderEnvironmentMap(pdsJobUUID, configForProduct1, processBuilder);

        /* test */
        assertEquals("value1", environmentMap.get("KEY_A")); // value set, default is ignored
        assertEquals("p1.defaultb", environmentMap.get("KEY_B")); // default used because no value set
        assertEquals(null, environmentMap.get("KEY_UNKNOWN"));

    }

    @Test
    void an_optional_parameter_with_default_will_have_default_in_environmen_when_not_set() {
        /* prepare */
        // create job configuration
        PDSJobConfiguration config = new PDSJobConfiguration();
        config.setProductId(PRODUCT_ID_1);
        PDSExecutionParameterEntry entry1 = new PDSExecutionParameterEntry();
        entry1.setKey("p1.keya");
        entry1.setValue("value1");

        config.getParameters().add(entry1);

        // create product setup configuration
        PDSProductParameterDefinition def1 = new PDSProductParameterDefinition();
        def1.setKey("p1.keya");
        def1.setDefault("p1.defaulta");

        PDSProductParameterDefinition def2 = new PDSProductParameterDefinition();
        def2.setKey("p1.keyb");
        def2.setDefault("p1.defaultb");

        List<PDSProductParameterDefinition> optionalList = productSetup1.getParameters().getOptional();
        optionalList.add(def1);
        optionalList.add(def2);

        // fake key conversion
        when(converter.convertKeyToEnv("p1.keya")).thenReturn("KEY_A");
        when(converter.convertKeyToEnv("p1.keyb")).thenReturn("KEY_B");

        /* execute */
        serviceToTest.initProcessBuilderEnvironmentMap(pdsJobUUID, config, processBuilder);

        /* test */
        assertEquals("value1", environmentMap.get("KEY_A")); // value set, default is ignored
        assertEquals("p1.defaultb", environmentMap.get("KEY_B")); // default used because no value set
        assertEquals(null, environmentMap.get("KEY_UNKNOWN"));

    }

    @Test
    void removeAllNonWhitelistedEnvironmentVariables_calls_env_cleaner() {
        /* execute */
        serviceToTest.initProcessBuilderEnvironmentMap(pdsJobUUID, configForProduct1, processBuilder);

        /* test */
        verify(environmentCleaner).clean(environmentMap, Collections.emptySet());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "a,b,c", "", "a" })
    void env_cleaner_is_called_with_config_whitelist_entries(String whiteListCommaSeparated) {
        /* prepare */
        Set<String> whiteList = whiteListCommaSeparated == null ? null : Set.of(whiteListCommaSeparated.split(","));
        productSetup1.setEnvWhitelist(whiteList);

        /* execute */
        serviceToTest.initProcessBuilderEnvironmentMap(pdsJobUUID, configForProduct1, processBuilder);

        /* test */
        verify(environmentCleaner).clean(environmentMap, whiteList);
    }

    @Test
    void calculated_environmet_variables_are_available_and_correct() {
        /* prepare */
        String workspaceLocation = "workspace_location_" + System.currentTimeMillis();
        String resultFile = "result_file_" + System.currentTimeMillis();
        String userMessageLocation = "user_message_location_" + System.currentTimeMillis();
        String eventsFolder = "events_folder_" + System.currentTimeMillis();
        boolean jobHasExtractedSources = true;
        boolean jobHasExtractedBinaries = false;
        String extractedSourcesFolder = "extracted_sources_folder_" + System.currentTimeMillis();
        String extractedBinariesFolder = "extracted_binaries_folder_" + System.currentTimeMillis();
        String binariesTarFileLocation = "binaries_tar_file_location" + System.currentTimeMillis();
        String zipFileLocation = "zip_file_location" + System.currentTimeMillis();
        String metaDataFileLocation = "meta_data_file_location_" + System.currentTimeMillis();

        when(locationData.getWorkspaceLocation()).thenReturn(workspaceLocation);
        when(locationData.getResultFileLocation()).thenReturn(resultFile);
        when(locationData.getUserMessagesLocation()).thenReturn(userMessageLocation);
        when(locationData.getEventsLocation()).thenReturn(eventsFolder);

        when(locationData.getExtractedSourcesLocation()).thenReturn(extractedSourcesFolder);
        when(locationData.getExtractedBinariesLocation()).thenReturn(extractedBinariesFolder);
        when(locationData.getBinariesTarFileLocation()).thenReturn(binariesTarFileLocation);
        when(locationData.getSourceCodeZipFileLocation()).thenReturn(zipFileLocation);
        when(locationData.getMetaDataFileLocation()).thenReturn(metaDataFileLocation);

        when(workspaceService.hasExtractedSources(pdsJobUUID)).thenReturn(jobHasExtractedSources);
        when(workspaceService.hasExtractedBinaries(pdsJobUUID)).thenReturn(jobHasExtractedBinaries);

        /* execute */
        serviceToTest.initProcessBuilderEnvironmentMap(pdsJobUUID, configForProduct1, processBuilder);

        /* test */
        assertEquals(workspaceLocation, environmentMap.get(PDS_JOB_WORKSPACE_LOCATION));
        assertEquals(resultFile, environmentMap.get(PDS_JOB_RESULT_FILE));
        assertEquals(userMessageLocation, environmentMap.get(PDS_JOB_USER_MESSAGES_FOLDER));
        assertEquals(eventsFolder, environmentMap.get(PDS_JOB_EVENTS_FOLDER));

        assertEquals(extractedSourcesFolder, environmentMap.get(PDS_JOB_EXTRACTED_SOURCES_FOLDER));
        assertEquals(extractedBinariesFolder, environmentMap.get(PDS_JOB_EXTRACTED_BINARIES_FOLDER));

        assertEquals(binariesTarFileLocation, environmentMap.get(PDS_JOB_BINARIES_TAR_FILE));
        assertEquals(zipFileLocation, environmentMap.get(PDS_JOB_SOURCECODE_ZIP_FILE));

        assertEquals(String.valueOf(jobHasExtractedSources), environmentMap.get(PDS_JOB_HAS_EXTRACTED_SOURCES));
        assertEquals(String.valueOf(jobHasExtractedBinaries), environmentMap.get(PDS_JOB_HAS_EXTRACTED_BINARIES));

        assertEquals(metaDataFileLocation, environmentMap.get(PDS_JOB_METADATA_FILE));
        assertEquals(pdsJobUUID.toString(), environmentMap.get(PDS_JOB_UUID));
    }

}
