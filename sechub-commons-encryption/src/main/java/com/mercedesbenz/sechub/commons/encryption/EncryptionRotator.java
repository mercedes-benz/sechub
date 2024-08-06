// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

public class EncryptionRotator {

    /**
     * Rotates encryption for existing encrypted data.
     *
     * @param encryptedData data which is encrypted and shall be rotated
     * @param setup         rotation setup to use, may not be <code>null</code>
     * @return new encrypted data
     */
    public byte[] rotate(byte[] encryptedData, EncryptionRotationSetup setup) {
        if (setup == null) {
            throw new IllegalArgumentException("NO rotation setup defined");
        }
        // no further checks necessary - is don by rotation setup builder which is the
        // only way to create such an object

        byte[] unencryptedData = setup.getOldCipher().decrypt(encryptedData, setup.getOldInitialVector());
        byte[] newEncryptedData = setup.getNewCipher().encrypt(unencryptedData, setup.getNewInitialVector());

        return newEncryptedData;
    }

}
