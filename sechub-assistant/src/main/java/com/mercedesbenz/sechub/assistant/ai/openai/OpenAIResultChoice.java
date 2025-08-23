// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Part of OpenAI result object representation - please look at <a href=
 * "https://platform.openai.com/docs/guides/completions#completions-response-format">Completion
 * response format</a> for details
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIResultChoice {
    private OpenAIMessageObject message;

    public OpenAIMessageObject getMessage() {
        return message;
    }

    public void setMessage(OpenAIMessageObject message) {
        this.message = message;
    }
}