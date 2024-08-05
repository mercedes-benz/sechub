// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Providing access to AES-GCM-SIV
 *
 * AES-GCM-SIV is a nonce misuse-resistant authenticated encryption algorithm.
 *
 * For more information refer to
 * <a href="https://datatracker.ietf.org/doc/html/rfc8452">RFC 8452</a>
 *
 * @author Jeremias Eppler
 * @author Albert Tregnaghi
 */
class AesGcmSivCipher implements PersistentCipher {

    private static BouncyCastleProvider cryptoProvider;

    public static final int AUTHENTICATION_TAG_LENGTH_IN_BITS = 16 * 8; // 16 bytes (128 bits)

    /**
     * The recommended initialization vector (iv) for AES-GCM-SIV is 12 bytes or 96
     * bits.
     *
     * For an explanation have a look at: -
     * https://datatracker.ietf.org/doc/html/rfc8452#section-4 -
     * https://crypto.stackexchange.com/questions/41601/aes-gcm-recommended-iv-size-why-12-bytes
     */
    static final int IV_LENGTH_IN_BYTES = 12;

    private static final String ALGORITHM = "AES/GCM-SIV/NoPadding";

    static {
        cryptoProvider = new BouncyCastleProvider();

        Security.addProvider(cryptoProvider);
    }

    private SecretKey secretKey;

    private PersistentCipherType type;

    AesGcmSivCipher(SecretKey secretKey, PersistentCipherType type) {
        this.secretKey = secretKey;
        this.type = type;
    }

    @Override
    public byte[] encrypt(byte[] origin, InitializationVector initVector) {

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, cryptoProvider);

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_IN_BITS, initVector.getInitializationBytes());

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] encrypted = cipher.doFinal(origin);

            return encrypted;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException providerException) {
            throw new IllegalStateException("Encryption not possible, please check the provider", providerException);
        } catch (BadPaddingException | IllegalBlockSizeException paddingBlockException) {
            throw new IllegalStateException("Should not occur. AES in GCM-SIV mode does not require padding.", paddingBlockException);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Key not valid", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Invalid algorithm parameters", e);
        }

    }

    @Override
    public byte[] decrypt(byte[] encryptedData, InitializationVector initVector) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM, cryptoProvider);

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_IN_BITS, initVector.getInitializationBytes());

            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] plaintextBytes = cipher.doFinal(encryptedData);
            return plaintextBytes;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException providerException) {
            throw new IllegalStateException("Decryption not possible, please check the provider", providerException);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Key not valid", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Invalid algorithm parameters", e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public InitializationVector createNewInitializationVector() {

        byte[] initializationVector = new byte[IV_LENGTH_IN_BYTES];

        SecureRandom random = new SecureRandom();
        random.nextBytes(initializationVector);

        return new InitializationVector(initializationVector);
    }

    @Override
    public PersistentCipherType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type == null ? "Unknown " + getClass().getSimpleName() + " type!" : type.name();
    }

}
