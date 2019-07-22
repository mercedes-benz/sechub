// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

public abstract class AbstractValidation<T> implements Validation<T> {

	protected class ValidationConfig{
		public int /* NOSONAR */minLength;
		public int /* NOSONAR */maxLength;
	}

	private ValidationConfig config;

	protected AbstractValidation() {
		this.config=new ValidationConfig();
		setup(config);
	}

	protected abstract void setup(ValidationConfig config);

	public final ValidationResult validate(T target) {
		ValidationContext<T> context = new ValidationContext<>(target);
		validate(context);
		return context.result;
	}

	protected void validateNotNull(ValidationContext<?> context) {
		if (context.objectToValidate!=null) {
			return;
		}
		context.addError("May not be null");
	}

	protected abstract void validate(ValidationContext<T> context);

	protected final int getMinLength() {
		return config.minLength;
	}

	protected final int getMaxLength() {
		return config.maxLength;
	}
}
