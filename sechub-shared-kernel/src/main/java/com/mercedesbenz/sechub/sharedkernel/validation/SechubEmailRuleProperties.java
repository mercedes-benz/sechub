// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

@MustBeDocumented(scope = "Email validation")
@ConfigurationProperties(prefix = SechubEmailRuleProperties.PREFIX)
public class SechubEmailRuleProperties {
    static final String PREFIX = "sechub.email.rule";

    private final List<String> allowedDomains;

    public SechubEmailRuleProperties(List<String> allowedDomains) {
        this.allowedDomains = allowedDomains;
    }

    public List<String> getAllowedDomains() {
        return allowedDomains != null ? Collections.unmodifiableList(allowedDomains) : null;
    }
}