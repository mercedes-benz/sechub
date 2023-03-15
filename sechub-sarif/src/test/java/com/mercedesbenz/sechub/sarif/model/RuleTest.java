// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

import static com.mercedesbenz.sechub.test.PojoTester.*;

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
        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setGuid("25361018-c7c6-11ec-9fb2-f3f888797467")));
        testBothAreNOTEqual(createExample(), change(createExample(), (rule) -> rule.setHelpUri("https://www.anotherUri.com")));
        /* @formatter:on */

    }

    private Rule createExample() {
        Rule rule = new Rule();

        rule.setId("123");
        rule.setGuid("d84e9e96-c7c5-11ec-be2f-9ff76f29cb3b");
        rule.setName("rule-name");
        rule.setShortDescription(new Message());
        rule.setFullDescription(new Message());
        rule.setHelpUri("https://www.myUri.com");
        rule.setHelp(new Message());
        rule.setProperties(new PropertyBag());

        return rule;
    }

}
