// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.assistant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.api.internal.gen.model.CodeExample;
import com.mercedesbenz.sechub.api.internal.gen.model.Reference;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubExplanationResponse;

public class FallbackExplanationReponseFactoryTest {

    private FallbackExplanationReponseFactory factoryToTest;
    private SecHubExplanationInput input;

    @BeforeAll
    static void beforeAll() {

    }

    @BeforeEach
    void beforeEach() {
        factoryToTest = new FallbackExplanationReponseFactory();
        input = mock(SecHubExplanationInput.class);
    }

    @Test
    void create_explanation_response_finding_explanation_title_is_set_from_input() {
        /* prepare */
        String findingName = "Test Finding";
        when(input.getFindingName()).thenReturn(findingName);

        /* execute + test */
        SecHubExplanationResponse response = factoryToTest.createExplanationResponse(input);

        /* test */
        assertThat(response.getFindingExplanation().getTitle()).isEqualTo(findingName);
    }

    @Test
    void create_explanation_response_finding_explanation_content_is_set_from_input() {
        /* prepare */
        String findingDescription = "Test Description";
        when(input.getFindingDescription()).thenReturn(findingDescription);

        /* execute + test */
        SecHubExplanationResponse response = factoryToTest.createExplanationResponse(input);

        /* test */
        assertThat(response.getFindingExplanation().getContent()).isEqualTo(findingDescription);
    }

    @Test
    void create_explanation_response_potential_impact_title_is_set_to_potential_impact() {
        /* execute + test */
        SecHubExplanationResponse response = factoryToTest.createExplanationResponse(input);

        /* test */
        assertThat(response.getPotentialImpact().getTitle()).isEqualTo("Potential Impact");
    }

    @Test
    void create_explanation_response_potential_impact_content_is_set_to_no_explicit_information_available() {
        /* execute + test */
        SecHubExplanationResponse response = factoryToTest.createExplanationResponse(input);

        /* test */
        assertThat(response.getPotentialImpact().getContent()).isEqualTo("No explicit information available. Please look at references");
    }

    @Test
    void create_explanation_response_recommendations_is_empty_list() {
        /* execute + test */
        SecHubExplanationResponse response = factoryToTest.createExplanationResponse(input);

        /* test */
        assertThat(response.getRecommendations()).isEmpty();
    }

    @Test
    void create_explanation_response_code_example_is_not_null() {
        /* execute + test */
        SecHubExplanationResponse response = factoryToTest.createExplanationResponse(input);

        /* test */
        assertThat(response.getCodeExample()).isNotNull().isInstanceOf(CodeExample.class);
    }

    @Test
    void create_explanation_response_references_is_empty_when_cwe_id_is_null() {
        /* prepare */
        when(input.getCweId()).thenReturn(null);

        /* execute + test */
        SecHubExplanationResponse response = factoryToTest.createExplanationResponse(input);

        /* test */
        assertThat(response.getReferences()).isEmpty();
    }

    @Test
    void create_explanation_response_references_contains_cwe_reference_when_cwe_id_is_not_null() {
        /* prepare */
        Integer cweId = 123;
        String findingName = "Test Finding";
        when(input.getCweId()).thenReturn(cweId);
        when(input.getFindingName()).thenReturn(findingName);

        /* execute + test */
        SecHubExplanationResponse response = factoryToTest.createExplanationResponse(input);

        /* test */
        List<Reference> references = response.getReferences();
        assertThat(references).hasSize(1);

        Reference reference = references.get(0);
        assertThat(reference.getTitle()).isEqualTo("CWE-" + cweId + " - " + findingName);
        assertThat(reference.getUrl()).isEqualTo("https://cwe.mitre.org/data/definitions/" + cweId + ".html");

    }

}