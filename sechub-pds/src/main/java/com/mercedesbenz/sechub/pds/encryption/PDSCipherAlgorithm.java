// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.encryption;

import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;

public enum PDSCipherAlgorithm {

    NONE(PersistentCipherType.NONE),

    AES_GCM_SIV_128(PersistentCipherType.AES_GCM_SIV_128),

    AES_GCM_SIV_256(PersistentCipherType.AES_GCM_SIV_256),;

    private PersistentCipherType type;

    private PDSCipherAlgorithm(PersistentCipherType type) {
        this.type = type;
    }

    public PersistentCipherType getType() {
        return type;
    }

}
