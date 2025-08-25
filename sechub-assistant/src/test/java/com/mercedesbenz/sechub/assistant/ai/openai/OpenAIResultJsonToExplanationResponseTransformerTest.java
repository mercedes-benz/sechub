// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant.ai.openai;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;
import com.mercedesbenz.sechub.test.TestFileReader;

class OpenAIResultJsonToExplanationResponseTransformerTest {

    private OpenAIResultJsonToExplanationResponseTransformer toTest;

    @BeforeEach
    void beforeEach() {
        toTest = new OpenAIResultJsonToExplanationResponseTransformer();
    }

    @Test
    void test_openai_result1_can_be_read_and_explanation_response_can_be_fetched_from_there() {
        /* prepare */
        String json = TestFileReader.readTextFromFile("src/test/resources/openai-completion-test-result1.json");

        /* execute */
        SecHubExplanationResponse response = toTest.buildExplanationResponse(json);

        /* test */
        assertThat(response).isNotNull();
        assertThat(response.getPotentialImpact().getTitle()).contains("Potential Impact");
        assertThat(response.getPotentialImpact().getContent()).contains("An attacker could use this vulnerability to execute arbitrary SQL queries");

        assertThat(response.getReferences()).isNotEmpty();
    }
}