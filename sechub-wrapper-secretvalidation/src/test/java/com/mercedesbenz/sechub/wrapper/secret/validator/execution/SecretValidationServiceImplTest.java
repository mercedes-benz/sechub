// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequest;

import de.jcup.sarif_2_1_0.model.ArtifactContent;
import de.jcup.sarif_2_1_0.model.Region;

class SecretValidationServiceImplTest {

    private SecretValidationServiceImpl serviceToTest;

    private static final SecretValidatorWebRequestService webRequestService = mock();

    @BeforeEach
    void beforeEach() {
        Mockito.reset(webRequestService);

        serviceToTest = new SecretValidationServiceImpl(webRequestService);
    }

    @Test
    void region_snippet_is_null_returns_expected_validation_result() {
        /* prepare */
        String ruleId = "rule-id";
        Region region = new Region();
        List<SecretValidatorRequest> requests = new ArrayList<>();
        when(webRequestService.validateFinding(null, ruleId, requests, 0)).thenReturn(new SecretValidationResult());

        /* execute */
        SecretValidationResult validateFindingByRegion = serviceToTest.validateFindingByRegion(region, ruleId, requests, 0);

        /* test */
        verify(webRequestService, never()).validateFinding(null, ruleId, requests, 0);
        assertEquals(SecretValidationStatus.SARIF_SNIPPET_NOT_SET, validateFindingByRegion.getValidationStatus());

    }

    @Test
    void region_snippet_text_is_null_returns_expected_validation_result() {
        /* prepare */
        String ruleId = "rule-id";
        Region region = new Region();
        region.setSnippet(new ArtifactContent());
        List<SecretValidatorRequest> requests = new ArrayList<>();
        when(webRequestService.validateFinding(region.getSnippet().getText(), ruleId, requests, 0)).thenReturn(new SecretValidationResult());

        /* execute */
        SecretValidationResult validateFindingByRegion = serviceToTest.validateFindingByRegion(region, ruleId, requests, 0);

        /* test */
        verify(webRequestService, never()).validateFinding(region.getSnippet().getText(), ruleId, requests, 0);
        assertEquals(SecretValidationStatus.SARIF_SNIPPET_NOT_SET, validateFindingByRegion.getValidationStatus());

    }

    @Test
    void region_snippet_text_is_blank_returns_expected_validation_result() {
        /* prepare */
        String ruleId = "rule-id";
        Region region = new Region();
        ArtifactContent snippet = new ArtifactContent();
        snippet.setText("   ");
        region.setSnippet(snippet);
        List<SecretValidatorRequest> requests = new ArrayList<>();
        when(webRequestService.validateFinding(region.getSnippet().getText(), ruleId, requests, 0)).thenReturn(new SecretValidationResult());

        /* execute */
        SecretValidationResult validateFindingByRegion = serviceToTest.validateFindingByRegion(region, ruleId, requests, 0);

        /* test */
        verify(webRequestService, never()).validateFinding(region.getSnippet().getText(), ruleId, requests, 0);
        assertEquals(SecretValidationStatus.SARIF_SNIPPET_NOT_SET, validateFindingByRegion.getValidationStatus());

    }

    @Test
    void region_snippet_text_is_set_results_in_web_request_service_called_once() {
        /* prepare */
        String ruleId = "rule-id";
        Region region = new Region();
        ArtifactContent snippet = new ArtifactContent();
        snippet.setText("secret");
        region.setSnippet(snippet);
        List<SecretValidatorRequest> requests = new ArrayList<>();
        when(webRequestService.validateFinding(region.getSnippet().getText(), ruleId, requests, 0)).thenReturn(new SecretValidationResult());

        /* execute */
        SecretValidationResult validateFindingByRegion = serviceToTest.validateFindingByRegion(region, ruleId, requests, 0);

        /* test */
        verify(webRequestService, times(1)).validateFinding(region.getSnippet().getText(), ruleId, requests, 0);
        assertEquals(SecretValidationStatus.NO_VALIDATION_CONFIGURED, validateFindingByRegion.getValidationStatus());

    }

}
