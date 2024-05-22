// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.springframework.validation.Errors;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationError;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult.SecHubConfigurationModelValidationErrorData;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

public class SecHubConfigurationValidatorTest {

    private SecHubConfigurationValidator validatorToTest;
    private Errors errors;
    private SecHubConfiguration target;
    private ValidationResult okResult;
    private ValidationResult failedResult;
    private SecHubConfigurationModelValidator modelValidator;
    private SecHubConfigurationModelValidationResult configurationModelValidationResult;

    @Before
    public void before() throws Exception {
        okResult = mock(ValidationResult.class);
        when(okResult.isValid()).thenReturn(true);

        failedResult = mock(ValidationResult.class);
        when(failedResult.isValid()).thenReturn(false);

        validatorToTest = new SecHubConfigurationValidator();
        errors = mock(Errors.class);
        target = mock(SecHubConfiguration.class);

        /* prepare defaults */
        when(target.getApiVersion()).thenReturn("1.0");
        when(target.getWebScan()).thenReturn(Optional.empty());

        configurationModelValidationResult = mock(SecHubConfigurationModelValidationResult.class);
        modelValidator = mock(SecHubConfigurationModelValidator.class);
        when(modelValidator.validate(any())).thenReturn(configurationModelValidationResult);

        validatorToTest.modelValidator = modelValidator;

    }

    @Test
    public void when_configuration_model_validation_has_missing_api_found_it_is_rejected() throws Exception {

        /* prepare */
        simulateModelValidationError(SecHubConfigurationModelValidationError.API_VERSION_NULL);

        /* execute */
        validatorToTest.validate(target, errors);

        /* test */
        verify(errors).rejectValue(eq(SecHubConfiguration.PROPERTY_API_VERSION), eq("field.required"), any(),
                eq(SecHubConfigurationModelValidationError.API_VERSION_NULL.getDefaultMessage()));
    }

    @Test
    public void when_configuration_model_validation_has_illegal_api_found_it_is_rejected() throws Exception {

        /* prepare */
        simulateModelValidationError(SecHubConfigurationModelValidationError.API_VERSION_NOT_SUPPORTED);

        /* execute */
        validatorToTest.validate(target, errors);

        /* test */
        verify(errors).rejectValue(eq(SecHubConfiguration.PROPERTY_API_VERSION), eq("api.error.unsupported.version"), any());
    }

    @Test
    public void when_no_error_data_for_any_from_model_validation_it_is_NOT_rejected() throws Exception {
        /* prepare */
        when(configurationModelValidationResult.findFirstOccurrenceOf(any())).thenReturn(null);

        /* execute */
        validatorToTest.validate(target, errors);

        /* test */
        verify(errors, never()).rejectValue(any(), any(), any());
    }

    @Test
    public void when_model_validation_complains_about_empty_infrascanconfig_it_is_rejected() throws Exception {
        /* prepare */
        simulateModelValidationError(SecHubConfigurationModelValidationError.INFRA_SCAN_HAS_NO_URIS_OR_IPS_DEFINED);

        /* execute */
        validatorToTest.validate(target, errors);

        /* test */
        assertError("api.error.infrascan.target.missing", Mockito.times(1));
    }

    @Test
    public void when_model_validation_complains_about_empty_webconfig_is_rejected() throws Exception {
        /* prepare */
        simulateModelValidationError(SecHubConfigurationModelValidationError.WEB_SCAN_HAS_NO_URL_DEFINED);

        /* execute */
        validatorToTest.validate(target, errors);

        /* test */
        assertError("api.error.webscan.target.missing", Mockito.times(1));
    }

    @Test
    public void when_model_validation_complains_about_wrong_schema_it_is_rejected() throws Exception {
        /* prepare */
        simulateModelValidationError(SecHubConfigurationModelValidationError.WEB_SCAN_URL_HAS_UNSUPPORTED_SCHEMA);

        /* execute */
        validatorToTest.validate(target, errors);

        assertIllegalSchemaError();
    }

    @Test
    public void when_configuration_has_no_scan_config_at__missingScanDefinitionError_occurs() {
        /* prepare */
        simulateModelValidationError(SecHubConfigurationModelValidationError.CONTAINS_NO_SCAN_CONFIGURATION);

        /* execute */
        validatorToTest.validate(target, errors);

        /* test */
        assertMissingScanDefinitionError();
    }

    void simulateModelValidationError(SecHubConfigurationModelValidationError error) {
        simulateModelValidationError(error, error.getDefaultMessage());
    }

    void simulateModelValidationError(SecHubConfigurationModelValidationError error, String message) {
        SecHubConfigurationModelValidationErrorData data = mock(SecHubConfigurationModelValidationErrorData.class);
        when(data.getError()).thenReturn(error);
        when(data.getMessage()).thenReturn(message);

        when(configurationModelValidationResult.findFirstOccurrenceOf(error)).thenReturn(data);

    }

    private void assertMissingScanDefinitionError() {
        _assertMissingScanDefinitionError(Mockito.times(1));
    }

    private void _assertMissingScanDefinitionError(VerificationMode mode) {
        verify(errors, mode).reject(eq("api.error.config.noscan.defined"), any());
    }

    private void assertIllegalSchemaError() {
        assertError("api.error.webscan.uri.illegalschema", Mockito.times(1));
    }

    private void assertError(String identifier, VerificationMode mode) {
        verify(errors, mode).reject(eq(identifier), any(), any());
    }

}
