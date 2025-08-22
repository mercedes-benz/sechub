// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import org.springframework.web.client.RestTemplate;

public interface OpenAIChatRestTemplateFactory {

    RestTemplate createRestTemplate();

}