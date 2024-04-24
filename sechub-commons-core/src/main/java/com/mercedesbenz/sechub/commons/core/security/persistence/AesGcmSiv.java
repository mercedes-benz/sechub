package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Providing access to AES-GCM-SIV
 * 
 * AES-GCM-SIV is a nonce misuse-resistant authenticated encryption algorithm.
 * 
 * For more information refer to <a href="https://datatracker.ietf.org/doc/html/rfc8452">RFC 8452</a>
 * 
 * @author Jeremias Eppler
 */
public class AesGcmSiv implements PersistenceCipher {
    
    //TODO: Encrypt secret?
    private SecretKey secret;
    private Provider cryptoProvider;
    private static PersistenceCipherType cipherType;

    private static final String ALGORITHM = "AES/GCM-SIV/NoPadding";

    // The recommended initialization vector (iv) for AES-GCM-SIV is 12 bytes or 96
    // bits.
    // For an explanation have a look at:
    // - https://datatracker.ietf.org/doc/html/rfc8452#section-4
    // - https://crypto.stackexchange.com/questions/41601/aes-gcm-recommended-iv-size-why-12-bytes
    public static final int IV_LENGTH_IN_BYTES = 12;

    public static final int AUTHENTICATION_TAG_LENGTH_IN_BITS = 16 * 8; // 16 bytes (128 bits)

    private AesGcmSiv(SecretKey secret) {
        this.secret = secret;
        cryptoProvider = new BouncyCastleProvider();
        Security.addProvider(cryptoProvider);
    }

    public static AesGcmSiv create(BinaryString binaryString) throws InvalidKeyException {
        AesGcmSiv instance = null;

        byte[] rawSecret = binaryString.getBytes();

        if (rawSecret.length == 32 || rawSecret.length == 16 || rawSecret.length == 24) {
            SecretKey secret = new SecretKeySpec(rawSecret, 0, rawSecret.length, "AES");
            instance = new AesGcmSiv(secret);
           
            if (rawSecret.length == 32 || rawSecret.length == 16) {
                cipherType = (rawSecret.length == 32) ? PersistenceCipherType.AES_GCM_SIV_256 : PersistenceCipherType.AES_GCM_SIV_128;
            } else if (rawSecret.length == 24) {
                cipherType = PersistenceCipherType.AES_GCM_SIV_192;
            }
        } else {
            throw new InvalidKeyException("The secret has to be 128, 192 or 256 bits long.");
        }

        return instance;
    }

    public static BinaryString generateNewInitializationVector() {
        return generateNewInitializationVector(BinaryStringEncodingType.BASE64);
    }
    
    public static BinaryString generateNewInitializationVector(BinaryStringEncodingType encodingType) {
        byte[] initializationVector = new byte[IV_LENGTH_IN_BYTES];

        SecureRandom random = new SecureRandom();
        random.nextBytes(initializationVector);

        return BinaryStringFactory.createFromBytes(initializationVector, encodingType);
    }

    public BinaryString encrypt(String plaintext, BinaryString initializationVector) throws InvalidAlgorithmParameterException, InvalidKeyException {
            return encrypt(plaintext, initializationVector, BinaryStringEncodingType.BASE64);
    }

    public String decrypt(BinaryString ciphertext, BinaryString initializationVector)
            throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM, cryptoProvider);

            SecretKeySpec keySpec = new SecretKeySpec(secret.getEncoded(), "AES");

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_IN_BITS, initializationVector.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] ciphertextBytes = ciphertext.getBytes();

            byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);

            String plaintext = new String(plaintextBytes);

            return plaintext;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException providerException) {
            throw new IllegalStateException("Decryption not possible, please check the provider", providerException);
        }
    }

    @Override
    public PersistenceCipherType getCipherType() {
        return cipherType;
    }

    @Override
    public BinaryString encrypt(String plaintext, BinaryString initializationVector, BinaryStringEncodingType encodingType)
            throws InvalidAlgorithmParameterException, InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, cryptoProvider);

            SecretKeySpec keySpec = new SecretKeySpec(secret.getEncoded(), "AES");

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_IN_BITS, initializationVector.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            return BinaryStringFactory.createFromBytes(ciphertext, encodingType);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException providerException) {
            throw new IllegalStateException("Encryption not possible, please check the provider", providerException);
        } catch (BadPaddingException | IllegalBlockSizeException paddingBlockException) {
            throw new IllegalStateException("Should not occure. AES in GCM-SIV mode does not require padding.", paddingBlockException);
        }
    }
}
