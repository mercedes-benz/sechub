// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

public interface InputValidator {
    void validate(PrepareWrapperContext context) throws PrepareWrapperInputValidatorException;
}
