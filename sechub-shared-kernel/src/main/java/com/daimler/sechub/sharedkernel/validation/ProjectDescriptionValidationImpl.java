// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class ProjectDescriptionValidationImpl extends AbstractSimpleStringValidation implements ProjectDescriptionValidation{

	@Override
	protected void setup(AbstractValidation<String>.ValidationConfig config) {
		config.maxLength=170;// we got 512 characters inside database /3 (UTF8)= 170
	}

	@Override
	protected void validate(ValidationContext<String> context) {
		validateNotNull(context);
		if (context.isInValid()) {
			return;
		}
		validateMaxLength(context);
	}

}
