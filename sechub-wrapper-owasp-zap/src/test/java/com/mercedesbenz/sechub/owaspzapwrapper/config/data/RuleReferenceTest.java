// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.data;

import static com.mercedesbenz.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class RuleReferenceTest {

    @Test
    void test_getters_and_setters() {
        /* test */
        testSetterAndGetter(createExample());
    }

    private RuleReference createExample() {
        RuleReference ruleReference = new RuleReference();

        ruleReference.setInfo("rule-ref-info");
        ruleReference.setReference("rule-ref");
        return ruleReference;
    }

}
