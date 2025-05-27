// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import static java.util.Objects.requireNonNull;

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

/**
 * This encryption does NOT provide key rotation! You should use this class only
 * for "throw away encryption".
 *
 * If you want key rotation and/or ensured cluster wide accessibility - e.g. for
 * persisted encrypted data - you have to use EncryptionSupport instead from
 * sechub-commons-encryption!
 *
 */
@Component
@ConditionalOnProperty(name = "sechub.security.encryption.secret-key")
public class AES256Encryption {

    private static final String TRANSFORMATION = "AES";
    private static final CryptoAccess<SecretKey> secretKeyCryptoAccess = new CryptoAccess<>();

    private final Cipher encrypt;
    private final Cipher decrypt;
    private final SealedObject sealedSecretKey;

    AES256Encryption(SecHubSecurityProperties secHubSecurityProperties) throws GeneralSecurityException {
        requireNonNull(secHubSecurityProperties, "SecHubSecurityProperties must not be null");
        SecretKey secretKey = getSecretKey(secHubSecurityProperties);
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

    private static SecretKey getSecretKey(SecHubSecurityProperties secHubSecurityProperties) {
        SecHubSecurityProperties.EncryptionProperties encryption = requireNonNull(secHubSecurityProperties.getEncryptionProperties(),
                "Property %s must not be null".formatted(SecHubSecurityProperties.EncryptionProperties.PREFIX));
        String secretKeyString = requireNonNull(encryption.getSecretKey(),
                "Property %s.%s must not be null".formatted(SecHubSecurityProperties.EncryptionProperties.PREFIX, "secret-key"));
        return new SecretKeySpec(secretKeyString.getBytes(StandardCharsets.UTF_8), TRANSFORMATION);
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
