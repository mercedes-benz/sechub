// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;

@Service
public class ScheduleEncryptionService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleEncryptionService.class);

    @Autowired
    ScheduleCipherPoolDataProvider poolDataProvider;

    @Autowired
    EncryptionSupport encryptionSupport;

    @Autowired
    EncryptionPoolFactory encryptionPoolFactory;

    @Autowired
    LatestCipherPoolIdResolver latestCipherPoolIdResolver;

    Long latestCipherPoolId;

    private EncryptionPool encryptionPool;

    @EventListener(ApplicationStartedEvent.class)
    void applicationStarted() throws EncryptionPoolException {
        List<ScheduleCipherPoolData> allPoolDataEntries = poolDataProvider.ensurePoolDataAvailable();

        encryptionPool = encryptionPoolFactory.createEncryptionPool(allPoolDataEntries);

        latestCipherPoolId = latestCipherPoolIdResolver.resolveLatestPoolId(allPoolDataEntries);
        if (latestCipherPoolId == null) {
            throw new IllegalStateException("Was not able to determine latest cipher pool data!");
        }

        /* sanity check */
        if (encryptionPool.getCipherForPoolId(latestCipherPoolId) == null) {
            throw new IllegalStateException("Encryption pool has no entry for latest cipher pool id: %d".formatted(latestCipherPoolId));
        }

    }

    public ScheduleEncryptionResult encryptWithLatestCipher(String string) {
        return new ScheduleEncryptionResult(latestCipherPoolId, enryptToByteArray(string, latestCipherPoolId));
    }

    public String decryptToString(byte[] encrypted, Long encryptionPoolId, InitializationVector initialVector) {
        PersistentCipher cipher = encryptionPool.getCipherForPoolId(encryptionPoolId);
        if (cipher == null) {
            throw new IllegalStateException("There was no registered cipher entry for pool id: %d".formatted(encryptionPoolId));
        }
        return encryptionSupport.decryptString(encrypted, cipher, initialVector);
    }

    private EncryptionResult enryptToByteArray(String string, Long encryptionPoolId) {
        PersistentCipher cipher = encryptionPool.getCipherForPoolId(encryptionPoolId);
        if (cipher == null) {
            throw new IllegalStateException("There was no registered cipher entry for pool id: %d".formatted(encryptionPoolId));
        }
        return encryptionSupport.encryptString(string, cipher);
    }

}
