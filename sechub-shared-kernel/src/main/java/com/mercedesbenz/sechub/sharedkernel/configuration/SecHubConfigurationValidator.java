// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel.*;
import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationError.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidationResult.SecHubConfigurationModelValidationErrorData;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelValidator;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;

@Component
public class SecHubConfigurationValidator implements Validator {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubConfigurationValidator.class);

    @Autowired
    UserContextService userContextService;

    @Autowired
    SecHubConfigurationModelValidator modelValidator;

    @Override
    public boolean supports(Class<?> clazz) {
        return SecHubConfiguration.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LOG.debug("Start validation for: {}", target);

        SecHubConfiguration configuration = (SecHubConfiguration) target;
        SecHubConfigurationModelValidationResult modelValidationResult = modelValidator.validateRemoteData(configuration);

        handleExplicitErrorCodes(errors, modelValidationResult);
        addGenericErrorsWhenNecessary(errors, modelValidationResult);

        logErrors(errors);

    }

    private void logErrors(Errors errors) {
        List<ObjectError> objectErrors = errors.getAllErrors();
        for (ObjectError error : objectErrors) {
            LOG.info("Rejected sechub configuration from user:{}. Reason: {}", userContextService.getUserId(), error.getDefaultMessage());
        }
    }

    private void addGenericErrorsWhenNecessary(Errors errors, SecHubConfigurationModelValidationResult modelValidationResult) {
        if (errors.hasErrors()) {
            /* already explicit handled errors - in this case we add not the generic ones */
            return;
        }
        /* okay not handled before with explicit error codes - so add generic errors */
        List<SecHubConfigurationModelValidationErrorData> modelErrors = modelValidationResult.getErrors();
        for (SecHubConfigurationModelValidationErrorData data : modelErrors) {
            errors.reject("api.error.config.generic", data.getMessage());
        }
    }

    private void handleExplicitErrorCodes(Errors errors, SecHubConfigurationModelValidationResult modelValidationResult) {
        handleAPI(errors, modelValidationResult);

        handleAtLeastOneScanConfiguration(errors, modelValidationResult);
        handleWebScan(errors, modelValidationResult);
        handleInfraScan(errors, modelValidationResult);
    }

    private void handleAtLeastOneScanConfiguration(Errors errors, SecHubConfigurationModelValidationResult modelValidationResult) {
        if (errors.hasErrors()) {
            return;
        }
        SecHubConfigurationModelValidationErrorData data = modelValidationResult.findFirstOccurrenceOf(CONTAINS_NO_SCAN_CONFIGURATION);
        if (data != null) {
            errors.reject("api.error.config.noscan.defined", data.getMessage());
        }
    }

    private void handleWebScan(Errors errors, SecHubConfigurationModelValidationResult modelValidationResult) {
        if (errors.hasErrors()) {
            return;
        }

        SecHubConfigurationModelValidationErrorData webScanHasNoURL = modelValidationResult.findFirstOccurrenceOf(WEB_SCAN_HAS_NO_URL_DEFINED);
        if (webScanHasNoURL != null) {
            errors.reject("api.error.webscan.target.missing", new Object[] {}, webScanHasNoURL.getMessage());
        }
        SecHubConfigurationModelValidationErrorData webScanHasUnsupportedSchema = modelValidationResult
                .findFirstOccurrenceOf(WEB_SCAN_URL_HAS_UNSUPPORTED_SCHEMA);
        if (webScanHasUnsupportedSchema != null) {
            errors.reject("api.error.webscan.uri.illegalschema", new Object[] {}, webScanHasUnsupportedSchema.getMessage());
        }
    }

    private void handleInfraScan(Errors errors, SecHubConfigurationModelValidationResult modelValidationResult) {
        if (errors.hasErrors()) {
            return;
        }
        SecHubConfigurationModelValidationErrorData data = modelValidationResult.findFirstOccurrenceOf(INFRA_SCAN_HAS_NO_URIS_OR_IPS_DEFINED);
        if (data != null) {
            errors.reject("api.error.infrascan.target.missing", new Object[] {}, data.getMessage());
        }
    }

    private void handleAPI(Errors errors, SecHubConfigurationModelValidationResult modelValidationResult) {
        if (errors.hasErrors()) {
            return;
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, PROPERTY_API_VERSION, "field.required", API_VERSION_NULL.getDefaultMessage());

        if (errors.hasErrors()) {
            return;
        }
        SecHubConfigurationModelValidationErrorData apiNotSupportedErrorData = modelValidationResult.findFirstOccurrenceOf(API_VERSION_NOT_SUPPORTED);
        if (apiNotSupportedErrorData != null) {
            errors.rejectValue(PROPERTY_API_VERSION, "api.error.unsupported.version", apiNotSupportedErrorData.getMessage());
        }
    }

}
