// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

public class ValidationContext<T> {

    ValidationResult result = new ValidationResult();
    T objectToValidate;

    ValidationContext(T target) {
        this.objectToValidate = target;
    }

    public void addError(String prefix, String error) {
        if (error == null) {
            return;
        }
        if (prefix == null) {
            result.addError(error);
        } else {
            result.addError(prefix + error);
        }
    }

    public boolean isInValid() {
        return !result.valid;
    }

    public void addErrors(ValidationResult otherResult) {
        if (otherResult == null) {
            return;
        }
        result.addErrors(otherResult);
    }

}