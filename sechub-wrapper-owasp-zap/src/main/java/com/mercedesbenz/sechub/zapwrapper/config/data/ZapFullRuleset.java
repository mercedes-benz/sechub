// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

public class ZapFullRuleset implements JSONable<ZapFullRuleset> {

    private String timestamp;
    private String origin;
    private Map<String, Rule> rules;

    public ZapFullRuleset() {
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
     * @throws ZapWrapperRuntimeException if either parameter <code>reference</code>
     *                                    or attribute <code>rules</code> is
     *                                    <code>null</code> or if the rule that was
     *                                    search for was not found inside
     *                                    <code>rules</code>.
     */
    public Rule findRuleByReference(String reference) {
        if (reference == null) {
            throw new ZapWrapperRuntimeException("Rule reference to be search for must not be 'null'!", ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        if (rules == null) {
            throw new ZapWrapperRuntimeException("Full ruleset must not be 'null', otherwise this cannot be search for reference: " + reference,
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }

        Rule rule = rules.getOrDefault(reference, null);
        if (rule == null) {
            throw new ZapWrapperRuntimeException(
                    "Rule could not be found inside full ruleset. Check installed ruleset for missing rules and specified rules to deactivate for wrong specifications.",
                    ZapWrapperExitCode.PDS_CONFIGURATION_ERROR);
        }
        return rule;
    }

    @Override
    public Class<ZapFullRuleset> getJSONTargetClass() {
        return ZapFullRuleset.class;
    }

}
