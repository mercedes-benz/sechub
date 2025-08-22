// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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