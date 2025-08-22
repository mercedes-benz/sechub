// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.assistant.SecHubExplanationInput;

public interface AIChat {

    /**
     * Tries to explain given input. If the chat is not configured correctly or not
     * able to handle the input, the result will be <code>null</code>
     *
     * @param input explanation input data
     * @return result or <code>null</code>
     */
    SecHubExplanationResponse explain(SecHubExplanationInput input);

    /**
     *
     * @return <code>true</code> when chat is enabled (means configured and usable)
     */
    boolean isEnabled();
}
