// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

public interface EmailRule {

    void applyRule(String email, ValidationContext<String> context);
}
