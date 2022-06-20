// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

public class OwaspZapFullRuleset implements JSONable<OwaspZapFullRuleset> {

    private String timestamp;
    private String origin;
    private Map<String, Rule> rules;

    public OwaspZapFullRuleset() {
        this.rules = new HashMap<>();
    }

    /**
     *
     * @return timestamp or <code>null</code> if not set.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @return origin or <code>null</code> if not set.
     */
    public String getOrigin() {
        return origin;
    }

    /**
     *
     * @return list of RuleReference objects or <code>null</code> if not set.
     */
    public Map<String, Rule> getRules() {
        return Collections.unmodifiableMap(rules);
    }

    /**
     *
     * @param reference of the rule to search for inside <code>rules</code>
     * @return rule found by the parameter <code>reference</code>
     *
     * @throws MustExitRuntimeException if either parameter <code>reference</code>
     *                                  or attribute <code>rules</code> is
     *                                  <code>null</code> or if the rule that was
     *                                  search for was not found inside
     *                                  <code>rules</code>.
     */
    public Rule findRuleByReference(String reference) {
        if (reference == null) {
            throw new MustExitRuntimeException("Rule reference to be search for must not be 'null'!", MustExitCode.SCAN_RULE_ERROR);
        }
        if (rules == null) {
            throw new MustExitRuntimeException("Full ruleset must not be 'null', otherwise this cannot be search for reference: " + reference,
                    MustExitCode.SCAN_RULE_ERROR);
        }

        Rule rule = rules.getOrDefault(reference, null);
        if (rule == null) {
            throw new MustExitRuntimeException(
                    "Rule could not be found inside full ruleset. Check installed ruleset for missing rules and specified rules to deactivate for wrong specifications.",
                    MustExitCode.SCAN_RULE_ERROR);
        }
        return rule;
    }

    @Override
    public Class<OwaspZapFullRuleset> getJSONTargetClass() {
        return OwaspZapFullRuleset.class;
    }

}
