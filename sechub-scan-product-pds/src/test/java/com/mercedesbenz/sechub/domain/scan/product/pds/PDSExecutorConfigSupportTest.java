// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;
import com.mercedesbenz.sechub.domain.scan.config.ScanMapping;
import com.mercedesbenz.sechub.domain.scan.config.ScanMappingRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class PDSExecutorConfigSupportTest {

    private static final String VALUE1B = "value1b";
    private static final String VALUE1A = "value1a";
    private static final String VALUE2A = "value2a";
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
    private ScanMappingRepository repository;

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

        SystemEnvironmentVariableSupport systemEnvironmentVariableSupport = mock(SystemEnvironmentVariableSupport.class);
        repository = mock(ScanMappingRepository.class);

        when(serviceCollection.getScanMappingRepository()).thenReturn(repository);
        when(serviceCollection.getSystemEnvironmentVariableSupport()).thenReturn(systemEnvironmentVariableSupport);
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
        mockSecHubMappingInDatabase();
        mockSecHubMappingId2InDatabase();

        SecHubConfiguration sechubConfiguration = new SecHubConfiguration();

        /* execute */
        Map<String, String> parameters = supportToTest.createJobParametersToSendToPDS(sechubConfiguration);

        /* test 1 */
        String p1 = parameters.get(SECHUB_MAPPING_ID_1);
        assertNotNull(p1, SECHUB_MAPPING_ID_1 + " was not found!");

        MappingData data1 = JSONConverter.get().fromJSON(MappingData.class, p1);

        boolean foundEntry1a = false;
        boolean foundEntry1b = false;

        for (MappingEntry entry : data1.getEntries()) {
            if (entry.getPattern().equals(PATTERN1A)) {
                assertEquals(VALUE1A, entry.getReplacement());
                foundEntry1a = true;
            } else if (entry.getPattern().equals(PATTERN1B)) {
                assertEquals(VALUE1B, entry.getReplacement());
                foundEntry1b = true;
            }
        }
        assertTrue(foundEntry1a, "entry 1a not found!");
        assertTrue(foundEntry1b, "entry 1b not found!");

        /* test 2 */
        String p2 = parameters.get(SECHUB_MAPPING_ID_2);
        assertNotNull(p2, SECHUB_MAPPING_ID_2 + " was not found!");
        MappingData data2 = JSONConverter.get().fromJSON(MappingData.class, p2);

        boolean foundEntry2a = false;
        for (MappingEntry entry : data2.getEntries()) {
            if (entry.getPattern().equals(PATTERN2A)) {
                assertEquals(VALUE2A, entry.getReplacement());
                foundEntry2a = true;
            }
        }
        assertTrue(foundEntry2a, "entry 2a not found!");

        /* test 3 - the unknown mapping is injected as empty variant */
        String p3 = parameters.get(SECHUB_MAPPING_ID_3_NOT_KNOWN_BY_SECHUB);
        assertNotNull(p3, SECHUB_MAPPING_ID_3_NOT_KNOWN_BY_SECHUB + " was not found!");
        MappingData data3 = JSONConverter.get().fromJSON(MappingData.class, p3);

        assertEquals(0, data3.getEntries().size());

    }

    private void mockSecHubMappingId2InDatabase() {
        String mappingId = SECHUB_MAPPING_ID_2;

        ScanMapping storedMapping2 = new ScanMapping(mappingId);
        MappingData storedData2 = new MappingData();
        storedData2.getEntries().add(new MappingEntry(PATTERN2A, VALUE2A, "comment 2a"));
        storedMapping2.setData(storedData2.toJSON());

        Optional<ScanMapping> optMapping2 = Optional.of(storedMapping2);
        when(repository.findById(mappingId)).thenReturn(optMapping2);
    }

    private void mockSecHubMappingInDatabase() {
        String mappingId = SECHUB_MAPPING_ID_1;

        ScanMapping storedMapping1 = new ScanMapping(mappingId);
        MappingData storedData1 = new MappingData();
        storedData1.getEntries().add(new MappingEntry(PATTERN1A, VALUE1A, "comment 1a"));
        storedData1.getEntries().add(new MappingEntry(PATTERN1B, VALUE1B, "comment 1b"));
        storedMapping1.setData(storedData1.toJSON());

        Optional<ScanMapping> optMapping1 = Optional.of(storedMapping1);
        when(repository.findById(mappingId)).thenReturn(optMapping1);
    }

}
