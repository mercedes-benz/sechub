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
import com.mercedesbenz.sechub.commons.encryption.EncryptionRotationSetup;
import com.mercedesbenz.sechub.commons.encryption.EncryptionRotator;
import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseScheduleEncryptionPoolRefresh;

/**
 * This service is the central point for all schedule encryption actions.
 *
 * @author Albert Tregnaghi
 *
 */
@Service
public class ScheduleEncryptionService {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleEncryptionService.class);

    @Autowired
    ScheduleCipherPoolDataProvider poolDataProvider;

    @Autowired
    EncryptionSupport encryptionSupport;

    @Autowired
    ScheduleEncryptionPoolFactory scheduleEncryptionPoolFactory;

    @Autowired
    ScheduleLatestCipherPoolIdResolver scheduleLatestCipherPoolIdResolver;

    @Autowired
    EncryptionRotator rotator;

    Long latestCipherPoolId;

    ScheduleEncryptionPool scheduleEncryptionPool;

    @EventListener(ApplicationStartedEvent.class)
    void applicationStarted() throws ScheduleEncryptionException {
        // If a new started server is not able to handle all ciphers from cipher pool it
        // will just
        // not start (ensures old and new jobs can always be handled)
        setupEncryptionPoolOrFail();

    }

    /**
     * @return latest cipher pool id supported by this service
     */
    public Long getLatestCipherPoolId() {
        return latestCipherPoolId;
    }

    /**
     * Tries to refresh encryption pool and latest id. Same like application startup
     * - but if it is not possible to create a new encryption pool (e.g. new
     * encryption is currently not supported by this server instance) just a warning
     * is logged and the old encryption pool is still in use.
     */
    @UseCaseScheduleEncryptionPoolRefresh(@Step(number = 1, name = "Refresh encryption pool", description = "Encryption pool is refreshed (if necessary)"))
    public void refreshEncryptionPoolAndLatestPoolIdIfNecessary() {

        if (isStillSameEncryptionPool()) {
            LOG.trace("Encryption pool has not changed. No update necessary");
            return;
        }

        Long oldLatestCipherPoolId = latestCipherPoolId;

        LOG.debug("Encryption pool has changed, start encryption pool recreation.");

        try {
            setupEncryptionPoolOrFail();
            LOG.info("Encryption pool has been recreated sucessfully.");

        } catch (ScheduleEncryptionException e) {
            LOG.warn("Was not able to refresh encryption pool, will stay on old encryption. Reason: {}", e.getMessage());
        }

        LOG.info("Changed latest cipher pool from: {} to: {}", oldLatestCipherPoolId, latestCipherPoolId);
    }

    private boolean isStillSameEncryptionPool() {
        return poolDataProvider.isContainingExactlyGivenPoolIds(scheduleEncryptionPool.getAllPoolIds());
    }

    private void setupEncryptionPoolOrFail() throws ScheduleEncryptionException {
        List<ScheduleCipherPoolData> allPoolDataEntries = poolDataProvider.ensurePoolDataAvailable();

        scheduleEncryptionPool = scheduleEncryptionPoolFactory.createEncryptionPool(allPoolDataEntries);

        latestCipherPoolId = scheduleLatestCipherPoolIdResolver.resolveLatestPoolId(allPoolDataEntries);
        if (latestCipherPoolId == null) {
            throw new IllegalStateException("Was not able to determine latest cipher pool data!");
        }

        /* sanity check */
        if (scheduleEncryptionPool.getCipherForPoolId(latestCipherPoolId) == null) {
            throw new IllegalStateException("Encryption pool has no entry for latest cipher pool id: %d".formatted(latestCipherPoolId));
        }

        LOG.info("Encryption pool created ({} pool entries)");
    }

    public ScheduleEncryptionResult encryptWithLatestCipher(String string) {
        return new ScheduleEncryptionResult(latestCipherPoolId, enryptToByteArray(string, latestCipherPoolId));
    }

    public String decryptToString(byte[] encrypted, Long encryptionPoolId, InitializationVector initialVector) {
        PersistentCipher cipher = scheduleEncryptionPool.getCipherForPoolId(encryptionPoolId);
        if (cipher == null) {
            throw new IllegalStateException("There was no registered cipher entry for pool id: %d".formatted(encryptionPoolId));
        }
        return encryptionSupport.decryptString(encrypted, cipher, initialVector);
    }

    private EncryptionResult enryptToByteArray(String string, Long encryptionPoolId) {
        PersistentCipher cipher = scheduleEncryptionPool.getCipherForPoolId(encryptionPoolId);
        if (cipher == null) {
            throw new IllegalStateException("There was no registered cipher entry for pool id: %d".formatted(encryptionPoolId));
        }
        return encryptionSupport.encryptString(string, cipher);
    }

    public ScheduleEncryptionResult rotateEncryption(byte[] data, Long oldCipherPoolId, InitializationVector oldInitialVector)
            throws ScheduleEncryptionException {
        if (latestCipherPoolId == null) {
            throw new IllegalStateException("latest cipher pool id is null!");
        }
        long newCipherPoolId = latestCipherPoolId;

        PersistentCipher oldCipher = scheduleEncryptionPool.getCipherForPoolId(oldCipherPoolId);
        PersistentCipher newCipher = scheduleEncryptionPool.getCipherForPoolId(newCipherPoolId);

        if (oldCipher == null) {
            throw new ScheduleEncryptionException("Old cipher not available: " + oldCipherPoolId);
        }
        if (newCipher == null) {
            throw new ScheduleEncryptionException("New cipher not available: " + newCipherPoolId);
        }

        InitializationVector newInitialVector = newCipher.createNewInitializationVector();

        EncryptionRotationSetup rotateSetup = EncryptionRotationSetup.builder().newCipher(newCipher).oldCipher(oldCipher).oldInitialVector(oldInitialVector)
                .newInitialVector(newInitialVector).build();

        byte[] newEncrypted = rotator.rotate(data, rotateSetup);
        EncryptionResult res = new EncryptionResult(newEncrypted, newInitialVector);

        return new ScheduleEncryptionResult(newCipherPoolId, res);
    }

}
