// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequest;

import de.jcup.sarif_2_1_0.model.ArtifactContent;
import de.jcup.sarif_2_1_0.model.Region;

@Service
public class SecretValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(SecretValidationService.class);

    @Autowired
    SecretValidatorWebRequestService webRequestService;

    public SecretValidationResult validateFindingByRegion(Region findingRegion, List<SecretValidatorRequest> requests, boolean trustAllCertificates) {
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
        return webRequestService.validateFinding(snippetText, requests, trustAllCertificates);
    }

}
