// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.data;

import static com.mercedesbenz.sechub.test.PojoTester.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

class DeactivatedRuleReferencesTest {

    @Test
    void test_getters_and_setters() {
        /* test */
        testSetterAndGetter(createExample());
    }

    private DeactivatedRuleReferences createExample() {
        DeactivatedRuleReferences deactivatedRuleReferences = new DeactivatedRuleReferences();

        RuleReference ruleRef = new RuleReference();
        ruleRef.setRef("rule-ref");
        ruleRef.setInfo("Rule was deactivated for testing reasons.");
        List<RuleReference> references = new LinkedList<>();
        references.add(ruleRef);
        deactivatedRuleReferences.setDeactivatedRuleReferences(references);
        return deactivatedRuleReferences;
    }

}
