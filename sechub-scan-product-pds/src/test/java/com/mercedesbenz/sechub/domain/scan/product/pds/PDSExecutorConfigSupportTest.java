// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData;
import com.mercedesbenz.sechub.commons.pds.data.PDSTemplateMetaData.PDSAssetData;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.config.ScanMapping;
import com.mercedesbenz.sechub.domain.scan.config.ScanMappingRepository;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.test.TestCanaryException;

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
    private static final String COMBINED_MAPPING_VALUE_DATA = SECHUB_MAPPING_ID_1 + "," + SECHUB_MAPPING_ID_2 + " , " + SECHUB_MAPPING_ID_3_NOT_KNOWN_BY_SECHUB;

    private static final String CONFIGURED_PDS_PRODUCT_IDENTIFIER = "a_string";
    private PDSExecutorConfigSupport supportToTest;
    private ProductExecutorConfig config;
    private ProductExecutorConfigSetup executorConfigSetup;
    private ProductExecutorConfigSetupCredentials credentialsInConfigSetup;
    private PDSExecutorConfigSuppportServiceCollection serviceCollection;
    private List<ProductExecutorConfigSetupJobParameter> jobParameters;
    private ScanMappingRepository repository;
    private SystemEnvironmentVariableSupport systemEnvironmentVariableSupport;
    private ProductExecutorContext context;
    private SecHubExecutionContext sechubExecutionContext;
    private SecHubConfiguration sechubConfiguration;
    private PDSTemplateMetaDataService templateMetaDataService;

    @BeforeEach
    public void before() throws Exception {
        sechubConfiguration = mock();
        sechubExecutionContext = mock();
        config = mock();
        context = mock();
        executorConfigSetup = mock();
        serviceCollection = mock();
        repository = mock();
        templateMetaDataService = mock();

        when(sechubExecutionContext.getTemplateDefinitions()).thenReturn(Collections.emptyList());
        when(sechubExecutionContext.getConfiguration()).thenReturn(sechubConfiguration);

        when(config.getProductIdentifier()).thenReturn(ProductIdentifier.UNKNOWN);// the scan type unknown is used, because not really relevant
        when(context.getExecutorConfig()).thenReturn(config);

        jobParameters = new ArrayList<>();
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_PRODUCTIDENTIFIER.getKey().getId(),
                CONFIGURED_PDS_PRODUCT_IDENTIFIER));
        jobParameters
                .add(new ProductExecutorConfigSetupJobParameter(SecHubProductExecutionPDSKeyProvider.PDS_FORBIDS_TARGETTYPE_INTERNET.getKey().getId(), "true"));
        jobParameters.add(
                new ProductExecutorConfigSetupJobParameter(SecHubProductExecutionPDSKeyProvider.PDS_FORBIDS_TARGETTYPE_INTRANET.getKey().getId(), "false"));

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_USE_SECHUB_MAPPINGS.getKey().getId(),
                COMBINED_MAPPING_VALUE_DATA));

        when(config.getSetup()).thenReturn(executorConfigSetup);
        credentialsInConfigSetup = new ProductExecutorConfigSetupCredentials();
        when(executorConfigSetup.getCredentials()).thenReturn(credentialsInConfigSetup);

        when(executorConfigSetup.getJobParameters()).thenReturn(jobParameters);

        Answer<String> defaultAnswerWithNoConversion = createAnswerWhichReturnsAlwaysJustTheOriginValue();
        systemEnvironmentVariableSupport = mock(SystemEnvironmentVariableSupport.class, defaultAnswerWithNoConversion);

        when(serviceCollection.getScanMappingRepository()).thenReturn(repository);
        when(serviceCollection.getSystemEnvironmentVariableSupport()).thenReturn(systemEnvironmentVariableSupport);
        when(serviceCollection.getTemplateMetaDataService()).thenReturn(templateMetaDataService);

        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);
    }

    @EnumSource(SecHubDataConfigurationType.class)
    @ParameterizedTest
    void isGivenStorageSupportedByPDSProduct_binary_and_source_required_from_model_all_supported(SecHubDataConfigurationType type) {
        /* prepare */
        PDSStorageContentProvider contentProvider = mock(PDSStorageContentProvider.class);

        when(contentProvider.isBinaryRequired()).thenReturn(true);
        when(contentProvider.isSourceRequired()).thenReturn(true);

        jobParameters
                .add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_SUPPORTED_DATATYPES.getKey().getId(), type.toString()));

        // create support again (necessary to have new job parameters included)
        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);

        /* execute */
        boolean result = supportToTest.isGivenStorageSupportedByPDSProduct(contentProvider);

        /* test */
        assertEquals(true, result);
    }

    @Test
    void isGivenStorageSupportedByPDSProduct_binary_and_source_required_from_model_no_type_in_jobparameters() {
        /* prepare */
        PDSStorageContentProvider contentProvider = mock(PDSStorageContentProvider.class);

        when(contentProvider.isBinaryRequired()).thenReturn(true);
        when(contentProvider.isSourceRequired()).thenReturn(true);

        /* execute */
        boolean result = supportToTest.isGivenStorageSupportedByPDSProduct(contentProvider);

        /* test */
        assertEquals(true, result);
    }

    @Test
    void isGivenStorageSupportedByPDSProduct_no_binary_in_model_required_but_product_supports_only_binary() {
        /* prepare */
        PDSStorageContentProvider contentProvider = mock(PDSStorageContentProvider.class);

        when(contentProvider.isBinaryRequired()).thenReturn(false);
        when(contentProvider.isSourceRequired()).thenReturn(true);

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_SUPPORTED_DATATYPES.getKey().getId(),
                SecHubDataConfigurationType.BINARY.toString()));

        // create support again (necessary to have new job parameters included)
        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);

        /* execute */
        boolean result = supportToTest.isGivenStorageSupportedByPDSProduct(contentProvider);

        /* test */
        assertEquals(false, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "binary,source", "none   , binary", "binary, source", "SOURCE,BINARY" })
    void isGivenStorageSupportedByPDSProduct_only_source_in_model_required_but_product_supports_binary_and_others(String typesAsString) {
        /* prepare */
        PDSStorageContentProvider contentProvider = mock(PDSStorageContentProvider.class);

        when(contentProvider.isBinaryRequired()).thenReturn(false);
        when(contentProvider.isSourceRequired()).thenReturn(true);

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_SUPPORTED_DATATYPES.getKey().getId(), typesAsString));

        // create support again (necessary to have new job parameters included)
        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);

        /* execute */
        boolean result = supportToTest.isGivenStorageSupportedByPDSProduct(contentProvider);

        /* test */
        assertEquals(true, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "binary,source", "none   , source", "binary, source", "SOURCE,BINARY", "binary,source,none" })
    void isGivenStorageSupportedByPDSProduct_only_binary_in_model_required_but_product_supports_source_and_others(String typesAsString) {
        /* prepare */
        PDSStorageContentProvider contentProvider = mock(PDSStorageContentProvider.class);

        when(contentProvider.isBinaryRequired()).thenReturn(true);
        when(contentProvider.isSourceRequired()).thenReturn(false);

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_SUPPORTED_DATATYPES.getKey().getId(), typesAsString));

        // create support again (necessary to have new job parameters included)
        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);

        /* execute */
        boolean result = supportToTest.isGivenStorageSupportedByPDSProduct(contentProvider);

        /* test */
        assertEquals(true, result);
    }

    @Test
    void isGivenStorageSupportedByPDSProduct_no_source_in_model_required_but_product_supports_only_source() {
        /* prepare */
        PDSStorageContentProvider contentProvider = mock(PDSStorageContentProvider.class);

        when(contentProvider.isBinaryRequired()).thenReturn(true);
        when(contentProvider.isSourceRequired()).thenReturn(false);

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_SUPPORTED_DATATYPES.getKey().getId(),
                SecHubDataConfigurationType.SOURCE.toString()));

        // create support again (necessary to have new job parameters included)
        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);

        /* execute */
        boolean result = supportToTest.isGivenStorageSupportedByPDSProduct(contentProvider);

        /* test */
        assertEquals(false, result);
    }

    @Test
    void isGivenStorageSupportedByPDSProduct_no_source_or_binary_in_model_required_but_product_supports_NONE() {
        /* prepare */
        PDSStorageContentProvider contentProvider = mock(PDSStorageContentProvider.class);

        when(contentProvider.isBinaryRequired()).thenReturn(false);
        when(contentProvider.isSourceRequired()).thenReturn(false);

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_SUPPORTED_DATATYPES.getKey().getId(),
                SecHubDataConfigurationType.NONE.toString()));

        // create support again (necessary to have new job parameters included)
        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);

        /* execute */
        boolean result = supportToTest.isGivenStorageSupportedByPDSProduct(contentProvider);

        /* test */
        assertEquals(true, result);
    }

    @EnumSource(value = SecHubDataConfigurationType.class, mode = Mode.EXCLUDE, names = { "NONE" })
    @ParameterizedTest
    void isGivenStorageSupportedByPDSProduct_no_source_or_binary_in_model_required_but_product_support_other_except_NONE(SecHubDataConfigurationType type) {
        /* prepare */
        PDSStorageContentProvider contentProvider = mock(PDSStorageContentProvider.class);

        when(contentProvider.isBinaryRequired()).thenReturn(false);
        when(contentProvider.isSourceRequired()).thenReturn(false);

        jobParameters
                .add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_SUPPORTED_DATATYPES.getKey().getId(), type.toString()));

        // create support again (necessary to have new job parameters included)
        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);

        /* execute */
        boolean result = supportToTest.isGivenStorageSupportedByPDSProduct(contentProvider);

        /* test */
        assertEquals(false, result);
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = { "unknown", "source,unknown", "binary,unknown", "unknown,none" })
    void isGivenStorageSupportedByPDSProduct_no_source_or_binary_in_model_but_wrong_or_missing_supported_datatypes(String typesAsString) {
        /* prepare */
        PDSStorageContentProvider contentProvider = mock(PDSStorageContentProvider.class);

        when(contentProvider.isBinaryRequired()).thenReturn(false);
        when(contentProvider.isSourceRequired()).thenReturn(false);

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(PDSConfigDataKeyProvider.PDS_CONFIG_SUPPORTED_DATATYPES.getKey().getId(), typesAsString));

        // create support again (necessary to have new job parameters included)
        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);

        /* execute */
        boolean result = supportToTest.isGivenStorageSupportedByPDSProduct(contentProvider);

        /* test */
        assertEquals(true, result);
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
    void createJobParametersToSendToPDS_null_list_of_template_meta_data_throws_illegal_argument_exception() {
        assertThatThrownBy(() -> supportToTest.createJobParametersToSendToPDS(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createJobParametersToSendToPDS_empty_template_list_is_accepted() throws Exception {
        /* prepare */
        when(sechubExecutionContext.getTemplateDefinitions()).thenReturn(Collections.emptyList());

        /* execute */
        Map<String, String> result = supportToTest.createJobParametersToSendToPDS(sechubExecutionContext);

        /* test */
        String data = result.get(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_TEMPLATE_META_DATA_LIST);
        assertThat(data).isEqualTo("[]");
    }

    @Test
    void createJobParametersToSendToPDS_environmentVariablesEntriesAreReplacedWithTheirContent() throws Exception {
        /* prepare */
        String parameterKey1 = "test.key1";
        String parameterKey2 = "test.key2";
        String parameterKey3 = "test.key3";

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(parameterKey1, "env:A_TESTVARIABLE"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(parameterKey2, "env:A_NOT_EXISTING_VARIABLE"));
        jobParameters.add(new ProductExecutorConfigSetupJobParameter(parameterKey3, "just-a-key-not-converted"));

        // create support again (necessary to have new job parameters included)
        supportToTest = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(context, serviceCollection);

        when(systemEnvironmentVariableSupport.getValueOrVariableContent("env:A_TESTVARIABLE")).thenReturn("resolved-value");
        when(systemEnvironmentVariableSupport.getValueOrVariableContent("env:A_NOT_EXISTING_VARIABLE")).thenReturn(null);

        /* execute */
        Map<String, String> parameterMap = supportToTest.createJobParametersToSendToPDS(sechubExecutionContext);

        /* test */
        assertEquals("resolved-value", parameterMap.get(parameterKey1));
        assertEquals(null, parameterMap.get(parameterKey2));
        assertEquals("just-a-key-not-converted", parameterMap.get(parameterKey3));

    }

    @Test
    void createJobParametersToSendToPDS_mapping_is_resolved() throws Exception {
        /* prepare */
        mockSecHubMappingInDatabase();
        mockSecHubMappingId2InDatabase();

        /* execute */
        Map<String, String> parameters = supportToTest.createJobParametersToSendToPDS(sechubExecutionContext);

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

        /*
         * test 4 - only the parameters are tried to convert, but NOT the mapping data
         */
        verify(systemEnvironmentVariableSupport, times(1)).getValueOrVariableContent(COMBINED_MAPPING_VALUE_DATA);
        verify(systemEnvironmentVariableSupport, times(1)).getValueOrVariableContent(any());
    }

    @Test
    void createJobParametersToSendToPDS_pds_template_meta_data_is_created_by_result_from_template_metadata_service() throws Exception {
        /* prepare */
        List<TemplateDefinition> templateDefinitions = mock();
        when(sechubExecutionContext.getTemplateDefinitions()).thenReturn(templateDefinitions);

        List<PDSTemplateMetaData> templateMetaDataServiceResult = createTemplateMetaDataServiceExampleResult();

        when(templateMetaDataService.createTemplateMetaData(eq(templateDefinitions), anyString(), any(ScanType.class), eq(sechubConfiguration)))
                .thenReturn(templateMetaDataServiceResult);
        /* execute */
        Map<String, String> parameterMap = supportToTest.createJobParametersToSendToPDS(sechubExecutionContext);

        /* test */
        String expectedJson = JSONConverter.get().toJSON(templateMetaDataServiceResult, false);
        String jsonFromConfigSupport = parameterMap.get(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_TEMPLATE_META_DATA_LIST);

        assertThat(jsonFromConfigSupport).isNotNull().isEqualTo(expectedJson).hasSizeGreaterThan(10);

    }

    @Test
    void createJobParametersToSendToPDS_templateDataService_is_called_twice_in_correct_order_and_arguments() throws Exception {
        /* prepare */
        List<TemplateDefinition> templateDefinitions = mock();
        when(sechubExecutionContext.getTemplateDefinitions()).thenReturn(templateDefinitions);

        List<PDSTemplateMetaData> templateMetaDataServiceResult = createTemplateMetaDataServiceExampleResult();

        when(templateMetaDataService.createTemplateMetaData(eq(templateDefinitions), anyString(), any(ScanType.class), eq(sechubConfiguration)))
                .thenReturn(templateMetaDataServiceResult);
        // next line does ensure that it is called with the former result. It also
        // breaks further processing
        doThrow(new TestCanaryException()).when(templateMetaDataService).ensureTemplateAssetFilesAreAvailableInStorage(templateMetaDataServiceResult);

        /* execute + test */
        assertThatThrownBy(() -> supportToTest.createJobParametersToSendToPDS(sechubExecutionContext)).isInstanceOf(TestCanaryException.class);

    }

    private List<PDSTemplateMetaData> createTemplateMetaDataServiceExampleResult() {
        List<PDSTemplateMetaData> templateMetaDataServiceResult = new ArrayList<>();
        PDSTemplateMetaData pdsTemplateMetaData1 = new PDSTemplateMetaData();
        PDSAssetData assetData1 = new PDSAssetData();
        assetData1.setAsset("asset-id-1");
        assetData1.setChecksum("checksum1");
        assetData1.setFile("file1");
        pdsTemplateMetaData1.setAssetData(assetData1);
        templateMetaDataServiceResult.add(pdsTemplateMetaData1);
        return templateMetaDataServiceResult;
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

    private Answer<String> createAnswerWhichReturnsAlwaysJustTheOriginValue() {
        return new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object arg0 = invocation.getArgument(0);
                if (arg0 == null) {
                    return null;
                }
                return arg0.toString();
            }

        };
    }

}
