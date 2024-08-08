// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.encryption;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.encryption.DefaultSecretKeyProvider;
import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;

import jakarta.annotation.PostConstruct;

@Service
public class PDSEncryptionService {

    @Autowired
    EncryptionSupport encryptionSupport;

    @Autowired
    PersistentCipherFactory cipherFactory;

    @Autowired
    PDSEncryptionConfiguration configuration;

    PersistentCipher cipher;

    @PostConstruct
    void init() {

        PDSCipherAlgorithm algorithm = configuration.getAlgorithm();
        if (algorithm == null) {
            throw new IllegalStateException("No cipher algorithm defined!");
        }
        SecretKeyProvider secretKeyProvider = null;
        switch (algorithm) {
        case NONE:
            break;
        default:
            byte[] base64decoded = Base64.getDecoder().decode(configuration.getSecretKeyBytes());
            secretKeyProvider = new DefaultSecretKeyProvider(base64decoded, algorithm.getType());
            break;

        }
        cipher = cipherFactory.createCipher(secretKeyProvider, algorithm.getType());
    }

    public String decryptString(byte[] encryptedData, InitializationVector initialVector) {
        return encryptionSupport.decryptString(encryptedData, cipher, initialVector);
    }

    public EncryptionResult encryptString(String plainText) {
        return encryptionSupport.encryptString(plainText, cipher);
    }

}
