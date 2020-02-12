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

	/**
	 * Setup configuration for validation - if necessary.
	 * @param config
	 */
	protected abstract void setup(ValidationConfig config);

	public final ValidationResult validate(T target) {
		ValidationContext<T> context = new ValidationContext<>(target);
		validate(context);
		return context.result;
	}
	/**
	 * Validates object inside context is not <code>null</code>
	 * @param context
	 */
	protected void validateNotNull(ValidationContext<?> context) {
		if (context.objectToValidate!=null) {
			return;
		}
		context.addError("May not be null");
	}
	
	/**
     * Validates given object is not <code>null</code>
     * @param context
     */
    protected void validateNotNull(ValidationContext<?> context, String message, Object object) {
        if (object!=null) {
            return;
        }
        context.addError("May not be null:"+message);
    }

	/**
	 * Validation implementation called by abstract implementation.<br>
	 * Either use methods from abstract class to validate here the context,
	 * or write your own custom validation and just use information found
	 * inside context
	 * @param context object containing information about target, validation and more
	 */
	protected abstract void validate(ValidationContext<T> context);

	protected final int getMinLength() {
		return config.minLength;
	}

	protected final int getMaxLength() {
		return config.maxLength;
	}
}
