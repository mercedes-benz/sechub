// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * A cipher which does not encrypt anything.
 *
 * It can be used during testing or debugging.
 *
 * Furthermore, this cipher can be used to change the encoding of the data. For
 * example, from plain to Base64.
 *
 * @author Jeremias Eppler
 */
public class NoneCipher implements PersistenceCipher {
    private NoneCipher() {
    }

    @Override
    public PersistenceCipherType getCipherType() {
        return PersistenceCipherType.NONE;
    }

    public static PersistenceCipher create(BinaryString secret) throws InvalidKeyException {
        return new NoneCipher();
    }

    @Override
    public BinaryString encrypt(String plaintext, BinaryString initializationVector) throws InvalidAlgorithmParameterException, InvalidKeyException {
        return encrypt(plaintext, initializationVector, BinaryStringEncodingType.PLAIN);
    }

    @Override
    public BinaryString encrypt(String plaintext, BinaryString initializationVector, BinaryStringEncodingType encodingType)
            throws InvalidAlgorithmParameterException, InvalidKeyException {
        return BinaryStringFactory.createFromString(plaintext, encodingType);
    }

    @Override
    public String decrypt(BinaryString ciphertext, BinaryString initializationVector)
            throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        return ciphertext.toString();
    }

    @Override
    public BinaryString generateNewInitializationVector() {
        return generateNewInitializationVector(BinaryStringEncodingType.PLAIN);
    }

    @Override
    public BinaryString generateNewInitializationVector(BinaryStringEncodingType encodingType) {
        return BinaryStringFactory.createFromString("", encodingType);
    }
}
