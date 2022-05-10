// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.data;

import java.util.LinkedList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class DeactivatedRuleReferences implements JSONable<DeactivatedRuleReferences> {

    private List<RuleReference> deactivatedRuleReferences;

    public DeactivatedRuleReferences() {
        this.deactivatedRuleReferences = new LinkedList<>();
    }

    /**
     *
     * @return list of RuleReference objects or <code>null</code> if not set.
     */
    public List<RuleReference> getDeactivatedRuleReferences() {
        return deactivatedRuleReferences;
    }

    public void setDeactivatedRuleReferences(List<RuleReference> deactivatedRuleReferences) {
        this.deactivatedRuleReferences = deactivatedRuleReferences;
    }

    @Override
    public Class<DeactivatedRuleReferences> getJSONTargetClass() {
        return DeactivatedRuleReferences.class;
    }

}
