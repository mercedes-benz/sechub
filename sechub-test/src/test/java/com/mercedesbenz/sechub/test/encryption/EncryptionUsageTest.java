// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test.encryption;

import static org.assertj.core.api.Assertions.*;

import javax.crypto.AEADBadTagException;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.encryption.DefaultSecretKeyProvider;
import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.EncryptionRotationSetup;
import com.mercedesbenz.sechub.commons.encryption.EncryptionRotator;
import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;

public class EncryptionUsageTest {

    @Test
    void cipher_direct_usage() {
        /* prepare */
        PersistentCipherType cipherType = PersistentCipherType.AES_GCM_SIV_256;
        byte[] rawPlainTextInBytes = "hallo welt".getBytes();
        String testSecret256bit = "x".repeat(32);

        /* execute - usage .. */
        PersistentCipherFactory factory = new PersistentCipherFactory();

        // encrypt and simulate storage
        PersistentCipher cipher = factory.createCipher(new DefaultSecretKeyProvider(testSecret256bit), cipherType);

        InitializationVector initVector = cipher.createNewInitializationVector();
        byte[] encrypted = cipher.encrypt(rawPlainTextInBytes, initVector);

        // simulate storage
        byte[] storedEncryptedDataBytes = encrypted;
        byte[] storedInitVectorBytes = initVector.getInitializationBytes();

        // decrypt
        PersistentCipher anotherCipher = factory.createCipher(new DefaultSecretKeyProvider(testSecret256bit), cipherType);
        byte[] decrypted = anotherCipher.decrypt(storedEncryptedDataBytes, new InitializationVector(storedInitVectorBytes));

        /* test */
        assertThat(decrypted).isEqualTo(rawPlainTextInBytes);

    }

    @Test
    void encryption_support_usage_with_string() { /* prepare */
        PersistentCipherType cipherType = PersistentCipherType.AES_GCM_SIV_256;
        String plainText = "hallo welt";
        String testSecret256bit = "x".repeat(32);

        /* execute - usage .. */
        PersistentCipherFactory factory = new PersistentCipherFactory();

        // encrypt and simulate storage
        PersistentCipher cipher = factory.createCipher(new DefaultSecretKeyProvider(testSecret256bit), cipherType);

        EncryptionSupport support = new EncryptionSupport();

        EncryptionResult encryptionResult = support.encryptString(plainText, cipher);

        // simulate storage
        byte[] storedEncryptedDataBytes = encryptionResult.getEncryptedData();
        byte[] storedInitVectorBytes = encryptionResult.getInitialVector().getInitializationBytes();

        // decrypt
        PersistentCipher anotherCipher = factory.createCipher(new DefaultSecretKeyProvider(testSecret256bit), cipherType);
        String decrypted = support.decryptString(storedEncryptedDataBytes, anotherCipher, new InitializationVector(storedInitVectorBytes));

        /* test */
        assertThat(decrypted).isEqualTo(plainText);

    }

    @Test
    void rotation_with_changed_password_same_init_vector() {
        /* prepare */
        String testData = "hallo welt";
        String oldPassword = "x".repeat(32);
        String newPassword = "y".repeat(32);

        PersistentCipherFactory factory = new PersistentCipherFactory();
        PersistentCipher cipherOldPassword = factory.createCipher(new DefaultSecretKeyProvider(oldPassword), PersistentCipherType.AES_GCM_SIV_256);

        EncryptionSupport encryptSupport = new EncryptionSupport();

        // encrypt with old cipher
        EncryptionResult encryptionResult = encryptSupport.encryptString(testData, cipherOldPassword);

        PersistentCipher cipherNewPassword = factory.createCipher(new DefaultSecretKeyProvider(newPassword), PersistentCipherType.AES_GCM_SIV_256);

        EncryptionRotator rotatorToTest = new EncryptionRotator();

        /* test */

        /* @formatter:off */
        EncryptionRotationSetup rotateSetup = EncryptionRotationSetup.builder().
                newCipher(cipherNewPassword).
                oldCipher(cipherOldPassword).
                oldInitialVector(encryptionResult.getInitialVector()).
        build();

        byte[] newEncrypted = rotatorToTest.rotate(encryptionResult.getEncryptedData(), rotateSetup);

        /* test */

        /* @formatter:on */
        String unencryptedTestData = encryptSupport.decryptString(newEncrypted, cipherNewPassword, rotateSetup.getNewInitialVector());

        assertThat(testData).isEqualTo(unencryptedTestData);

    }

    @Test
    void rotation_with_changed_password_different_init_vector() {
        /* prepare */
        String testData = "hallo welt";
        String oldPassword = "x".repeat(32);
        String newPassword = "y".repeat(32);

        PersistentCipherFactory factory = new PersistentCipherFactory();
        PersistentCipher cipherOldPassword = factory.createCipher(new DefaultSecretKeyProvider(oldPassword), PersistentCipherType.AES_GCM_SIV_256);

        EncryptionSupport encryptSupport = new EncryptionSupport();

        // encrypt with old cipher
        EncryptionResult firstEncryptionResult = encryptSupport.encryptString(testData, cipherOldPassword);

        PersistentCipher cipherNewPassword = factory.createCipher(new DefaultSecretKeyProvider(newPassword), PersistentCipherType.AES_GCM_SIV_256);
        InitializationVector newInitVector = cipherNewPassword.createNewInitializationVector();

        EncryptionRotator rotatorToTest = new EncryptionRotator();

        /* test */

        /* @formatter:off */
        EncryptionRotationSetup rotateSetup = EncryptionRotationSetup.builder().
                newCipher(cipherNewPassword).
                oldCipher(cipherOldPassword).
                oldInitialVector(firstEncryptionResult.getInitialVector()).
                newInitialVector(newInitVector).
                build();

        byte[] newEncrypted = rotatorToTest.rotate(firstEncryptionResult.getEncryptedData(), rotateSetup);

        /* test */

        /* @formatter:on */
        String unencryptedTestData = encryptSupport.decryptString(newEncrypted, cipherNewPassword, newInitVector);

        assertThat(testData).isEqualTo(unencryptedTestData);

        // additional test - we check that the decryption with old init vector does not
        // work
        assertThatThrownBy(() -> encryptSupport.decryptString(newEncrypted, cipherNewPassword, firstEncryptionResult.getInitialVector()))
                .isInstanceOf(IllegalStateException.class).hasRootCauseInstanceOf(AEADBadTagException.class);
    }
}
