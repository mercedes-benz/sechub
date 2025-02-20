// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

/**
 * Interface for objects that can validate themselves after JSON deserialization
 */
public interface JSONValidatable {
    /**
     * Validates the object's state after deserialization
     * @throws JSONValidationException if validation fails
     */
    void validate() throws JSONValidationException;
}
