// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

@MustBeDocumented(scope = "Email validation")
@ConfigurationProperties(prefix = SechubEmailRuleProperties.PREFIX)
public class SechubEmailRuleProperties {
    static final String PREFIX = "sechub.email.rule";

    private final List<String> allowedDomains;

    @ConstructorBinding
    public SechubEmailRuleProperties(List<String> allowedDomains) {
        this.allowedDomains = allowedDomains != null ? Collections.unmodifiableList(allowedDomains) : Collections.emptyList();
        ;
    }

    /**
     * Get the list of allowed domains for an email address.
     *
     * @return list of allowed domains or an empty list, never <code>code</code>
     */
    public List<String> getAllowedDomains() {
        return allowedDomains;
    }
}