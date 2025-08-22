// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIResultObject {

    private List<OpenAIResultChoice> choices = new ArrayList<>();

    public List<OpenAIResultChoice> getChoices() {
        return choices;
    }
}
