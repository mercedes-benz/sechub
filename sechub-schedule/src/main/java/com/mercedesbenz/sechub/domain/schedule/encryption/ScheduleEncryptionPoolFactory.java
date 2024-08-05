// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubSecretKeyProviderFactory;

@Component
public class ScheduleEncryptionPoolFactory {

    @Autowired
    EncryptionSupport encryptionSupport;

    @Autowired
    PersistentCipherFactory cipherFactory;

    @Autowired
    SecHubSecretKeyProviderFactory secHubSecretKeyProviderFactory;

    /**
     * Creates an encryption pool for the given list of scheduler cipher pool data.
     * The factory will create the necessary persistent cipher instances and
     * automatically check that the encryption is possible with the current setup.
     *
     * If it is not possible - e.g. a cipher is created but with the wrong password
     * - the factory will throw an ScheduleEncryptionException
     *
     * @param allPoolDataEntries - may not be <code>null</code>
     * @return
     */
    public ScheduleEncryptionPool createEncryptionPool(List<ScheduleCipherPoolData> allPoolDataEntries) throws ScheduleEncryptionException {
        if (allPoolDataEntries == null) {
            throw new IllegalArgumentException("allPoolDataEntries may never be null!");
        }

        Map<Long, PersistentCipher> map = new LinkedHashMap<>();

        for (ScheduleCipherPoolData poolData : allPoolDataEntries) {

            PersistentCipher cipher = createCipher(poolData.getAlgorithm().getType(), poolData.getPasswordSourceType(), poolData.getPasswordSourceData());

            byte[] testEncrypted = poolData.getTestEncrypted();
            InitializationVector initialVector = new InitializationVector(poolData.getTestInitialVector());

            String decrypted = encryptionSupport.decryptString(testEncrypted, cipher, initialVector);

            String expected = poolData.getTestText();

            if (!expected.equals(decrypted)) {
                /*
                 * We block server start here because a new server must always be able to handle
                 * the complete encryption pool.
                 */
                throw new ScheduleEncryptionException(
                        "The cipher pool entry with id: %d cannot be handled by the server, because origin test text: '%s' was not retrieved from encrypted test text data, but instead: '%s'"
                                .formatted(poolData.getId(), expected, decrypted));
            }

            /* Server is able to encrypt/decrypt data with given secret - register it */
            map.put(poolData.getId(), cipher);

        }
        return new ScheduleEncryptionPool(map);
    }

    private PersistentCipher createCipher(PersistentCipherType cipherType, SecHubCipherPasswordSourceType passwordSourceType, String passwordSourceData) {

        SecretKeyProvider secretKeyProvider = secHubSecretKeyProviderFactory.createSecretKeyProvider(cipherType, passwordSourceType, passwordSourceData);

        return cipherFactory.createCipher(secretKeyProvider, cipherType);

    }
}
