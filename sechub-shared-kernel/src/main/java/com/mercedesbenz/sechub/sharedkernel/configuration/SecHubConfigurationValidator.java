// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.configuration;

import static com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.mercedesbenz.sechub.commons.core.util.SimpleNetworkUtils;
import com.mercedesbenz.sechub.commons.model.SecHubInfrastructureScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

@Component
public class SecHubConfigurationValidator implements Validator {

    private static final Logger LOG = LoggerFactory.getLogger(SecHubConfigurationValidator.class);

    @Autowired
    ApiVersionValidationFactory apiVersionValidationFactory;

    @Autowired
    UserContextService userContextService;

    private ApiVersionValidation apiVersionValidation;

    @PostConstruct
    void postConstruct() {
        apiVersionValidation = apiVersionValidationFactory.createValidationAccepting("1.0");
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return SecHubConfiguration.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LOG.debug("Start validation for: {}", target);

        SecHubConfiguration configuration = (SecHubConfiguration) target;

        validateAPI(configuration, errors);
        validateAtLeastOneScanConfiguration(configuration, errors);
        validateWebScan(configuration, errors);
        validateInfraScan(configuration, errors);

        if (errors.hasErrors()) {
            List<ObjectError> objectErrors = errors.getAllErrors();
            for (ObjectError error : objectErrors) {
                LOG.info("Rejected sechub configuration from user:{}. Reason: {}", userContextService.getUserId(), error.getDefaultMessage());
            }
        }

    }

    private void validateAtLeastOneScanConfiguration(SecHubConfiguration configuration, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }
        if (!hasAtLeastOneScanConfiguration(configuration)) {
            errors.reject("api.error.config.noscan.defined", "There is not any scan option given, so cannot start scan!");
        }
    }

    private void validateWebScan(SecHubConfiguration configuration, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }
        Optional<SecHubWebScanConfiguration> webscanOption = configuration.getWebScan();
        if (!webscanOption.isPresent()) {
            return;
        }

        SecHubWebScanConfiguration webscan = webscanOption.get();
        URI uri = webscan.getUri();

        if (SimpleNetworkUtils.isURINullOrEmpty(uri)) {
            errors.reject("api.error.webscan.target.missing", new Object[] {},
                    "Webscan configuration contains no target at all - but at one URI is necessary for a webscan!");

        } else {
            if (!SimpleNetworkUtils.isHttpProtocol(uri)) {
                errors.reject("api.error.webscan.uri.illegalschema", new Object[] { uri },
                        "Webscan configuration contains uri '{0}' which is not of supported protocolls (http,https)");
            }
        }
    }

    private void validateInfraScan(SecHubConfiguration configuration, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }
        Optional<SecHubInfrastructureScanConfiguration> infraScanOption = configuration.getInfraScan();
        if (!infraScanOption.isPresent()) {
            return;
        }
        SecHubInfrastructureScanConfiguration infraScan = infraScanOption.get();
        if (infraScan.getUris().isEmpty() && infraScan.getIps().isEmpty()) {
            errors.reject("api.error.infrascan.target.missing", new Object[] {},
                    "Webscan configuration contains no target at all - but at least one URI or IP is necessary for scans!");
        }
    }

    private void validateAPI(SecHubConfiguration configuration, Errors errors) {
        if (errors.hasErrors()) {
            return;
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, PROPERTY_API_VERSION, "field.required");

        if (errors.hasErrors()) {
            return;
        }
        String apiVersion = configuration.getApiVersion();
        ValidationResult apiValidationResult = apiVersionValidation.validate(apiVersion);

        if (!apiValidationResult.isValid()) {
            errors.rejectValue(PROPERTY_API_VERSION, "api.error.unsupported.version", apiValidationResult.getErrorDescription());
        }
    }

    private boolean hasAtLeastOneScanConfiguration(SecHubConfiguration configuration) {
        boolean atLeastOne = false;
        if (configuration != null) {
            atLeastOne = atLeastOne || configuration.getCodeScan().isPresent(); /* NOSONAR */
            atLeastOne = atLeastOne || configuration.getInfraScan().isPresent();
            atLeastOne = atLeastOne || configuration.getWebScan().isPresent();
        }
        return atLeastOne;
    }

}
