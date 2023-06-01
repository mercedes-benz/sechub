// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

public interface ResilienceConsultant {

    /**
     * Gives a consultation for current situation
     *
     * @param context represents current situation
     * @return a proposal or <code>null</code>
     */
    public ResilienceProposal consultFor(ResilienceContext context);

}
