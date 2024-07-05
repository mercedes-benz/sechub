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
     * @param string        text to encrypt
     * @param cipher        cipher to use for encryption
     * @param initialVector initial vector for cipher
     * @return encrypted bytes
     */
    public byte[] encryptString(String string, PersistentCipher cipher, InitializationVector initialVector) {
        return encrypt(string, cipher, initialVector, stringTransformer);
    }

    /**
     * Encrypt given object
     *
     * @param <T>           target object type
     * @param object        object to encrypt
     * @param cipher        cipher to use for encryption
     * @param initialVector initial vector for cipher
     * @param transformer   transformer used to transform object into a byte array
     * @return encrypted bytes
     */
    public <T> byte[] encrypt(T object, PersistentCipher cipher, InitializationVector initialVector, ByteArrayTransformer<T> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("transformer may not be null!");
        }
        byte[] transformToBytes = transformer.transformToBytes(object);

        return cipher.encrypt(transformToBytes, initialVector);
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
