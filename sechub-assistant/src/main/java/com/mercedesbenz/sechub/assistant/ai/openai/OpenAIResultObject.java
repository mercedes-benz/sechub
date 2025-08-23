// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * OpenAI result object representation - please look at <a href=
 * "https://platform.openai.com/docs/guides/completions#completions-response-format">Completion
 * response format</a> for details
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIResultObject {

    private List<OpenAIResultChoice> choices = new ArrayList<>();

    public List<OpenAIResultChoice> getChoices() {
        return choices;
    }
}
