// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

/**
 * Represents the result of an encryption. Contains encrypted data but also
 * initial vector.
 *
 * @author Albert Tregnaghi
 *
 */
public class EncryptionResult {

    private byte[] encryptedData;

    private InitializationVector initialVector;

    public EncryptionResult(byte[] encryptedData, InitializationVector initialVector) {
        if (initialVector == null) {
            throw new IllegalArgumentException("initial vector may not be null!");
        }
        this.encryptedData = encryptedData;
        this.initialVector = initialVector;
    }

    /**
     * @return encrypted data or <code>null</code>
     */
    public byte[] getEncryptedData() {
        return encryptedData;
    }

    /**
     * @return initial vector, never <code>null</code>
     */
    public InitializationVector getInitialVector() {
        return initialVector;
    }

}