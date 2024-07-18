// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;

@MustBeKeptStable("used in database as values and also inside event communication/domain messages")
public enum SecHubCipherAlgorithm {

    NONE(PersistentCipherType.NONE),

    AES_GCM_SIV_128(PersistentCipherType.AES_GCM_SIV_128),

    AES_GCM_SIV_256(PersistentCipherType.AES_GCM_SIV_256),;

    private PersistentCipherType type;

    private SecHubCipherAlgorithm(PersistentCipherType type) {
        this.type = type;
    }

    public PersistentCipherType getType() {
        return type;
    }

}
