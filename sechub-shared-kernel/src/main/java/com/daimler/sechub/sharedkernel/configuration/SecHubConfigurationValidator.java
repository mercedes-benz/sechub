// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.configuration;

import static com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.daimler.sechub.sharedkernel.validation.ApiVersionValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

@Component
public class SecHubConfigurationValidator implements Validator {

	private static final Logger LOG = LoggerFactory.getLogger(SecHubConfigurationValidator.class);

	@Autowired
	ApiVersionValidation apiValidation;

	@Override
	public boolean supports(Class<?> clazz) {
		return SecHubConfiguration.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		LOG.debug("Start validation for: {}", target);

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, PROPERTY_API_VERSION, "field.required");

		SecHubConfiguration configuration = (SecHubConfiguration) target;
		String apiVersion = configuration.getApiVersion();
		ValidationResult apiValidationResult = apiValidation.validate(apiVersion);
		if (!apiValidationResult.isValid()) {
			errors.rejectValue(PROPERTY_API_VERSION, "api.error.unsupported.version",
					apiValidationResult.getErrorDescription());
			return;
		}
		Optional<SecHubWebScanConfiguration> webscanOption = configuration.getWebScan();
		if (webscanOption.isPresent()) {
			SecHubWebScanConfiguration webscan = webscanOption.get();
			List<URI> uris = webscan.getUris();
			for (URI uri: uris) {
				String schema = uri.getScheme();
				if ("http".equals(schema)|| "https".equals(schema)) {
					continue;
				}
				errors.reject("api.error.webscan.uri.illegalschema", new Object[] {uri}, "Webscan configuration contains uri '{0}' which is not of supported protocolls (http,https)");
			}
		}
		if (!hasAtLeastOneScanConfiguration(configuration)) {
			errors.reject("api.error.config.noscan.defined", "There is not any scan option given, so cannot start scan!");
		}

	}

	private boolean hasAtLeastOneScanConfiguration(SecHubConfiguration configuration) {
		boolean atLeastOne=false;
		if (configuration!=null) {
			atLeastOne = atLeastOne || configuration.getCodeScan().isPresent(); /*NOSONAR*/
			atLeastOne = atLeastOne || configuration.getInfraScan().isPresent();
			atLeastOne = atLeastOne || configuration.getWebScan().isPresent();
		}
		return atLeastOne;
	}


}
