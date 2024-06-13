// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

public interface PrepareInputValidator {

    void validate(PrepareWrapperContext context) throws PrepareWrapperInputValidatorException;
}
