// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import com.mercedesbenz.sechub.commons.core.doc.Description;
import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

@MustBeDocumented(scope = "Email validation")
@ConfigurationProperties(prefix = SechubEmailRuleProperties.PREFIX)
public class SechubEmailRuleProperties {
    static final String PREFIX = "sechub.email.rule";

    private final List<String> allowedDomains;

    /* @formatter:off */
    @ConstructorBinding
    public SechubEmailRuleProperties(

            @Description("The allowed domains for email addresses of SecHub users. A comma separated list of strings like: 'example.com,company.org'. If nothing is specified all domains are allowed.")
            List<String> allowedDomains) {

        this.allowedDomains = allowedDomains != null ? Collections.unmodifiableList(allowedDomains) : Collections.emptyList();
    }
    /* @formatter:on */

    /**
     * Get the list of allowed domains for an email address.
     *
     * @return list of allowed domains or an empty list, never <code>code</code>
     */
    public List<String> getAllowedDomains() {
        return allowedDomains;
    }
}