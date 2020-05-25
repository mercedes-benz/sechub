// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import java.util.Collection;

public abstract class AbstractValidation<T> implements Validation<T> {

    protected class ValidationConfig {
        public int /* NOSONAR */ minLength;
        public int /* NOSONAR */ maxLength;
    }

    private ValidationConfig config;

    protected AbstractValidation() {
        this.config = new ValidationConfig();
        setup(config);
    }

    protected ValidationConfig getConfig() {
        return config;
    }

    /**
     * Setup configuration for validation - if necessary.
     * 
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
     * 
     * @param context
     */
    protected void validateNotNull(ValidationContext<?> context) {
        if (context.objectToValidate != null) {
            return;
        }
        context.addError("May not be null");
    }

    protected void validateNotNull(ValidationContext<?> context, Object whichShallNotbeNull, String name) {
        if (whichShallNotbeNull != null) {
            return;
        }
        context.addError(name + " may not be null");
    }

    protected void validateMinSize(ValidationContext<?> context, Collection<?> toInspect, int minimum, String name) {
        if (toInspect == null) {
            return;
        }
        if (toInspect.size() >= minimum) {
            return;
        }
        context.addError(name + " may not be smaller than " + minimum);
    }

    protected void validateMaxSize(ValidationContext<?> context, Collection<?> toInspect, int maximum, String name) {
        if (toInspect == null) {
            return;
        }
        if (toInspect.size() <= maximum) {
            return;
        }
        context.addError(name + " may not be larger than " + maximum);
    }
    protected void validateMaxLength(ValidationContext<?> context, String toInspect, int maximum, String name) {
        if (toInspect == null) {
            return;
        }
        if (toInspect.length() <= maximum) {
            return;
        }
        context.addError(name + " may not be larger than " + maximum);
    }

    @SuppressWarnings("unchecked")
    protected <X> void validateContainsExpectedOnly(ValidationContext<?> context, String failureMessage, X toInspect, X... allowed) {
        if (toInspect == null) {
            return;
        }
        for (X current: allowed) {
            if (toInspect.equals(current)) {
                return;
            }
        }
        context.addError(failureMessage);
    }

    /**
     * Validates given object is not <code>null</code>
     * 
     * @param context
     */
    protected void validateNotNull(ValidationContext<?> context, String message, Object object) {
        if (object != null) {
            return;
        }
        context.addError("May not be null:" + message);
    }

    /**
     * Validation implementation called by abstract implementation.<br>
     * Either use methods from abstract class to validate here the context, or write
     * your own custom validation and just use information found inside context
     * 
     * @param context object containing information about target, validation and
     *                more
     */
    protected abstract void validate(ValidationContext<T> context);

    protected final int getMinLength() {
        return config.minLength;
    }

    protected final int getMaxLength() {
        return config.maxLength;
    }

    protected T getObjectToValidate(ValidationContext<T> context) {
        return context.objectToValidate;
    }
}
