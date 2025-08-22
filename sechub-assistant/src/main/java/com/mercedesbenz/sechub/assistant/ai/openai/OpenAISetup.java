// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import java.net.URI;

public interface OpenAISetup {

    URI getCompletionsUri();

    String getApiToken();

    boolean isEnabled();

    String getModel();

}