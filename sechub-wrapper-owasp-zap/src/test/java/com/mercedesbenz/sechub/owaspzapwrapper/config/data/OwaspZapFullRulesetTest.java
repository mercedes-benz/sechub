// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.data;

import static com.mercedesbenz.sechub.test.PojoTester.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

class OwaspZapFullRulesetTest {

    private OwaspZapFullRuleset fullRuleSetToTest;

    @BeforeEach
    void beforeEach() {
        fullRuleSetToTest = createExample();
    }

    @Test
    void test_getters_and_setters() {
        /* test */
        testSetterAndGetter(fullRuleSetToTest);
    }

    @Test
    void rule_not_found_throws_mustexitruntimeexception() {
        /* execute + test */
        assertThrows(MustExitRuntimeException.class, () -> fullRuleSetToTest.findRuleByReference("wrong-ref"));

    }

    @Test
    void rule_ref_is_null_throws_mustexitruntimeexception() {
        /* execute + test */
        assertThrows(MustExitRuntimeException.class, () -> fullRuleSetToTest.findRuleByReference(null));

    }

    @Test
    void rules_is_null_throws_mustexitruntimeexception() {
        /* prepare */
        fullRuleSetToTest.setRules(null);

        /* execute + test */
        assertThrows(MustExitRuntimeException.class, () -> fullRuleSetToTest.findRuleByReference("rule-ref"));

    }

    @Test
    void rule_found_returns_correct_rule() {
        /* execute */
        Rule rule = fullRuleSetToTest.findRuleByReference("rule-ref");

        /* test */
        assertNotNull(rule);
        assertEquals("12345", rule.getId());
        assertEquals("rule-name", rule.getName());
        assertEquals("active", rule.getType());
        assertEquals("link-to-rule", rule.getLink());

    }

    private OwaspZapFullRuleset createExample() {
        OwaspZapFullRuleset fullRuleset = new OwaspZapFullRuleset();

        fullRuleset.setOrigin("link-to-origin");
        fullRuleset.setTimestamp("timestamp");
        Map<String, Rule> rules = new HashMap<>();
        Rule rule = new Rule();
        rule.setId("12345");
        rule.setName("rule-name");
        rule.setType("active");
        rule.setLink("link-to-rule");
        rules.put("rule-ref", rule);
        fullRuleset.setRules(rules);
        return fullRuleset;
    }

}
