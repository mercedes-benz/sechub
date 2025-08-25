// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BufferedClientHttpRequestOpenAIChatRestTemplateFactory implements OpenAIChatRestTemplateFactory {

    @Override
    public RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        BufferingClientHttpRequestFactory bufferedRequestFactory = new BufferingClientHttpRequestFactory(requestFactory);

        return new RestTemplate(bufferedRequestFactory);
    }

}
