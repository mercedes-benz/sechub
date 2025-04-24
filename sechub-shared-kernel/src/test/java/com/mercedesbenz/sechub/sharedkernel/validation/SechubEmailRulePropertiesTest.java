// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = SechubEmailRuleConfiguration.class)
@TestPropertySource(properties = { "sechub.email.rule.allowed-domains=example.org,test.com" })
class SechubEmailRulePropertiesTest {

    private final SechubEmailRuleProperties properties;

    @Autowired
    private SechubEmailRulePropertiesTest(SechubEmailRuleProperties properties) {
        this.properties = properties;
    }

    @Test
    void list_of_allowed_domains_is_created_correctly() {
        /* test */
        assertThat(properties).isNotNull();
        assertThat(properties.getAllowedDomains()).containsOnly("example.org", "test.com");
    }

}
