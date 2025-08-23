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
public class OpenAIMessageObject {

    private String role;
    private String content;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
