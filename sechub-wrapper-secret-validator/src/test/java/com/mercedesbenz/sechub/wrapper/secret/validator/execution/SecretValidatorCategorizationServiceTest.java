// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.MalformedURLException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorCategorization;
import com.mercedesbenz.sechub.wrapper.secret.validator.support.SarifImporterKeys;

import de.jcup.sarif_2_1_0.model.PropertyBag;
import de.jcup.sarif_2_1_0.model.Region;

class SecretValidatorCategorizationServiceTest {

    private SecretValidatorCategorizationService serviceToTest = new SecretValidatorCategorizationService();

    @Test
    void categorization_config_is_null_results_in_property_bag_being_empty() {
        /* prepare */
        Region findingRegion = new Region();
        SecretValidationResult validationResult = new SecretValidationResult();

        /* execute */
        serviceToTest.categorizeFindingByRegion(validationResult, findingRegion, null);

        /* test */
        assertNull(findingRegion.getProperties());
    }

    @Test
    void categorization_config_is_empty_results_in_property_bag_being_empty() {
        /* prepare */
        Region findingRegion = new Region();
        SecretValidationResult validationResult = new SecretValidationResult();
        SecretValidatorCategorization categorization = new SecretValidatorCategorization();

        /* execute */
        serviceToTest.categorizeFindingByRegion(validationResult, findingRegion, categorization);

        /* test */
        assertNull(findingRegion.getProperties());
    }

    @Test
    void validation_result_valid_results_in_configured_categorization() {
        /* prepare */
        Region findingRegion = new Region();
        SecretValidationResult validationResult = new SecretValidationResult();
        validationResult.setValidationStatus(SecretValidationStatus.VALID);
        validationResult.setValidatedByUrl("http://api.example.com");

        SecretValidatorCategorization categorization = new SecretValidatorCategorization();
        categorization.setValidationSuccessSeverity("critical");

        /* execute */
        serviceToTest.categorizeFindingByRegion(validationResult, findingRegion, categorization);

        /* test */
        PropertyBag properties = findingRegion.getProperties();
        Map<String, Object> additionalProperties = properties.getAdditionalProperties();

        assertEquals(2, additionalProperties.size());
        assertEquals(categorization.getValidationSuccessSeverity(), additionalProperties.get(SarifImporterKeys.SECRETSCAN_SECHUB_SEVERITY.getKey()));
        assertEquals("http://api.example.com", additionalProperties.get(SarifImporterKeys.SECRETSCAN_VALIDATED_BY_URL.getKey()));
    }

    @Test
    void validation_result_invalid_results_in_configured_categorization() {
        /* prepare */
        Region findingRegion = new Region();
        SecretValidationResult validationResult = new SecretValidationResult();
        validationResult.setValidationStatus(SecretValidationStatus.INVALID);

        SecretValidatorCategorization categorization = new SecretValidatorCategorization();
        categorization.setValidationFailedSeverity("low");

        /* execute */
        serviceToTest.categorizeFindingByRegion(validationResult, findingRegion, categorization);

        /* test */
        PropertyBag properties = findingRegion.getProperties();
        Map<String, Object> additionalProperties = properties.getAdditionalProperties();

        assertEquals(1, additionalProperties.size());
        assertEquals(categorization.getValidationFailedSeverity(), additionalProperties.get(SarifImporterKeys.SECRETSCAN_SECHUB_SEVERITY.getKey()));
    }

    @Test
    void validation_result_with_no_validation_configured_results_in_default_categorization() {
        /* prepare */
        Region findingRegion = new Region();
        SecretValidationResult validationResult = new SecretValidationResult();
        validationResult.setValidationStatus(SecretValidationStatus.NO_VALIDATION_CONFIGURED);

        SecretValidatorCategorization categorization = new SecretValidatorCategorization();
        categorization.setDefaultSeverity("medium");

        /* execute */
        serviceToTest.categorizeFindingByRegion(validationResult, findingRegion, categorization);

        /* test */
        PropertyBag properties = findingRegion.getProperties();
        Map<String, Object> additionalProperties = properties.getAdditionalProperties();

        assertEquals(1, additionalProperties.size());
        assertEquals(categorization.getDefaultSeverity(), additionalProperties.get(SarifImporterKeys.SECRETSCAN_SECHUB_SEVERITY.getKey()));
    }

    @Test
    void validation_result_with_sarif_snippet_not_set_results_in_default_categorization() {
        /* prepare */
        Region findingRegion = new Region();
        SecretValidationResult validationResult = new SecretValidationResult();
        validationResult.setValidationStatus(SecretValidationStatus.SARIF_SNIPPET_NOT_SET);

        SecretValidatorCategorization categorization = new SecretValidatorCategorization();
        categorization.setDefaultSeverity("medium");

        /* execute */
        serviceToTest.categorizeFindingByRegion(validationResult, findingRegion, categorization);

        /* test */
        PropertyBag properties = findingRegion.getProperties();
        Map<String, Object> additionalProperties = properties.getAdditionalProperties();

        assertEquals(1, additionalProperties.size());
        assertEquals(categorization.getDefaultSeverity(), additionalProperties.get(SarifImporterKeys.SECRETSCAN_SECHUB_SEVERITY.getKey()));
    }

    @Test
    void validation_empty_results_in_default_categorization() {
        /* prepare */
        Region findingRegion = new Region();
        SecretValidationResult validationResult = new SecretValidationResult();

        SecretValidatorCategorization categorization = new SecretValidatorCategorization();
        categorization.setDefaultSeverity("medium");

        /* execute */
        serviceToTest.categorizeFindingByRegion(validationResult, findingRegion, categorization);

        /* test */
        PropertyBag properties = findingRegion.getProperties();
        Map<String, Object> additionalProperties = properties.getAdditionalProperties();

        assertEquals(1, additionalProperties.size());
        assertEquals(categorization.getDefaultSeverity(), additionalProperties.get(SarifImporterKeys.SECRETSCAN_SECHUB_SEVERITY.getKey()));
    }

    @Test
    void validation_result_valid_results_in_severity_value_is_null() throws MalformedURLException {
        /* prepare */
        Region findingRegion = new Region();
        SecretValidationResult validationResult = new SecretValidationResult();
        validationResult.setValidationStatus(SecretValidationStatus.VALID);

        // we do not configure a successful validation category here
        // this should not happen because we create the validation config file step by
        // step,
        // but we need to make sure this will not fail in any case
        SecretValidatorCategorization categorization = new SecretValidatorCategorization();
        categorization.setValidationFailedSeverity("low");

        /* execute */
        serviceToTest.categorizeFindingByRegion(validationResult, findingRegion, categorization);

        /* test */
        PropertyBag properties = findingRegion.getProperties();
        Map<String, Object> additionalProperties = properties.getAdditionalProperties();

        assertEquals(1, additionalProperties.size());
        assertNull(additionalProperties.get(SarifImporterKeys.SECRETSCAN_SECHUB_SEVERITY.getKey()));
    }

}
