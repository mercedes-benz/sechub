// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

public interface PersistentCipher {

    /**
     * Encrypts plain data with given initial vector
     *
     * @param data          the origin (not encrypted) data
     * @param initialVector an initial vector
     * @return encrypted data as byte array
     */
    public byte[] encrypt(byte[] data, InitializationVector initialVector);

    /**
     * Decrypts given encrypted data with initial vector
     *
     * @param encryptedData the encrypted data
     * @param initialVector an initial vector to use for decryption
     * @return decrypted data as byte array
     */
    public byte[] decrypt(byte[] encryptedData, InitializationVector initialVector);

    /**
     * Creates a new initialization vector which can be used for encryption. Remark:
     * The bytes of the initialization vector must be stored together with the
     * encrypted data. Otherwise it is not possible to retain the origin data even
     * when the secret key is known!
     *
     * @return initialization vector which provides initialization bytes
     */
    public InitializationVector createNewInitializationVector();

    /**
     * @return the type of the cipher
     */
    public PersistentCipherType getType();
}
