// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import java.util.Collection;

public abstract class AbstractValidation<T> implements Validation<T> {

    protected class ValidationConfig {
        public String /* NOSONAR */ errorPrefix;
        public int /* NOSONAR */ minLength;
        public int /* NOSONAR */ maxLength;
    }

    private ValidationConfig config;

    protected AbstractValidation() {
        this.config = new ValidationConfig();
        this.config.errorPrefix=getValidatorName()+":";
                
        setup(config);
    }
    
    protected abstract String getValidatorName();

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
        addErrorMessage(context,"May not be null");
    }

    protected void validateNotNull(ValidationContext<?> context, Object whichShallNotbeNull, String name) {
        if (whichShallNotbeNull != null) {
            return;
        }
        addErrorMessage(context,name + " may not be null");
    }

    protected void validateMinSize(ValidationContext<?> context, Collection<?> toInspect, int minimum, String name) {
        if (toInspect == null) {
            return;
        }
        if (toInspect.size() >= minimum) {
            return;
        }
        addErrorMessage(context,name + " may not be smaller than " + minimum);
    }

    protected void validateMaxSize(ValidationContext<?> context, Collection<?> toInspect, int maximum, String name) {
        if (toInspect == null) {
            return;
        }
        if (toInspect.size() <= maximum) {
            return;
        }
        addErrorMessage(context,name + " may not be larger than " + maximum);
    }
    protected void validateMaxLength(ValidationContext<?> context, String toInspect, int maximum, String name) {
        if (toInspect == null) {
            return;
        }
        if (toInspect.length() <= maximum) {
            return;
        }
        addErrorMessage(context,name + " may not be larger than " + maximum);
    }
    
    protected void validateMinLength(ValidationContext<?> context, String toInspect, int minimum, String name) {
        if (toInspect == null) {
            return;
        }
        if (toInspect.length() >= minimum) {
            return;
        }
        addErrorMessage(context,name + " may not be smaller than " + minimum);
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
        addErrorMessage(context,failureMessage);
    }

    /**
     * Adds error message to context. Will use configured error prefix
     * - when error prefix is "hello:" and message is "world" the error message will be "hello:world"
     * @param context
     * @param message  
     */
    protected final void addErrorMessage(ValidationContext<?> context, String message) {
        context.addError(config.errorPrefix, message);
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
