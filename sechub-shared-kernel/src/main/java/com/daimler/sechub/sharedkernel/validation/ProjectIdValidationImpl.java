// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class ProjectIdValidationImpl extends AbstractSimpleStringValidation implements ProjectIdValidation {

	public static final int PROJECTID_LENGTH_MIN = 2;
	public static final int PROJECTID_LENGTH_MAX = 20;

	@Override
	protected void setup(ValidationConfig config) {
		config.minLength = PROJECTID_LENGTH_MIN;
		config.maxLength = PROJECTID_LENGTH_MAX;
	}

	@Override
	protected void validate(ValidationContext<String> context) {
		validateNotNull(context);
		if (context.isInValid()) {
			return;
		}
		validateNoUpperCaseCharacters(context);
		validateSameLengthWhenTrimmed(context);
		validateLength(context);
		validateNotContainingCharackters(context, '.',',','!','?','\\',':',';','$','%','/');

	}
}
