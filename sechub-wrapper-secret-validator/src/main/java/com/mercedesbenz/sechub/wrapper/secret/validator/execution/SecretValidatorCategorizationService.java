// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import static com.mercedesbenz.sechub.wrapper.secret.validator.support.SarifImporterKeys.*;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorCategorization;

import de.jcup.sarif_2_1_0.model.PropertyBag;
import de.jcup.sarif_2_1_0.model.Region;

@Service
public class SecretValidatorCategorizationService {

    public void categorizeFindingByRegion(SecretValidationResult validationResult, Region findingRegion, SecretValidatorCategorization categorization) {
        if (categorization == null || categorization.isEmpty()) {
            return;
        }
        SecretValidationStatus validationStatus = validationResult.getValidationStatus();
        PropertyBag properties = new PropertyBag();

        switch (validationStatus) {
        case VALID:
            properties.setAdditionalProperty(SECRETSCAN_SECHUB_SEVERITY.getKey(), categorization.getValidationSuccessSeverity());
            String validatedByUrl = validationResult.getValidatedByUrl();
            if (validatedByUrl != null) {
                properties.setAdditionalProperty(SECRETSCAN_VALIDATED_BY_URL.getKey(), validatedByUrl);
            }
            break;
        case INVALID:
            properties.setAdditionalProperty(SECRETSCAN_SECHUB_SEVERITY.getKey(), categorization.getValidationFailedSeverity());
            break;
        case NO_VALIDATION_CONFIGURED:
        case SARIF_SNIPPET_NOT_SET:
        default:
            properties.setAdditionalProperty(SECRETSCAN_SECHUB_SEVERITY.getKey(), categorization.getDefaultSeverity());
        }
        findingRegion.setProperties(properties);
    }

}
