// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Update variant, will not check name
 * @author Albert Tregnaghi
 *
 */
@Component
public class UpdateProjectInputValidator implements Validator{

	@Autowired
	ProjectJsonInputValidation validation;

	public UpdateProjectInputValidator() {
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return validation.supports(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		validation.checkApiVersion(errors,validation.asInput(target));
		validation.checkWhitelist(errors, validation.asInput(target));
		validation.checkMetaData(errors, validation.asInput(target));
	}
}
