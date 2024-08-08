// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

@MustBeKeptStable("used in database as values and also inside event communication/domain messages")
public enum SecHubCipherPasswordSourceType {

    /**
     * No password
     */
    NONE,

    /**
     * Password comes from an environment variable, the name of the variable has to
     * be defined inside password source date.
     *
     * <b>Attention:<b> The content of the variable may not be empty and MUST be
     * base64 encoded!
     */
    ENVIRONMENT_VARIABLE
}
