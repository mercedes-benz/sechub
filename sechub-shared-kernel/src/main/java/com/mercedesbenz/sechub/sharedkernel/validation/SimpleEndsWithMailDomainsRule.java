// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;

@Component
public class SimpleEndsWithMailDomainsRule implements EmailRule {

    private final SechubEmailRuleProperties sechubEmailRuleProperties;

    public SimpleEndsWithMailDomainsRule(SechubEmailRuleProperties sechubEmailRuleProperties) {
        this.sechubEmailRuleProperties = requireNonNull(sechubEmailRuleProperties, "The value of 'sechubEmailRuleProperties' must not be null!");
    }

    @Override
    public void applyRule(String email, ValidationContext<String> context) {
        if (sechubEmailRuleProperties.getAllowedDomains() == null || sechubEmailRuleProperties.getAllowedDomains().isEmpty()) {
            return;
        }
        if (email == null || context == null) {
            return;
        }
        for (String allowedDomain : sechubEmailRuleProperties.getAllowedDomains()) {
            if (!allowedDomain.startsWith("@")) {
                allowedDomain = "%s%s".formatted("@", allowedDomain);
            }
            if (email.endsWith(allowedDomain)) {
                return;
            }
        }
        context.addError(null, "The email address did not end with any of the allowed domains: %s".formatted(sechubEmailRuleProperties.getAllowedDomains()));
    }
}