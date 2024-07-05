// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

class NoneCipher implements PersistentCipher {

    @Override
    public byte[] encrypt(byte[] plainData, InitializationVector initVector) {
        return plainData;
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, InitializationVector initVector) {
        return encryptedData;
    }

    @Override
    public InitializationVector createNewInitializationVector() {
        return new InitializationVector();
    }

    @Override
    public PersistentCipherType getType() {
        return PersistentCipherType.NONE;
    }

    @Override
    public String toString() {
        return getType().name();
    }
}
