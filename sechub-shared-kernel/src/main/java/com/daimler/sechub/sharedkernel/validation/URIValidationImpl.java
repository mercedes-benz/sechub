package com.daimler.sechub.sharedkernel.validation;

import java.net.URI;

import org.springframework.stereotype.Component;

@Component
public class URIValidationImpl extends AbstractValidation<URI> implements URIValidation {

	@Override
	protected void setup(AbstractValidation<URI>.ValidationConfig config) {

	}

	@Override
	protected void validate(ValidationContext<URI> context) {
		validateNotNull(context);
		URI uri = context.objectToValidate;
		if (uri == null) {
			/* already handled before */
			return;
		}

		/* we accept no empty URIs - which is allowed in java" */
		String simpleString = uri.toString().trim();
		if (simpleString.isEmpty()) {
			context.addError("May not be empty!");
			return;
		}
	}

}
