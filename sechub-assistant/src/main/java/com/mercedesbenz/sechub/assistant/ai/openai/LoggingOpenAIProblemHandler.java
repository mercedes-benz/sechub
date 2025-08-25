// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingOpenAIProblemHandler implements OpenAIProblemHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoggingOpenAIProblemHandler.class);

    @Override
    public void handleExplanationProblem(RuntimeException e) {
        /* we log without stack trace to not flood the logs... */
        logger.error("Open AI explanation failed. Reason={}:{}", e.getClass().getSimpleName(), e.getMessage());
    }

}
