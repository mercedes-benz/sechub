// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant;

import static java.util.Objects.*;

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

    private final FallbackExplanationResponseFactory fallbackExplanationResponseFactory;

    private final SecHubExplanationInputCollector inputCollector;

    public FindingAssistantService(SecHubExplanationInputCollector inputCollector, List<AIChat> chats,
            FallbackExplanationResponseFactory fallbackExplanationResponseFactory) {
        this.inputCollector = inputCollector;
        this.chats = chats;
        this.fallbackExplanationResponseFactory = fallbackExplanationResponseFactory;

        logger.info("Found {} AI chats:", chats.size());
        for (AIChat chat : chats) {
            logger.info("- {}, enabled={}", chat.getClass().getSimpleName(), chat.isEnabled());
        }
    }

    public SecHubExplanationResponse createSecHubExplanationResponse(String projectId, UUID jobUUID, int findingId) {
        requireNonNull(projectId, "Project id must be not null!");
        requireNonNull(jobUUID, "Job UUID must not be null!");

        SecHubExplanationInput input = inputCollector.collectInput(projectId, jobUUID, findingId);
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
                    logger.trace("Chat {} returned result:\n{}", chat.getClass().getName(), JSONConverter.get().toJSON(result, true));
                }
                /* we use first result */
                break;
            } else {
                logger.warn("Chat {} returned no result!", chat.getClass().getName());
            }

        }

        if (result == null) {
            /* no result available we need fallback data */
            logger.debug("None of the {} available chats was able to respond, create fallback response", chats.size());
            result = fallbackExplanationResponseFactory.createExplanationResponse(input);
        }

        return result;
    }

}
