// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution.test;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.secret.validator.execution.SecretValidationModul;
import com.mercedesbenz.sechub.wrapper.secret.validator.execution.SecretValidationResult;
import com.mercedesbenz.sechub.wrapper.secret.validator.execution.SecretValidationStatus;
import com.mercedesbenz.sechub.wrapper.secret.validator.execution.SecretValidatorSpringProfiles;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequest;

import de.jcup.sarif_2_1_0.model.Region;

/**
 * This service is only available and used if the application is started with
 * the {@link SecretValidatorSpringProfiles.INTEGRATIONTEST} profile. It returns
 * findings as invalid if the list of requests passed to the
 * <code>validateFindingByRegion(...)</code> method is <code>null</code> or
 * empty. If the requests list contains at least one entry the finding is
 * returned as valid.
 *
 */
@Profile(SecretValidatorSpringProfiles.INTEGRATIONTEST)
@Service
public class IntegrationTestSecretValidationService implements SecretValidationModul {

    @Override
    public SecretValidationResult validateFindingByRegion(Region findingRegion, List<SecretValidatorRequest> requests, boolean trustAllCertificates) {
        if (requests == null || requests.isEmpty()) {
            SecretValidationResult validationResult = new SecretValidationResult();
            validationResult.setValidationStatus(SecretValidationStatus.INVALID);
            return validationResult;
        }

        SecretValidationResult validationResult = new SecretValidationResult();
        validationResult.setValidationStatus(SecretValidationStatus.VALID);
        return validationResult;
    }

}
