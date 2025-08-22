// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

public interface OpenAIProblemHandler {

    /**
     * Handles the given runtime exception. Implementations will never throw any
     * exception here!
     *
     * @param e runtime exception
     */
    void handleExplanationProblem(RuntimeException e);

}