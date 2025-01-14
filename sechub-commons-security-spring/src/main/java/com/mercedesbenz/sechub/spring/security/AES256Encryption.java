// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@Component
@ConditionalOnProperty(name = "sechub.security.encryption.secret-key")
public class AES256Encryption {

    private static final String TRANSFORMATION = "AES";
    private static final CryptoAccess<SecretKey> secretKeyCryptoAccess = new CryptoAccess<>();

    private final Cipher encrypt;
    private final Cipher decrypt;
    private final SealedObject sealedSecretKey;

    AES256Encryption(SecurityProperties securityProperties) throws GeneralSecurityException {
        SecretKey secretKey = new SecretKeySpec(securityProperties.getEncryption().getSecretKey().getBytes(StandardCharsets.UTF_8), TRANSFORMATION);
        this.sealedSecretKey = secretKeyCryptoAccess.seal(secretKey);

        this.encrypt = Cipher.getInstance(TRANSFORMATION);
        try {
            initEncrypt();
        } catch (Exception e) {
            throw new GeneralSecurityException(e);
        }

        this.decrypt = Cipher.getInstance(TRANSFORMATION);
        try {
            initDecrypt();
        } catch (Exception e) {
            throw new GeneralSecurityException(e);
        }
    }

    public byte[] encrypt(String plainText) {
        byte[] encryptedBytes;

        try {
            encryptedBytes = encrypt.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            initEncrypt();
            throw new AES256EncryptionException("Failed to encrypt text", e);
        }

        return encryptedBytes;
    }

    public String decrypt(byte[] encryptedBytes) {
        byte[] decryptedBytes;

        try {
            decryptedBytes = decrypt.doFinal(encryptedBytes);
        } catch (Exception e) {
            initDecrypt();
            throw new AES256EncryptionException("Failed to decrypt text", e);
        }

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private void initEncrypt() {
        try {
            this.encrypt.init(Cipher.ENCRYPT_MODE, secretKeyCryptoAccess.unseal(sealedSecretKey));
        } catch (InvalidKeyException e) {
            throw new AES256EncryptionException("Failed to init encryption cipher", e);
        }
    }

    private void initDecrypt() {
        try {
            this.decrypt.init(Cipher.DECRYPT_MODE, secretKeyCryptoAccess.unseal(sealedSecretKey));
        } catch (InvalidKeyException e) {
            throw new AES256EncryptionException("Failed to init decryption cipher", e);
        }
    }
}
