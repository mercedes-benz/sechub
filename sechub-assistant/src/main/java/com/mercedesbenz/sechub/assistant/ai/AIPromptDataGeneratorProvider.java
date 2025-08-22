// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AIPromptDataGeneratorProvider {

    @Autowired
    DefaultAIExplanationPromptDataGenerator defaultExplanationPromptDataGenerator;

    public AIExplanationPromptDataGenerator getExplanationPromptGenerator(AIChat chat) {
        // currently we always return the default prompt generator
        return defaultExplanationPromptDataGenerator;
    }

}
