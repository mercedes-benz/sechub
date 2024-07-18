// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

@MustBeKeptStable("used in database as values and also inside event communication/domain messages")
public enum SecHubCipherPasswordSourceType {

    NONE,

    ENVIRONMENT_VARIABLE
}
