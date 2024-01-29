// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config.data;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeactivatedRuleReferencesTest {

    private DeactivatedRuleReferences deactivatedRuleReferencesToTest;

    @BeforeEach
    void beforeEach() {
        deactivatedRuleReferencesToTest = new DeactivatedRuleReferences();
    }

    @Test
    void adding_to_list_results_in_list_containing_added_values() {
        /* prepare */
        String reference = "reference";
        String info = "Info about rule reference";
        RuleReference ruleRef = new RuleReference(reference, info);

        /* execute */
        deactivatedRuleReferencesToTest.addRuleReference(ruleRef);

        /* test */
        Iterator<RuleReference> iterator = deactivatedRuleReferencesToTest.getDeactivatedRuleReferences().iterator();
        assertTrue(iterator.hasNext());

        RuleReference ruleRefFromList = iterator.next();
        assertEquals(reference, ruleRefFromList.getReference());
        assertEquals(info, ruleRefFromList.getInfo());

        assertFalse(iterator.hasNext());

    }

}
