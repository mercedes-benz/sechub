// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * Interface for cryptographic algorithms used to protect data at rest.
 *
 * "At rest" refers to data which is usually stored in a database, file or other
 * persistent storage.
 *
 * @author Jeremias Eppler
 */
public interface PersistenceCipher {
    public static PersistenceCipher create(BinaryString secret) throws InvalidKeyException {
        return null;
    }

    public BinaryString generateNewInitializationVector();

    public BinaryString generateNewInitializationVector(BinaryStringEncodingType encodingType);

    public BinaryString encrypt(String plaintext, BinaryString initializationVector) throws InvalidAlgorithmParameterException, InvalidKeyException;

    public BinaryString encrypt(String plaintext, BinaryString initializationVector, BinaryStringEncodingType encodingType)
            throws InvalidAlgorithmParameterException, InvalidKeyException;

    public String decrypt(BinaryString ciphertext, BinaryString initializationVector)
            throws IllegalArgumentException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;

    public PersistenceCipherType getCipherType();
}
