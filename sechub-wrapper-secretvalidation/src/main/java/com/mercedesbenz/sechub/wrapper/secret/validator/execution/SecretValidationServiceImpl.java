// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequest;

import de.jcup.sarif_2_1_0.model.ArtifactContent;
import de.jcup.sarif_2_1_0.model.Region;

@Profile("!" + SecretValidatorSpringProfiles.INTEGRATIONTEST)
@Service
public class SecretValidationServiceImpl implements SecretValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(SecretValidationServiceImpl.class);

    @Autowired
    SecretValidatorWebRequestService webRequestService;

    @Override
    public SecretValidationResult validateFindingByRegion(Region findingRegion, String ruleId, List<SecretValidatorRequest> requests, long connectionRetries) {
        ArtifactContent snippet = findingRegion.getSnippet();
        SecretValidationResult validationResult = new SecretValidationResult();
        if (snippet == null) {
            LOG.warn("Cannot validate finding because the SARIF snippet is null.");
            validationResult.setValidationStatus(SecretValidationStatus.SARIF_SNIPPET_NOT_SET);
            return validationResult;
        }

        String snippetText = snippet.getText();
        if (snippetText == null || snippetText.isBlank()) {
            LOG.warn("Cannot validate finding because the SARIF snippet text is null or empty.");
            validationResult.setValidationStatus(SecretValidationStatus.SARIF_SNIPPET_NOT_SET);
            return validationResult;
        }
        return webRequestService.validateFinding(snippetText, ruleId, requests, connectionRetries);
    }

}
