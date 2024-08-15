// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

public enum PersistentCipherType {

    NONE(null),

    AES_GCM_SIV_128("AES"),

    AES_GCM_SIV_256("AES"),

    ;

    private String secretKeyAlgorithm;

    /**
     * Creates a new cipher type
     *
     * @param secretKeyAlgorithm the algorithm which shall be used for secret key
     *                           creation
     */
    private PersistentCipherType(String secretKeyAlgorithm) {
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

    String getSecretKeyAlgorithm() {
        return secretKeyAlgorithm;
    }

}
