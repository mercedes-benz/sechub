// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;

class PDSWorkspacePreparationContextFactoryTest {

    private static final String TEST_PRODUCT_ID = "test-product-id";

    private PDSWorkspacePreparationContextFactory factoryToTest;
    private SecHubConfigurationModelSupport modelSupport;
    private PDSServerConfigurationService serverConfigService;
    private PDSJobConfigurationSupport jobConfigurationSupport;
    private SecHubConfigurationModel sechubConfigurationModel;
    private PDSProductSetup productSetup;

    @BeforeEach
    void beforeEach() {

        modelSupport = mock(SecHubConfigurationModelSupport.class);
        serverConfigService = mock(PDSServerConfigurationService.class);
        jobConfigurationSupport = mock(PDSJobConfigurationSupport.class);
        productSetup = mock(PDSProductSetup.class);

        factoryToTest = new PDSWorkspacePreparationContextFactory();

        factoryToTest.modelSupport = modelSupport;
        factoryToTest.serverConfigService = serverConfigService;

        when(jobConfigurationSupport.getProductId()).thenReturn(TEST_PRODUCT_ID);
        sechubConfigurationModel = mock(SecHubConfigurationModel.class);
        when(serverConfigService.getProductSetupOrNull(TEST_PRODUCT_ID)).thenReturn(productSetup);
    }

    @Test
    void null_argument_throws_illegal_argument_exception() {
        assertThrows(IllegalArgumentException.class, () -> factoryToTest.createPreparationContext(null));
    }

    @Test
    void no_model_set__when_supported_datatypes_is_binary_and_source_but_not_none_preparation_context_is_similar() {

        /* prepare */
        Set<SecHubDataConfigurationType> set = new LinkedHashSet<>();
        set.add(SecHubDataConfigurationType.SOURCE);
        set.add(SecHubDataConfigurationType.BINARY);
        when(jobConfigurationSupport.getSupportedDataTypes(any())).thenReturn(set);
        when(jobConfigurationSupport.resolveSecHubConfigurationModel()).thenReturn(null);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        assertTrue(result.isBinaryAccepted());
        assertTrue(result.isSourceAccepted());
        assertFalse(result.isNoneAccepted());

        assertFalse(result.isExtractedSourceAvailable());
        assertFalse(result.isExtractedBinaryAvailable());

    }

    @Test
    void no_model_set__when_supported_datatypes_are_all_accepted_context_is_similar() {

        /* prepare */
        Set<SecHubDataConfigurationType> set = new LinkedHashSet<>();
        set.add(SecHubDataConfigurationType.SOURCE);
        set.add(SecHubDataConfigurationType.BINARY);
        set.add(SecHubDataConfigurationType.NONE);

        when(jobConfigurationSupport.getSupportedDataTypes(any())).thenReturn(set);
        when(jobConfigurationSupport.resolveSecHubConfigurationModel()).thenReturn(null);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        assertTrue(result.isBinaryAccepted());
        assertTrue(result.isSourceAccepted());
        assertTrue(result.isNoneAccepted());
    }

    @ParameterizedTest
    @EnumSource(SecHubDataConfigurationType.class)
    void no_model_set__when_supported_datatypes_is_only_one_type_the_created_context_is_for_this_type_only(SecHubDataConfigurationType type) {

        /* prepare */
        when(jobConfigurationSupport.getSupportedDataTypes(any())).thenReturn(Collections.singleton(type));
        when(jobConfigurationSupport.resolveSecHubConfigurationModel()).thenReturn(null);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        verify(modelSupport, never()).isBinaryRequired(any(), any());
        verify(modelSupport, never()).isSourceRequired(any(), any());

        assertEquals(type.equals(SecHubDataConfigurationType.BINARY), result.isBinaryAccepted());
        assertEquals(type.equals(SecHubDataConfigurationType.SOURCE), result.isSourceAccepted());
        assertEquals(type.equals(SecHubDataConfigurationType.NONE), result.isNoneAccepted());
    }

    @Test
    void model_set_no_binary_data_only_source_when_supported_datatypes_is_binary_and_source_only_source_accepted() {

        /* prepare */
        Set<SecHubDataConfigurationType> set = new LinkedHashSet<>();
        set.add(SecHubDataConfigurationType.SOURCE);
        set.add(SecHubDataConfigurationType.BINARY);

        when(jobConfigurationSupport.getSupportedDataTypes(any())).thenReturn(set);
        when(jobConfigurationSupport.resolveSecHubConfigurationModel()).thenReturn(sechubConfigurationModel);

        when(productSetup.getScanType()).thenReturn(ScanType.CODE_SCAN);
        when(modelSupport.isBinaryRequired(ScanType.CODE_SCAN, sechubConfigurationModel)).thenReturn(false);
        when(modelSupport.isSourceRequired(ScanType.CODE_SCAN, sechubConfigurationModel)).thenReturn(true);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        verify(modelSupport).isBinaryRequired(ScanType.CODE_SCAN, sechubConfigurationModel);
        verify(modelSupport).isSourceRequired(ScanType.CODE_SCAN, sechubConfigurationModel);

        assertFalse(result.isBinaryAccepted());
        assertTrue(result.isSourceAccepted());
        assertFalse(result.isNoneAccepted());
    }

    @Test
    void jobConfigurationSupport_getSupportedDataTypes_is_called_with_defaultValue_from_productSetup() {

        /* prepare */
        when(serverConfigService.getProductParameterDefaultValueOrNull(TEST_PRODUCT_ID,
                PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES)).thenReturn("something-from-product");

        /* execute */
        factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        verify(jobConfigurationSupport).getSupportedDataTypes("something-from-product");
    }

    @Test
    void model_set_binary_data_but_no_source_when_supported_datatypes_is_binary_and_source_only_binary_accepted() {

        /* prepare */
        Set<SecHubDataConfigurationType> set = new LinkedHashSet<>();
        set.add(SecHubDataConfigurationType.SOURCE);
        set.add(SecHubDataConfigurationType.BINARY);

        when(jobConfigurationSupport.getSupportedDataTypes(any())).thenReturn(set);
        when(jobConfigurationSupport.resolveSecHubConfigurationModel()).thenReturn(sechubConfigurationModel);

        when(productSetup.getScanType()).thenReturn(ScanType.CODE_SCAN);
        when(modelSupport.isBinaryRequired(ScanType.CODE_SCAN, sechubConfigurationModel)).thenReturn(true);
        when(modelSupport.isSourceRequired(ScanType.CODE_SCAN, sechubConfigurationModel)).thenReturn(false);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        verify(modelSupport).isBinaryRequired(ScanType.CODE_SCAN, sechubConfigurationModel);
        verify(modelSupport).isSourceRequired(ScanType.CODE_SCAN, sechubConfigurationModel);

        assertTrue(result.isBinaryAccepted());
        assertFalse(result.isSourceAccepted());
        assertFalse(result.isNoneAccepted());
    }

    @Test
    void model_set_binary_data_and_source_when_supported_datatypes_is_only_none_only_none_is_accepted() {

        /* prepare */
        when(jobConfigurationSupport.getSupportedDataTypes(any())).thenReturn(Collections.singleton(SecHubDataConfigurationType.NONE));
        when(jobConfigurationSupport.resolveSecHubConfigurationModel()).thenReturn(sechubConfigurationModel);

        when(productSetup.getScanType()).thenReturn(ScanType.CODE_SCAN);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        verify(modelSupport, never()).isBinaryRequired(any(), any());
        verify(modelSupport, never()).isSourceRequired(any(), any());

        assertFalse(result.isBinaryAccepted());
        assertFalse(result.isSourceAccepted());
        assertTrue(result.isNoneAccepted());
    }

    @Test
    void model_set_when_supported_datatypes_is_only_empty_nothing_is_accepted() {

        /* prepare */
        when(jobConfigurationSupport.getSupportedDataTypes(any())).thenReturn(Collections.emptySet());
        when(jobConfigurationSupport.resolveSecHubConfigurationModel()).thenReturn(sechubConfigurationModel);

        when(productSetup.getScanType()).thenReturn(ScanType.CODE_SCAN);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        verify(modelSupport, never()).isBinaryRequired(any(), any());
        verify(modelSupport, never()).isSourceRequired(any(), any());

        assertFalse(result.isBinaryAccepted());
        assertFalse(result.isSourceAccepted());
        assertFalse(result.isNoneAccepted());
    }

    @Test
    void model_set__when_supported_datatypes_are_all_accepted_and_source_and_binary_supported_context_has_only_none() {

        /* prepare */
        Set<SecHubDataConfigurationType> set = new LinkedHashSet<>();
        set.add(SecHubDataConfigurationType.SOURCE);
        set.add(SecHubDataConfigurationType.BINARY);
        set.add(SecHubDataConfigurationType.NONE);

        when(jobConfigurationSupport.getSupportedDataTypes(any())).thenReturn(set);
        when(jobConfigurationSupport.resolveSecHubConfigurationModel()).thenReturn(sechubConfigurationModel);

        when(productSetup.getScanType()).thenReturn(ScanType.LICENSE_SCAN);
        when(modelSupport.isBinaryRequired(ScanType.LICENSE_SCAN, sechubConfigurationModel)).thenReturn(true);
        when(modelSupport.isSourceRequired(ScanType.LICENSE_SCAN, sechubConfigurationModel)).thenReturn(true);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        verify(modelSupport).isBinaryRequired(ScanType.LICENSE_SCAN, sechubConfigurationModel);
        verify(modelSupport).isSourceRequired(ScanType.LICENSE_SCAN, sechubConfigurationModel);

        assertTrue(result.isBinaryAccepted());
        assertTrue(result.isSourceAccepted());
        assertTrue(result.isNoneAccepted());
    }

    @Test
    void model_set__when_supported_datatypes_are_all_accepted_but_no_source_or_binary_supported_context_has_only_none() {

        /* prepare */
        Set<SecHubDataConfigurationType> set = new LinkedHashSet<>();
        set.add(SecHubDataConfigurationType.SOURCE);
        set.add(SecHubDataConfigurationType.BINARY);
        set.add(SecHubDataConfigurationType.NONE);

        when(jobConfigurationSupport.getSupportedDataTypes(any())).thenReturn(set);
        when(jobConfigurationSupport.resolveSecHubConfigurationModel()).thenReturn(sechubConfigurationModel);

        when(productSetup.getScanType()).thenReturn(ScanType.WEB_SCAN);
        when(modelSupport.isBinaryRequired(ScanType.WEB_SCAN, sechubConfigurationModel)).thenReturn(false);
        when(modelSupport.isSourceRequired(ScanType.WEB_SCAN, sechubConfigurationModel)).thenReturn(false);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);

        /* test */
        verify(modelSupport).isBinaryRequired(ScanType.WEB_SCAN, sechubConfigurationModel);
        verify(modelSupport).isSourceRequired(ScanType.WEB_SCAN, sechubConfigurationModel);

        assertFalse(result.isBinaryAccepted());
        assertFalse(result.isSourceAccepted());
        assertTrue(result.isNoneAccepted());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 10, 4711 })
    void preparedContext_has_read_resilience_max_from_configuration_support(int configSupportValue) {
        /* prepare */
        when(jobConfigurationSupport.getJobStorageReadResilienceRetriesMax(anyInt())).thenReturn(configSupportValue);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);
        assertEquals(configSupportValue, result.getJobStorageReadResilienceRetriesMax());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 5, 331 })
    void preparedContext_has_read_resilience_wait_seconds_from_configuration_support(int configSupportValue) {
        /* prepare */
        when(jobConfigurationSupport.getJobStorageReadResiliencRetryWaitSeconds(anyInt())).thenReturn(configSupportValue);

        /* execute */
        PDSWorkspacePreparationContext result = factoryToTest.createPreparationContext(jobConfigurationSupport);
        assertEquals(configSupportValue, result.getJobStorageReadResilienceRetryWaitSeconds());
    }

}
