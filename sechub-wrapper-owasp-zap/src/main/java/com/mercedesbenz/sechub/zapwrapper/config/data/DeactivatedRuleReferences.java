// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class DeactivatedRuleReferences implements JSONable<DeactivatedRuleReferences> {

    private List<RuleReference> deactivatedRuleReferences;

    public DeactivatedRuleReferences() {
        this.deactivatedRuleReferences = new LinkedList<>();
    }

    /**
     * Add a RuleReference to the list of rule references.
     *
     * @param ruleReference
     */
    public void addRuleReference(RuleReference ruleReference) {
        deactivatedRuleReferences.add(ruleReference);
    }

    /**
     *
     * @return list of RuleReference objects or <code>null</code> if not set.
     */
    public List<RuleReference> getDeactivatedRuleReferences() {
        return Collections.unmodifiableList(deactivatedRuleReferences);
    }

    @Override
    public Class<DeactivatedRuleReferences> getJSONTargetClass() {
        return DeactivatedRuleReferences.class;
    }

}
