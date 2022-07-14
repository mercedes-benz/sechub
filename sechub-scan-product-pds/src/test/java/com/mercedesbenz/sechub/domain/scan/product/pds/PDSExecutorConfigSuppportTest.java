// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.mapping.NamePatternToIdEntry;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.mercedesbenz.sechub.commons.pds.PDSMappingJobParameterData;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;
import com.mercedesbenz.sechub.domain.scan.config.ScanMappingConfigurationService;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.mercedesbenz.sechub.sharedkernel.SystemEnvironment;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class PDSExecutorConfigSuppportTest {

    private static final String PATTERN2A = "pattern2a";
    private static final String PATTERN1B = "pattern1b";
    private static final String PATTERN1A = "pattern1a";
    private static final String SECHUB_MAPPING_ID_1 = "the.key1";
    private static final String SECHUB_MAPPING_ID_2 = "the.key2";
    private static final String SECHUB_MAPPING_ID_3_NOT_KNOWN_BY_SECHUB = "the.key3.not.known.by.sechub";

    private static final String CONFIGURED_PDS_PRODUCT_IDENTIFIER = "a_string";
    private PDSExecutorConfigSuppport supportToTest;
    private ProductExecutorConfig config;
    private ProductExecutorConfigSetup executorConfigSetup;
    private ProductExecutorConfigSetupCredentials credentialsInConfigSetup;
    private PDSExecutorConfigSuppportServiceCollection serviceCollection;
    private List<ProductExecutorConfigSetupJobParameter> jobParameters;
    private ScanMappingConfigurationService mappingConfigurationService;

    @BeforeEach
    public void before() throws Exception {
        config = mock(ProductExecutorConfig.class);
        executorConfigSetup = mock(ProductExecutorConfigSetup.class);

        jobParameters = new ArrayList<>();
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_PRODUCTIDENTIFIER.getKey().getId(),
                CONFIGURED_PDS_PRODUCT_IDENTIFIER));
        jobParameters
                .add(new ProductExecutorConfigSetupJobParameter(SecHubProductExecutionPDSKeyProvider.PDS_FORBIDS_TARGETTYPE_INTERNET.getKey().getId(), "true"));
        jobParameters.add(
                new ProductExecutorConfigSetupJobParameter(SecHubProductExecutionPDSKeyProvider.PDS_FORBIDS_TARGETTYPE_INTRANET.getKey().getId(), "false"));

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_USE_SECHUB_MAPPINGS.getKey().getId(),
                SECHUB_MAPPING_ID_1 + "," + SECHUB_MAPPING_ID_2 + " , " + SECHUB_MAPPING_ID_3_NOT_KNOWN_BY_SECHUB));

        when(config.getSetup()).thenReturn(executorConfigSetup);
        credentialsInConfigSetup = new ProductExecutorConfigSetupCredentials();
        when(executorConfigSetup.getCredentials()).thenReturn(credentialsInConfigSetup);

        when(executorConfigSetup.getJobParameters()).thenReturn(jobParameters);

        serviceCollection = mock(PDSExecutorConfigSuppportServiceCollection.class);

        SystemEnvironment systemEnvironment = mock(SystemEnvironment.class);
        mappingConfigurationService = mock(ScanMappingConfigurationService.class);

        when(serviceCollection.getMappingConfigurationService()).thenReturn(mappingConfigurationService);
        when(serviceCollection.getSystemEnvironment()).thenReturn(systemEnvironment);
        supportToTest = PDSExecutorConfigSuppport.createSupportAndAssertConfigValid(config, serviceCollection);
    }

    @Test
    void getPDSProductIdentifier_returns_configured_value() {
        assertEquals(CONFIGURED_PDS_PRODUCT_IDENTIFIER, supportToTest.getPDSProductIdentifier());
    }

    @Test
    void isTargetTypeForbidden_returns_true_for_target_type_requested_is_internet_when_internet_is_forbidden_in_configuration() {
        assertEquals(true, supportToTest.isTargetTypeForbidden(NetworkTargetType.INTERNET));
    }

    @Test
    void isTargetTypeForbidden_returns_false_for_target_type_requested_is_intranet_when_internet_is_forbidden_in_configuration() {
        assertEquals(false, supportToTest.isTargetTypeForbidden(NetworkTargetType.INTRANET));
    }

    @Test
    void createJobParametersToSendToPDS_mapping_is_resolved() {
        /* prepare */
        List<NamePatternToIdEntry> key1List = new ArrayList<>();
        key1List.add(new NamePatternToIdEntry(PATTERN1A, "value1a"));
        key1List.add(new NamePatternToIdEntry(PATTERN1B, "value1b"));
        when(mappingConfigurationService.getNamePatternToIdEntriesOrNull(SECHUB_MAPPING_ID_1)).thenReturn(key1List);

        List<NamePatternToIdEntry> key2List = new ArrayList<>();
        key2List.add(new NamePatternToIdEntry(PATTERN2A, "value2a"));
        when(mappingConfigurationService.getNamePatternToIdEntriesOrNull(SECHUB_MAPPING_ID_2)).thenReturn(key2List);

        SecHubConfiguration sechubConfiguration = new SecHubConfiguration();

        /* execute */
        Map<String, String> parameters = supportToTest.createJobParametersToSendToPDS(sechubConfiguration);

        /* test 1 */
        String p1 = parameters.get(SECHUB_MAPPING_ID_1);
        assertNotNull(p1, SECHUB_MAPPING_ID_1 + " was not found!");
        PDSMappingJobParameterData data1 = JSONConverter.get().fromJSON(PDSMappingJobParameterData.class, p1);

        assertEquals(SECHUB_MAPPING_ID_1, data1.getMappingId());

        boolean foundEntry1a = false;
        boolean foundEntry1b = false;

        for (NamePatternToIdEntry entry : data1.getEntries()) {
            if (entry.getNamePattern().equals(PATTERN1A)) {
                assertEquals("value1a", entry.getId());
                foundEntry1a = true;
            } else if (entry.getNamePattern().equals(PATTERN1B)) {
                assertEquals("value1b", entry.getId());
                foundEntry1b = true;
            }
        }
        assertTrue(foundEntry1a, "entry 1a not found!");
        assertTrue(foundEntry1b, "entry 1b not found!");

        /* test 2 */
        String p2 = parameters.get(SECHUB_MAPPING_ID_2);
        assertNotNull(p2, SECHUB_MAPPING_ID_2 + " was not found!");
        PDSMappingJobParameterData data2 = JSONConverter.get().fromJSON(PDSMappingJobParameterData.class, p2);

        assertEquals(SECHUB_MAPPING_ID_2, data2.getMappingId());
        boolean foundEntry2a = false;
        for (NamePatternToIdEntry entry : data2.getEntries()) {
            if (entry.getNamePattern().equals(PATTERN2A)) {
                assertEquals("value2a", entry.getId());
                foundEntry2a = true;
            }
        }
        assertTrue(foundEntry2a, "entry 2a not found!");

        /* test 3 - the unknown mapping is injected as empty variant */
        String p3 = parameters.get(SECHUB_MAPPING_ID_3_NOT_KNOWN_BY_SECHUB);
        assertNotNull(p3, SECHUB_MAPPING_ID_3_NOT_KNOWN_BY_SECHUB + " was not found!");
        PDSMappingJobParameterData data3 = JSONConverter.get().fromJSON(PDSMappingJobParameterData.class, p3);

        assertEquals(SECHUB_MAPPING_ID_3_NOT_KNOWN_BY_SECHUB, data3.getMappingId());
        assertEquals(0, data3.getEntries().size());

    }

}
