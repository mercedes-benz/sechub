// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.security;

import java.io.Serializable;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

/**
 * Represents a common possibility to encrypt data. The secret key of an
 * instance can unseal and seal objects for example. Be aware: Every crypto
 * access object has its own secret key inside! So you need to use the same
 * crypto access object for you operations...
 *
 * @author Albert Tregnaghi
 *
 * @param <T>
 */
public class CryptoAccess<T extends Serializable> {
    private static KeyGenerator keyGen;
    private char[] transformation = new char[] { 'A', 'E', 'S' };
    private SecretKey secretKey;

    /**
     * Shared crypto access which can encrypt/decrypt strings
     */
    public static final CryptoAccess<String> CRYPTO_STRING = new CryptoAccess<>();

    /**
     * Shared crypto access which can encrypt/decrypt char arrays
     */
    public static final CryptoAccess<char[]> CRYPTO_CHAR_ARRAY = new CryptoAccess<>();

    public CryptoAccess() {
        secretKey = getkeyGen(transformation).generateKey();
    }

    private static KeyGenerator getkeyGen(char[] transformation) {
        if (CryptoAccess.keyGen != null) {
            return CryptoAccess.keyGen;
        }
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(new String(transformation));
            keyGen.init(128);
            CryptoAccess.keyGen = keyGen;
            return CryptoAccess.keyGen;

        } catch (Exception e) {
            throw new IllegalStateException("FATAL:cannot create key generator", e);
        }
    }

    public SealedObject seal(T object) {
        try {
            Cipher cipher = Cipher.getInstance(new String(transformation));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new SecureRandom());
            return new SealedObject(object, cipher);
        } catch (Exception e) {
            throw new IllegalStateException("cannot create sealed object for given objects", e);
        }
    }

    @SuppressWarnings("unchecked")
    public T unseal(SealedObject object) {
        try {
            if (object == null) {
                return null;
            }
            Cipher cipher = Cipher.getInstance(new String(transformation));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new SecureRandom());
            return (T) object.getObject(cipher);
        } catch (Exception e) {
            throw new IllegalStateException("cannot create sealed object for given objects", e);
        }
    }
}
