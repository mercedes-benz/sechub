// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mercedesbenz.sechub.zapwrapper.config.data.DeactivatedRuleReferences;
import com.mercedesbenz.sechub.zapwrapper.config.data.ZapFullRuleset;

class RuleProviderTest {

    private RuleProvider rulesProvider;

    @BeforeEach
    void beforeEach() {
        rulesProvider = new RuleProvider();
    }

    @ParameterizedTest
    @MethodSource("invalidParams")
    void null_as_files_returns_new_empty_objects(File file) {
        /* execute */
        ZapFullRuleset fullRuleset = rulesProvider.fetchFullRuleset(file);
        DeactivatedRuleReferences deactivatedRuleReferences = rulesProvider.fetchDeactivatedRuleReferences(file);

        /* test */
        assertNotNull(fullRuleset);
        assertEquals(null, fullRuleset.getOrigin());
        assertEquals(null, fullRuleset.getTimestamp());
        assertNotNull(fullRuleset.getRules());
        assertTrue(fullRuleset.getRules().isEmpty());

        assertNotNull(deactivatedRuleReferences);
        assertNotNull(deactivatedRuleReferences.getDeactivatedRuleReferences());
        assertTrue(deactivatedRuleReferences.getDeactivatedRuleReferences().isEmpty());
    }

    @Test
    void valid_fullruleset_file_returns_valid_object() {
        /* prepare */
        File testFile = new File("src/test/resources/zap-available-rules/zap-full-ruleset.json");

        /* execute */
        ZapFullRuleset fullRuleset = rulesProvider.fetchFullRuleset(testFile);

        /* test */
        assertNotNull(fullRuleset);
        assertEquals("https://www.zaproxy.org/docs/alerts/", fullRuleset.getOrigin());
        assertEquals("2022-05-13 14:44:00.635104", fullRuleset.getTimestamp());
        assertNotNull(fullRuleset.getRules());
        assertEquals(146, fullRuleset.getRules().size());
    }

    @Test
    void valid_deactivatedrulereferences_file_returns_valid_object() {
        /* prepare */
        File testFile = new File("src/test/resources/wrapper-deactivated-rule-examples/zap-rules-to-deactivate.json");

        /* execute */
        DeactivatedRuleReferences deactivatedRuleReferences = rulesProvider.fetchDeactivatedRuleReferences(testFile);

        /* test */
        assertNotNull(deactivatedRuleReferences);
        assertNotNull(deactivatedRuleReferences.getDeactivatedRuleReferences());
        assertEquals(2, deactivatedRuleReferences.getDeactivatedRuleReferences().size());
    }

    private static Stream<String> invalidParams() {
        return Stream.of("not-existing-file.json", null);
    }

}
