// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.data;

import static com.mercedesbenz.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class RuleTest {

    @Test
    void test_getters_and_setters() {
        /* test */
        testSetterAndGetter(createExample());
    }

    private Rule createExample() {
        Rule rule = new Rule();

        rule.setId("12345");
        rule.setName("rule-name");
        rule.setType("active");
        rule.setLink("link-to-rule");
        return rule;
    }

}
