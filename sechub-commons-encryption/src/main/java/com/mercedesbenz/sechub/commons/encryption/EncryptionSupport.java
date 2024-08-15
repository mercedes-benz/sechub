// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

/**
 * A class to simplify encryption (and also decryption)
 *
 * @author Albert Tregnaghi
 *
 */
public class EncryptionSupport {

    private StringByteArrayTransformer stringTransformer = new StringByteArrayTransformer();

    /**
     * Encrypt given string
     *
     * @param string text to encrypt
     * @param cipher cipher to use for encryption
     * @return {@link EncryptionResult}, never <code>null</code>
     */
    public EncryptionResult encryptString(String string, PersistentCipher cipher) {
        return encrypt(string, cipher, stringTransformer);
    }

    /**
     * Encrypt given object. Will create new initial vector automatically.
     *
     * @param <T>         target object type
     * @param object      object to encrypt
     * @param cipher      cipher to use for encryption
     * @param transformer transformer used to transform object into a byte array
     * @return {@link EncryptionResult}, never <code>null</code>
     */
    public <T> EncryptionResult encrypt(T object, PersistentCipher cipher, ByteArrayTransformer<T> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("transformer may not be null!");
        }
        byte[] transformToBytes = transformer.transformToBytes(object);

        InitializationVector initialVector = cipher.createNewInitializationVector();

        byte[] encrypted = cipher.encrypt(transformToBytes, initialVector);

        EncryptionResult result = new EncryptionResult(encrypted, initialVector);
        return result;
    }

    /**
     * Decrypts given encrypted byte array and automatically transform to a string
     * by using a {@link StringByteArrayTransformer}.
     *
     * @param encryptedData
     * @param cipher
     * @param initialVector
     * @return string or null
     */
    public String decryptString(byte[] encryptedData, PersistentCipher cipher, InitializationVector initialVector) {
        return decrypt(encryptedData, cipher, initialVector, stringTransformer);
    }

    /**
     * Decrypts given encrypted byte array via given byte array transformer instance
     *
     * @param <T>           Target object type
     * @param encryptedData byte array containing encrypted dta
     * @param cipher        cipher to use
     * @param initialVector initial vector to use
     * @param transformer   transformer which will be used to transform byte array
     *                      result from cipher instance to target object
     * @return target object or <code>null</code>
     */
    public <T> T decrypt(byte[] encryptedData, PersistentCipher cipher, InitializationVector initialVector, ByteArrayTransformer<T> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("transformer may not be null!");
        }
        byte[] unencrypted = cipher.decrypt(encryptedData, initialVector);

        return transformer.transformFromBytes(unencrypted);
    }

}
