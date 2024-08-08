// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NoneCipherTest {

    @Test
    void none_cipher_encryption_and_decryption_works_but_encrypted_data_is_origin() {

        /* prepare */
        NoneCipher cipherToTest = new NoneCipher();

        InitializationVector initVector = cipherToTest.createNewInitializationVector();
        byte[] initVectorInBytes = initVector.getInitializationBytes();
        byte[] dataToEncrypt = "i am the plain text :-)".getBytes();

        /* execute */
        byte[] encryptedBytes = cipherToTest.encrypt(dataToEncrypt, initVector);

        /* test */
        NoneCipher cipherFromOutside = new NoneCipher();
        byte[] decrypted = cipherFromOutside.decrypt(encryptedBytes, new InitializationVector(initVectorInBytes));

        assertEquals(new String(dataToEncrypt), new String(decrypted));
        assertEquals(new String(encryptedBytes), new String(dataToEncrypt));

    }

}
