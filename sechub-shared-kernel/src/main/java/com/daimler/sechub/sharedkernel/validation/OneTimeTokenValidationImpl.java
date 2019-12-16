// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class OneTimeTokenValidationImpl extends AbstractSimpleStringValidation  implements OneTimeTokenValidation{


	@Override
	protected void setup(AbstractValidation<String>.ValidationConfig config) {
		config.minLength = 40;
		config.maxLength = 60;
	}

	@Override
	protected void validate(ValidationContext<String> context) {
		validateNotNull(context);
		if (context.isInValid()) {
			return;
		}
		validateSameLengthWhenTrimmed(context);
		validateLength(context);

		validateOnlyAlphabeticDigitOrAllowedParts(context);
	}

}
