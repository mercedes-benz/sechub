// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorCategorization;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorConfigurationModel;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorConfigurationModelList;
import com.mercedesbenz.sechub.wrapper.secret.validator.support.SarifValidationSupport;

import de.jcup.sarif_2_1_0.model.Location;
import de.jcup.sarif_2_1_0.model.Result;
import de.jcup.sarif_2_1_0.model.Run;
import de.jcup.sarif_2_1_0.model.SarifSchema210;

class SecretValidatorExecutionServiceTest {

    private SecretValidatorExecutionService serviceToTest;

    private SecretValidatorExecutionContextFactory contextFactory;
    private SecretValidationServiceImpl validationService;
    private SerecoSeveritySarifEnhancementService sarifEnhancementService;
    private SarifValidationSupport sarifValidationSupport;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new SecretValidatorExecutionService();

        contextFactory = mock(SecretValidatorExecutionContextFactory.class);
        validationService = mock(SecretValidationServiceImpl.class);
        sarifEnhancementService = mock(SerecoSeveritySarifEnhancementService.class);
        sarifValidationSupport = mock(SarifValidationSupport.class);

        serviceToTest.contextFactory = contextFactory;
        serviceToTest.validationService = validationService;
        serviceToTest.sarifEnhancementService = sarifEnhancementService;
        serviceToTest.sarifValidationSupport = sarifValidationSupport;
    }

    @Test
    void finding_cannot_be_validated_results_in_validation_and_categorization_never_being_called() {
        SarifSchema210 report = createEmptySarifReport();

        @SuppressWarnings("unchecked")
        Map<String, SecretValidatorConfigurationModel> ruleConfigurations = mock(Map.class);
        when(ruleConfigurations.get(any())).thenReturn(null);

        SecretValidatorExecutionContext executionContext = SecretValidatorExecutionContext.builder().setSarifReport(report)
                .setValidatorConfiguration(ruleConfigurations).setTrustAllCertificates(true).build();
        when(contextFactory.create()).thenReturn(executionContext);

        when(sarifValidationSupport.findingCanBeValidated(any())).thenReturn(false);

        /* execute */
        serviceToTest.execute();

        /* test */
        verify(contextFactory, times(1)).create();
        verify(validationService, never()).validateFindingByRegion(any(), any(), anyBoolean());
        verify(sarifEnhancementService, never()).addSerecoSeverityInfo(any(), any(), any());
        verify(sarifValidationSupport, times(1)).findingCanBeValidated(any());
    }

    @Test
    void empty_config_map_results_in_validation_and_categorization_never_being_called() {
        /* prepare */
        SarifSchema210 report = createEmptySarifReport();

        @SuppressWarnings("unchecked")
        Map<String, SecretValidatorConfigurationModel> ruleConfigurations = mock(Map.class);
        when(ruleConfigurations.get(any())).thenReturn(null);

        SecretValidatorExecutionContext executionContext = SecretValidatorExecutionContext.builder().setSarifReport(report)
                .setValidatorConfiguration(ruleConfigurations).setTrustAllCertificates(true).build();
        when(contextFactory.create()).thenReturn(executionContext);

        when(sarifValidationSupport.findingCanBeValidated(any())).thenReturn(true);

        /* execute */
        serviceToTest.execute();

        /* test */
        verify(contextFactory, times(1)).create();
        verify(validationService, never()).validateFindingByRegion(any(), any(), anyBoolean());
        verify(sarifEnhancementService, never()).addSerecoSeverityInfo(any(), any(), any());
        verify(sarifValidationSupport, times(1)).findingCanBeValidated(any());
    }

    @Test
    void categorization_of_config_is_null_results_in_validation_and_categorization_never_being_called() {
        SarifSchema210 report = createEmptySarifReport();

        SecretValidatorConfigurationModel config = new SecretValidatorConfigurationModel();
        config.setCategorization(null);

        @SuppressWarnings("unchecked")
        Map<String, SecretValidatorConfigurationModel> ruleConfigurations = mock(Map.class);
        when(ruleConfigurations.get(any())).thenReturn(config);

        SecretValidatorExecutionContext executionContext = SecretValidatorExecutionContext.builder().setSarifReport(report)
                .setValidatorConfiguration(ruleConfigurations).setTrustAllCertificates(true).build();
        when(contextFactory.create()).thenReturn(executionContext);

        when(sarifValidationSupport.findingCanBeValidated(any())).thenReturn(true);

        /* execute */
        serviceToTest.execute();

        /* test */
        verify(contextFactory, times(1)).create();
        verify(validationService, never()).validateFindingByRegion(any(), any(), anyBoolean());
        verify(sarifEnhancementService, never()).addSerecoSeverityInfo(any(), any(), any());
        verify(sarifValidationSupport, times(1)).findingCanBeValidated(any());
    }

    @Test
    void categorization_of_config_is_empty_results_in_validation_and_categorization_never_being_called() {
        SarifSchema210 report = createEmptySarifReport();

        SecretValidatorConfigurationModel config = new SecretValidatorConfigurationModel();
        SecretValidatorCategorization categorization = new SecretValidatorCategorization();
        config.setCategorization(categorization);

        @SuppressWarnings("unchecked")
        Map<String, SecretValidatorConfigurationModel> ruleConfigurations = mock(Map.class);
        when(ruleConfigurations.get(any())).thenReturn(config);

        SecretValidatorExecutionContext executionContext = SecretValidatorExecutionContext.builder().setSarifReport(report)
                .setValidatorConfiguration(ruleConfigurations).setTrustAllCertificates(true).build();
        when(contextFactory.create()).thenReturn(executionContext);

        when(sarifValidationSupport.findingCanBeValidated(any())).thenReturn(true);

        /* execute */
        serviceToTest.execute();

        /* test */
        verify(contextFactory, times(1)).create();
        verify(validationService, never()).validateFindingByRegion(any(), any(), anyBoolean());
        verify(sarifEnhancementService, never()).addSerecoSeverityInfo(any(), any(), any());
        verify(sarifValidationSupport, times(1)).findingCanBeValidated(any());
    }

    @Test
    void finding_location_cannot_be_validated_results_in_validation_and_categorization_never_being_called() {
        SarifSchema210 report = createEmptySarifReport();

        SecretValidatorConfigurationModel config = new SecretValidatorConfigurationModel();
        SecretValidatorCategorization categorization = mock(SecretValidatorCategorization.class);
        categorization.setDefaultSeverity("high");
        config.setCategorization(categorization);

        @SuppressWarnings("unchecked")
        Map<String, SecretValidatorConfigurationModel> ruleConfigurations = mock(Map.class);
        when(ruleConfigurations.get(any())).thenReturn(config);

        SecretValidatorExecutionContext executionContext = SecretValidatorExecutionContext.builder().setSarifReport(report)
                .setValidatorConfiguration(ruleConfigurations).setTrustAllCertificates(true).build();
        when(contextFactory.create()).thenReturn(executionContext);

        when(sarifValidationSupport.findingCanBeValidated(any())).thenReturn(true);
        when(sarifValidationSupport.findingLocationCanBeValidated(any())).thenReturn(false);

        /* execute */
        serviceToTest.execute();

        /* test */
        verify(contextFactory, times(1)).create();
        verify(validationService, never()).validateFindingByRegion(any(), any(), anyBoolean());
        verify(sarifEnhancementService, never()).addSerecoSeverityInfo(any(), any(), any());
        verify(sarifValidationSupport, times(1)).findingCanBeValidated(any());
        verify(sarifValidationSupport, times(1)).findingLocationCanBeValidated(any());
    }

    @Test
    void valid_config_and_valid_sarif_report_results_in_validation_and_categorization_being_called_for_configured_rule() {
        /* prepare */
        SecretValidatorExecutionContext executionContext = createValidExecutionContext();
        when(contextFactory.create()).thenReturn(executionContext);

        SecretValidationResult secretValidationResult = new SecretValidationResult();
        when(validationService.validateFindingByRegion(any(), any(), anyBoolean())).thenReturn(secretValidationResult);

        doNothing().when(sarifEnhancementService).addSerecoSeverityInfo(any(), any(), any());

        when(sarifValidationSupport.findingCanBeValidated(any())).thenReturn(true);
        when(sarifValidationSupport.findingLocationCanBeValidated(any())).thenReturn(true);

        /* execute */
        serviceToTest.execute();

        /* test */
        verify(contextFactory, times(1)).create();
        verify(validationService, times(6)).validateFindingByRegion(any(), any(), anyBoolean());
        verify(sarifEnhancementService, times(6)).addSerecoSeverityInfo(any(), any(), any());
        verify(sarifValidationSupport, times(6)).findingCanBeValidated(any());
        verify(sarifValidationSupport, times(6)).findingLocationCanBeValidated(any());
    }

    private SecretValidatorExecutionContext createValidExecutionContext() {
        SarifSchema210 report = createSarifReport(new File("src/test/resources/config-test-files/valid-files/test-result.txt"));

        Map<String, SecretValidatorConfigurationModel> ruleConfigurations = createRuleConfigurations(
                new File("src/test/resources/config-test-files/valid-files/test-config.json"));

        /* @formatter:off */
        return SecretValidatorExecutionContext.builder()
                                        .setTrustAllCertificates(true)
                                        .setSarifReport(report)
                                        .setValidatorConfiguration(ruleConfigurations)
                                        .build();
        /* @formatter:on */
    }

    private SarifSchema210 createSarifReport(File file) {
        try {
            String sarifReportJson = TestFileReader.loadTextFile(file);
            return JSONConverter.get().fromJSON(SarifSchema210.class, sarifReportJson);
        } catch (Exception e) {
            throw new IllegalStateException("Creating SARIF report model from: " + file + " failed!", e);
        }
    }

    private Map<String, SecretValidatorConfigurationModel> createRuleConfigurations(File file) {
        try {
            String validatorConfigJson = TestFileReader.loadTextFile(file);
            SecretValidatorConfigurationModelList configurationDataList = JSONConverter.get().fromJSON(SecretValidatorConfigurationModelList.class,
                    validatorConfigJson);

            Map<String, SecretValidatorConfigurationModel> ruleConfigurations = new HashMap<>();
            for (SecretValidatorConfigurationModel configData : configurationDataList.getValidatorConfigList()) {
                ruleConfigurations.put(configData.getRuleId(), configData);
            }
            return ruleConfigurations;
        } catch (Exception e) {
            throw new IllegalStateException("Creating secret validator configuration from: " + file + " failed!", e);
        }
    }

    private SarifSchema210 createEmptySarifReport() {
        List<Run> runs = new ArrayList<>();

        Run run = new Run();

        List<Result> results = new ArrayList<>();
        Result result = new Result();

        ArrayList<Location> locations = new ArrayList<>();
        locations.add(new Location());

        result.setLocations(locations);
        results.add(result);
        run.setResults(results);
        ;
        runs.add(run);

        SarifSchema210 report = new SarifSchema210();
        report.setRuns(runs);

        return report;
    }

}
