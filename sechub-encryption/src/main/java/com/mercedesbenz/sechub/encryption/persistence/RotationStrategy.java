// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.encryption.persistence;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * The rotation strategy helps to rotate the cipher text.
 *
 * It can be used to rotate (re-encrpyt) by using: - different initialization
 * vectors - different secret keys - different algorithms
 *
 * or a combination of the above.
 *
 * @author Jeremias Eppler
 */
public class RotationStrategy {
    private PersistenceCipher currentCipher;
    private PersistenceCipher newCipher;
    private boolean performSecretRotation = false;

    private RotationStrategy(PersistenceCipher currentCipher, PersistenceCipher newCipher, boolean performSecretRotation) {
        this.currentCipher = currentCipher;
        this.newCipher = newCipher;
        this.performSecretRotation = performSecretRotation;
    }

    /**
     * Create a new rotation strategy which only allows to rotate the initialization
     * vector.
     *
     * @param secret
     * @param cipher
     * @return
     * @throws InvalidKeyException
     */
    public static RotationStrategy createInitializationVectorOnlyRotationStrategy(BinaryString secret, PersistenceCipherType cipher)
            throws InvalidKeyException {
        PersistenceCipher currentCipher = PersistenceCipherFactory.create(cipher, secret);
        PersistenceCipher newCipher = PersistenceCipherFactory.create(cipher, secret);

        boolean performSecretRotation = false;

        return new RotationStrategy(currentCipher, newCipher, performSecretRotation);
    }

    /**
     * Create a new rotation strategy which allows to rotate the secret.
     *
     * This is useful in case of a secret leak.
     *
     * @param currentSecret
     * @param newSecret
     * @param cipher
     * @return
     * @throws InvalidKeyException
     */
    public static RotationStrategy createSecretRotationStrategy(BinaryString currentSecret, BinaryString newSecret, PersistenceCipherType cipher)
            throws InvalidKeyException {
        PersistenceCipher currentCipher = PersistenceCipherFactory.create(cipher, currentSecret);
        PersistenceCipher newCipher = PersistenceCipherFactory.create(cipher, newSecret);

        boolean performSecretRotation = true;

        return new RotationStrategy(currentCipher, newCipher, performSecretRotation);
    }

    /**
     * Create a new rotation strategy which allows to rotate the secret and ciphers.
     *
     * This is useful if the underling cryptographic cipher or mode of operation is
     * deemed as insecure.
     *
     * For example, this is the case with Data Encryption Standard (DES) and the
     * Triple DES variant.
     *
     *
     * @param currentSecret
     * @param newSecret
     * @param currentCipherType
     * @param newCipherType
     * @return
     * @throws InvalidKeyException
     */
    public static RotationStrategy createCipherAndSecretRotationStrategy(BinaryString currentSecret, BinaryString newSecret,
            PersistenceCipherType currentCipherType, PersistenceCipherType newCipherType) throws InvalidKeyException {
        PersistenceCipher currentCipher = PersistenceCipherFactory.create(currentCipherType, currentSecret);
        PersistenceCipher newCipher = PersistenceCipherFactory.create(newCipherType, newSecret);

        boolean performSecretRotation = true;

        return new RotationStrategy(currentCipher, newCipher, performSecretRotation);
    }

    /**
     * Rotate the encrypted cipher text using the same initialization vector.
     *
     * The given cipher text is decrypted and encrypted again using the given
     * initialization vector the old and the new cipher text.
     *
     * @param cipherText
     * @param initializationVector
     * @return
     * @throws InvalidKeyException
     * @throws IllegalArgumentException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public BinaryString rotate(BinaryString cipherText, BinaryString initializationVector)
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        return rotate(cipherText, initializationVector, null, cipherText.getType());
    }

    /**
     * Rotate the encrypted cipher text using different initialization vectors.
     *
     * The given cipher text is decrypted and encrypted again using two different
     * initialization vectors.
     *
     * @param cipherText
     * @param initializationVector
     * @param newIntializationVector
     * @return
     * @throws InvalidKeyException
     * @throws IllegalArgumentException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public BinaryString rotate(BinaryString cipherText, BinaryString initializationVector, BinaryString newIntializationVector)
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        return rotate(cipherText, initializationVector, newIntializationVector, cipherText.getType());
    }

    /**
     * Rotate the encrypted cipher text using different initialization vectors and
     * the given encoding type.
     *
     * The given cipher text is decrypted and encrypted again using two different
     * initialization vectors.
     *
     * The resulting cipher text is returned in the specified
     * {@link BinaryStringEncodingType}.
     *
     * @param cipherText
     * @param initializationVector
     * @param newIntializationVector
     * @param newBinaryStringEncoding
     * @return
     * @throws InvalidKeyException
     * @throws IllegalArgumentException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public BinaryString rotate(BinaryString cipherText, BinaryString initializationVector, BinaryString newIntializationVector,
            BinaryStringEncodingType newBinaryStringEncoding)
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        if (cipherText == null) {
            throw new IllegalArgumentException("The ciphertext cannot be null!");
        }

        if (initializationVector == null) {
            throw new IllegalArgumentException("The initialization vector (nonce) cannot be null!");
        }

        String plainText = currentCipher.decrypt(cipherText, initializationVector);

        BinaryString newCipherText = null;

        if (newIntializationVector != null) {
            newCipherText = newCipher.encrypt(plainText, newIntializationVector);
        } else {
            newCipherText = newCipher.encrypt(plainText, initializationVector);
        }

        return newCipherText;
    }

    public PersistenceCipherType getCurrentCipher() {
        return currentCipher.getCipherType();
    }

    public PersistenceCipherType getNewCipher() {
        return newCipher.getCipherType();
    }

    public boolean isSecretRotationStrategy() {
        return performSecretRotation;
    }

    public boolean isCipherRotationStrategy() {
        return currentCipher.getCipherType() != newCipher.getCipherType();
    }
}
