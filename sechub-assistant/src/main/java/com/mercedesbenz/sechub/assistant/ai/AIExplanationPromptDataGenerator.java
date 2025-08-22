// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai;

import com.mercedesbenz.sechub.assistant.SecHubExplanationInput;

public interface AIExplanationPromptDataGenerator {

    public AIPromptData createExplanationPromptData(SecHubExplanationInput input);
}
