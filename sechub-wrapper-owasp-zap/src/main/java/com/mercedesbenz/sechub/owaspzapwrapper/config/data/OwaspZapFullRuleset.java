// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.data;

import java.util.LinkedList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.MustExitRuntimeException;

public class OwaspZapFullRuleset implements JSONable<OwaspZapFullRuleset> {

    private String timestamp;
    private String origin;
    private List<Rule> rules;

    public OwaspZapFullRuleset() {
        this.rules = new LinkedList<>();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     *
     * @return list of RuleReference objects or <code>null</code> if not set.
     */
    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     *
     * @return Rule object or <code>null</code> if not found.
     */
    public Rule findRuleByRef(String ref) {
        if (ref == null) {
            throw new MustExitRuntimeException("Rule ref to be search for must not be 'null'!", MustExitCode.SCAN_RULE_ERROR);
        }
        if (rules == null) {
            throw new MustExitRuntimeException("Full ruleset must not be 'null', otherwise this cannot be search for ref: " + ref,
                    MustExitCode.SCAN_RULE_ERROR);
        }

        for (Rule rule : rules) {
            if (ref.equals(rule.getRef())) {
                return rule;
            }
        }
        throw new MustExitRuntimeException(
                "Rule could not be found inside full ruleset. Check installed ruleset for missing rules and specified rules to deactivate for wrong specifications.",
                MustExitCode.SCAN_RULE_ERROR);
    }

    @Override
    public Class<OwaspZapFullRuleset> getJSONTargetClass() {
        return OwaspZapFullRuleset.class;
    }

}
