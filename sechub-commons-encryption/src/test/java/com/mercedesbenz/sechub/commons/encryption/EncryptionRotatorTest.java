// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncryptionRotatorTest {

    private EncryptionRotator rotatorToTest;
    private EncryptionRotationSetup rotationSetup;
    private PersistentCipher oldCipher;
    private PersistentCipher newCipher;
    private InitializationVector oldInitialVector;
    private InitializationVector newInitialVector;
    private byte[] plainTextAsBytes;
    private byte[] newEncryptedData;
    private byte[] oldEncryptedData;

    @BeforeEach
    void beforeEach() {
        plainTextAsBytes = "testdata".getBytes();
        oldEncryptedData = "old-encrypted".getBytes();
        newEncryptedData = "new-encrypted".getBytes();

        rotatorToTest = new EncryptionRotator();
        rotationSetup = mock(EncryptionRotationSetup.class);

        oldCipher = mock(PersistentCipher.class);
        newCipher = mock(PersistentCipher.class);

        oldInitialVector = mock(InitializationVector.class);
        newInitialVector = mock(InitializationVector.class);

        when(rotationSetup.getOldCipher()).thenReturn(oldCipher);
        when(rotationSetup.getNewCipher()).thenReturn(newCipher);
        when(rotationSetup.getOldInitialVector()).thenReturn(oldInitialVector);
        when(rotationSetup.getNewInitialVector()).thenReturn(newInitialVector);

        when(oldCipher.decrypt(oldEncryptedData, oldInitialVector)).thenReturn(plainTextAsBytes);
        when(newCipher.encrypt(plainTextAsBytes, newInitialVector)).thenReturn(newEncryptedData);
    }

    @Test
    void roation_uses_old_cipher_and_initvector_to_decrypt() {

        /* execute */
        rotatorToTest.rotate(oldEncryptedData, rotationSetup);

        /* test */
        verify(oldCipher).decrypt(oldEncryptedData, oldInitialVector);

    }

    @Test
    void roation_uses_new_cipher_and_new_initvector_to_encrypt() {

        /* execute */
        rotatorToTest.rotate(oldEncryptedData, rotationSetup);

        /* test */
        verify(newCipher).encrypt(plainTextAsBytes, newInitialVector);

    }

    @Test
    void roation_result_is_new_encrypted_data() {

        /* execute */
        byte[] result = rotatorToTest.rotate(oldEncryptedData, rotationSetup);

        /* test */
        assertThat(result).isEqualTo(newEncryptedData);

    }

}
