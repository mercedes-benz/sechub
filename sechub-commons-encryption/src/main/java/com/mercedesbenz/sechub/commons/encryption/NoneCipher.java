// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

class NoneCipher implements PersistentCipher {

    private static final InitializationVector INITIAL_VECTOR = new InitializationVector("none".getBytes(EncryptionConstants.UTF8_CHARSET_ENCODING));

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
        /*
         * We always use the "same" initial vector which is always "none". Means it is
         * also clear in database that this is not a real initial vector. The NoneCipher
         * implementation does need an initial vector at all... but having a database
         * column and having this method inside the interface contract, it is necessary
         * to provide a value.
         */
        return INITIAL_VECTOR;
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
