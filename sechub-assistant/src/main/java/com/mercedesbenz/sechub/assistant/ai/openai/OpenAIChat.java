// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.assistant.SecHubExplanationInput;
import com.mercedesbenz.sechub.assistant.ai.AIChat;
import com.mercedesbenz.sechub.assistant.ai.AIExplanationPromptDataGenerator;
import com.mercedesbenz.sechub.assistant.ai.AIPromptData;
import com.mercedesbenz.sechub.assistant.ai.AIPromptDataGeneratorProvider;
import com.mercedesbenz.sechub.commons.model.JSONConverter;

@Component
@EnableConfigurationProperties(OpenAIProperties.class)
public class OpenAIChat implements AIChat {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIChat.class);

    private final OpenAISetup properties;

    private final AIPromptDataGeneratorProvider promptGeneratorProvider;

    private final OpenAIChatRestTemplateFactory restTemplateFactory;

    private final OpenAIProblemHandler problemHandler;

    private final OpenAIResultJsonToExplanationResponseTransformer beautifier;

    OpenAIChat(AIPromptDataGeneratorProvider promptGeneratorFactory, OpenAISetup properties, OpenAIChatRestTemplateFactory restTemplateFactory,
            OpenAIProblemHandler problemHandler, OpenAIResultJsonToExplanationResponseTransformer beautifier) {
        this.properties = properties;
        this.promptGeneratorProvider = promptGeneratorFactory;
        this.restTemplateFactory = restTemplateFactory;
        this.problemHandler = problemHandler;
        this.beautifier = beautifier;
    }

    private class BearerTokenInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            HttpHeaders headers = request.getHeaders();
            headers.setBearerAuth(properties.getApiToken());
            return execution.execute(request, body);
        }
    }

    @Override
    public SecHubExplanationResponse explain(SecHubExplanationInput input) {
        if (input == null) {
            logger.warn("Input is null, cannot explain");
            return null;
        }
        URI completionsURI = properties.getCompletionsUri();
        if (completionsURI == null) {
            logger.warn("completionsURI is null, cannot explain");
            return null;
        }

        OpenAIMessageObject[] messages = createMessages(input);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> openAIRequestMap = new HashMap<>();

        String model = properties.getModel();

        if (model != null && !model.isBlank()) {
            openAIRequestMap.put("model", model);
        }
        openAIRequestMap.put("messages", messages);

        try {

            String json = JSONConverter.get().toJSON(openAIRequestMap);
            logger.debug("Open AI json input:\n{}", json);

            HttpEntity<String> request = new HttpEntity<String>(json, headers);
            logger.debug("Fetch explanation from open ai URL: {}", completionsURI);

            RestTemplate restTemplate = restTemplateFactory.createRestTemplate();
            restTemplate.getInterceptors().add(new BearerTokenInterceptor());

            ResponseEntity<String> response = restTemplate.postForEntity(completionsURI, request, String.class);

            String body = response.getBody();
            logger.debug("Open AI body output:\n{}", body);

            SecHubExplanationResponse explainResponse = beautifier.buildExplanationResponse(body);
            return explainResponse;

        } catch (RuntimeException e) {
            problemHandler.handleExplanationProblem(e);
            return null;

        }

    }

    OpenAIMessageObject[] createMessages(SecHubExplanationInput input) {
        AIExplanationPromptDataGenerator promptGenerator = promptGeneratorProvider.getExplanationPromptGenerator(this);

        AIPromptData data = promptGenerator.createExplanationPromptData(input);

        String user = data.getUser();
        if (user == null) {
            user = "";
        }
        String system = data.getSystem();
        if (system == null) {
            system = "";
        }

        OpenAIMessageObject userMessage = new OpenAIMessageObject();
        userMessage.setRole("user");
        userMessage.setContent(user);

        OpenAIMessageObject systemMessage = new OpenAIMessageObject();
        systemMessage.setRole("system");
        systemMessage.setContent(system);

        return new OpenAIMessageObject[] { systemMessage, userMessage };
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }

}
