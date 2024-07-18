// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.util.List;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorRequest;

import de.jcup.sarif_2_1_0.model.Region;

public interface SecretValidationModul {

    SecretValidationResult validateFindingByRegion(Region findingRegion, List<SecretValidatorRequest> requests, boolean trustAllCertificates);

}
