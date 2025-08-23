// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.commons.model.JSONConverter;

@Component
public class OpenAIResultJsonToExplanationResponseTransformer {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIResultJsonToExplanationResponseTransformer.class);

    public SecHubExplanationResponse buildExplanationResponse(String json) {
        if (json == null || json.isEmpty()) {
            logger.debug("No body defined");
            return null;
        }

        OpenAIResultObject result = JSONConverter.get().fromJSON(OpenAIResultObject.class, json);
        if (logger.isTraceEnabled()) {
            logger.trace("OpenAIResult json beautified:\n{}", JSONConverter.get().toJSON(result, true));
        }

        List<OpenAIResultChoice> choices = result.getChoices();
        if (!choices.isEmpty()) {
            String content = choices.iterator().next().getMessage().getContent();
            if (content != null && !content.isBlank()) {
                return transform(content);
            } else {
                logger.warn("Open ai called, but content was empty!");
            }
        } else {
            logger.warn("Open ai called, but no choices available!");
        }
        return null;
    }

    private SecHubExplanationResponse transform(String content) {
        SecHubExplanationResponse explainResponse = JSONConverter.get().fromJSON(SecHubExplanationResponse.class, content);
        if (logger.isTraceEnabled()) {
            logger.trace("Transformed to content: {}", JSONConverter.get().toJSON(explainResponse, true));
        }
        return explainResponse;
    }

}
