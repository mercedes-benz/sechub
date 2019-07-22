// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.daimler.sechub.sharedkernel.validation.ApiVersionValidation;
import com.daimler.sechub.sharedkernel.validation.ProjectIdValidation;
import com.daimler.sechub.sharedkernel.validation.UserIdValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

@Component
public class ProjectJsonInputValidation {

	@Autowired
	ApiVersionValidation apiValidation;

	@Autowired
	ProjectIdValidation projectIdValidation;;

	@Autowired
	UserIdValidation userIdValidation;

	public boolean supports(Class<?> clazz) {
		return ProjectJsonInput.class.isAssignableFrom(clazz);
	}

	public ProjectJsonInput asInput(Object target) {
		return (ProjectJsonInput) target;
	}

	public void checkProjectId(Errors errors, ProjectJsonInput input) {
		ValidationResult projectIdValidationResult = projectIdValidation.validate(input.getName());
		if (!projectIdValidationResult.isValid()) {
			errors.rejectValue(ProjectJsonInput.PROPERTY_NAME, "api.error.projectid.invalid",
					projectIdValidationResult.getErrorDescription());

		}
	}

	public void checkApiVersion(Errors errors, ProjectJsonInput input) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, ProjectJsonInput.PROPERTY_API_VERSION, "field.required");

		String apiVersion = input.getApiVersion();
		if (apiVersion == null) {
			return; /* handled before */
		}
		ValidationResult apiValidationResult = apiValidation.validate(apiVersion);
		if (!apiValidationResult.isValid()) {
			errors.rejectValue(ProjectJsonInput.PROPERTY_API_VERSION, "api.error.unsupported.version",
					apiValidationResult.getErrorDescription());
		}
	}

	public void checkOwnerUserId(Errors errors, ProjectJsonInput input) {
		ValidationResult userIdValidationResult = userIdValidation.validate(input.getOwner());
		if (!userIdValidationResult.isValid()) {
			errors.rejectValue(ProjectJsonInput.PROPERTY_OWNER, "api.error.userid.invalid",
					userIdValidationResult.getErrorDescription());

		}
	}
}
