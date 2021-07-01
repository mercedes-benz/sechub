// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import static com.daimler.sechub.test.PojoTester.*;

import org.junit.jupiter.api.Test;

class RuleTest {

    @Test
    void test_setter() {
        testSetterAndGetter(createExample());
    }

    @Test
    void test_equals_and_hashcode() {
        /* @formatter:off */
        testBothAreEqualAndHaveSameHashCode(createExample(), createExample());

        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setShortDescription(new Message("other"))));
        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setProperties(null)));
        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setName("other")));
        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setId("other")));
        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setHelp(new Message("other"))));
        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setFullDescription(new Message("other"))));
        /* @formatter:on */

    }

    private Rule createExample() {
        Rule rule = new Rule();

        rule.setId("123");
        rule.setName("rule-name");
        rule.setShortDescription(new Message());
        rule.setFullDescription(new Message());
        rule.setHelp(new Message());
        rule.setProperties(new PropertyBag());

        return rule;
    }

}
