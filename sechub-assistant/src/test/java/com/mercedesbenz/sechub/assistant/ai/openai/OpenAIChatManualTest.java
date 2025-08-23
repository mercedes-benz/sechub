// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.assistant.SecHubExplanationInput;
import com.mercedesbenz.sechub.assistant.ai.AIExplanationPromptDataGenerator;
import com.mercedesbenz.sechub.assistant.ai.AIPromptDataGeneratorProvider;
import com.mercedesbenz.sechub.assistant.ai.DefaultAIExplanationPromptDataGenerator;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.test.ManualTest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

class OpenAIChatManualTest implements ManualTest {

    private static final String ENV_SECHUB_OPENAI_TOKEN = "SECHUB_OPENAI_TOKEN";
    private static final String ENV_SECHUB_OPENAI_COMPLETIONS_URL = "SECHUB_OPENAI_COMPLETIONS_URL";
    private static final String ENV_SECHUB_OPENAI_MODEL = "SECHUB_OPENAPI_MODEL";

    private static final Logger logger = LoggerFactory.getLogger(OpenAIChatManualTest.class);

    @Test
    void connect_with_open_ai_instance_with_prompt_and_show_result_in_console_log() {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger openAIPackageLogger = loggerContext.getLogger(OpenAIChat.class.getPackageName());
        openAIPackageLogger.setLevel(Level.TRACE);

        String model = System.getenv(ENV_SECHUB_OPENAI_MODEL);
        String apiToken = System.getenv(ENV_SECHUB_OPENAI_TOKEN);
        if (apiToken == null) {
            throw new IllegalArgumentException("Cannot execute manual test without api token! Please set in ENV variable:" + ENV_SECHUB_OPENAI_TOKEN);
        }

        String baseUrlAsString = System.getenv(ENV_SECHUB_OPENAI_COMPLETIONS_URL);
        if (baseUrlAsString == null) {
            throw new IllegalArgumentException("Cannot execute manual test without base url! Please set in  ENV variable:" + ENV_SECHUB_OPENAI_COMPLETIONS_URL);
        }
        URI baseUri = URI.create(baseUrlAsString);
        OpenAISetup properties = new OpenAIProperties(apiToken, model, baseUri);

        AIPromptDataGeneratorProvider promptGeneratorProvider = mock();
        AIExplanationPromptDataGenerator promptGenerator = new DefaultAIExplanationPromptDataGenerator();
        when(promptGeneratorProvider.getExplanationPromptGenerator(any(OpenAIChat.class))).thenReturn(promptGenerator);

        OpenAIChatRestTemplateFactory restTemplateFactory = new BufferedClientHttpRequestOpenAIChatRestTemplateFactory();
        OpenAIProblemHandler problemHandler = new LoggingOpenAIProblemHandler();
        OpenAIResultJsonToExplanationResponseTransformer beautifier = new OpenAIResultJsonToExplanationResponseTransformer();
        OpenAIChat chat = new OpenAIChat(promptGeneratorProvider, properties, restTemplateFactory, problemHandler, beautifier);

        SecHubExplanationInput input = new SecHubExplanationInput();
        input.setCweId(69);
        input.setFileName("TestImproperUserInp.java");
        input.setRelevantSource("String htmlOutput = \"<div>User entered following: \" + userInput + \"</div>\";");

        /* execute */
        SecHubExplanationResponse result = chat.explain(input);

        /* test */
        assertThat(result).isNotNull();
        logger.info("Fetched result=\n{}", JSONConverter.get().toJSON(result, true));

        String contentLowerCased = result.getFindingExplanation().getContent().toLowerCase();
        assertThat(contentLowerCased).contains("improper");

    }

}
