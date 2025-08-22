// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.assistant.ai.AIChat;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

@Service
public class FindingAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(FindingAssistantService.class);

    final List<AIChat> chats;

    private FallbackExplanationReponseFactory fallbackExplanationReponseFactory;

    private SecHubExplanationInputCollector inputCollector;

    public FindingAssistantService(SecHubExplanationInputCollector inputCollector, List<AIChat> chats,
            FallbackExplanationReponseFactory fallbackExplanationReponseFactory) {
        this.inputCollector = inputCollector;
        this.chats = chats;
        this.fallbackExplanationReponseFactory = fallbackExplanationReponseFactory;

        logger.info("Assistent service found {} ai chats:");
        for (AIChat chat : chats) {
            logger.info("- {}, enabled={}", chat.getClass().getSimpleName(), chat.isEnabled());
        }
    }

    public SecHubExplanationResponse createSecHubExplanationResponse(String projectId, UUID jobUUID, int findingId) {

        SecHubExplanationInput input = inputCollector.collectInputFor(projectId, jobUUID, findingId);
        if (!input.isAvailable()) {
            throw new NotFoundException("The finding is not found or you have no access");
        }

        SecHubExplanationResponse result = null;

        for (AIChat chat : chats) {
            if (!chat.isEnabled()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Chat {} not enabled", chat.getClass().getName());
                }
                continue;
            }

            /* call chat */
            if (logger.isDebugEnabled()) {
                logger.debug("Chat {} is asked for explanation", chat.getClass().getSimpleName());
            }
            result = chat.explain(input);

            /* handle */
            if (result != null) {
                logger.debug("Chat {} has returned explanation", chat.getClass().getSimpleName());
                if (logger.isTraceEnabled()) {
                    logger.trace("Chat {} returned result:\n", chat.getClass().getName(), JSONConverter.get().toJSON(result, true));
                }
                /* we use first result */
                break;
            } else {
                logger.warn("Chat {} returned no result!", chat.getClass().getName());
            }

        }

        if (result == null) {
            /* no result available we need fallback data */
            logger.debug("None of the {} available chats was able to respond, create fallback reponse", chats.size());
            result = fallbackExplanationReponseFactory.createExplanationResponse(input);
        }

        return result;
    }

}
