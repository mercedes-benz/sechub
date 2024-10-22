// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.encryption;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(AES256EncryptionProperties.class)
public class AES256Encryption {

    private static final Logger LOG = LoggerFactory.getLogger(AES256Encryption.class);
    private static final String TRANSFORMATION = "AES";
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private final Cipher encrypt;
    private final Cipher decrypt;

    AES256Encryption(AES256EncryptionProperties properties) throws GeneralSecurityException {
        SecretKey secretKey = new SecretKeySpec(properties.getSecretKeyBytes(), TRANSFORMATION);
        this.encrypt = Cipher.getInstance(TRANSFORMATION);
        encrypt.init(Cipher.ENCRYPT_MODE, secretKey);
        this.decrypt = Cipher.getInstance(TRANSFORMATION);
        decrypt.init(Cipher.DECRYPT_MODE, secretKey);
    }

    public String encrypt(String plainText) {
        byte[] encryptedBytes;

        try {
            encryptedBytes = encrypt.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            String errMsg = "Failed to encrypt text: %s".formatted(e.getMessage());
            LOG.debug(errMsg);
            throw new RuntimeException(errMsg, e);
        }

        return ENCODER.encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedText) {
        byte[] decryptedBytes;

        try {
            decryptedBytes = decrypt.doFinal(DECODER.decode(encryptedText));
        } catch (Exception e) {
            String errMsg = "Failed to decrypt text: %s".formatted(e.getMessage());
            LOG.debug(errMsg);
            throw new RuntimeException(errMsg, e);
        }

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
