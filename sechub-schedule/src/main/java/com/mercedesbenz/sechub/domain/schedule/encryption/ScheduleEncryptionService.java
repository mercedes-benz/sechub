// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.EncryptionRotationSetup;
import com.mercedesbenz.sechub.commons.encryption.EncryptionRotator;
import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipher;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;
import com.mercedesbenz.sechub.commons.encryption.SecretKeyProvider;
import com.mercedesbenz.sechub.domain.schedule.ScheduleShutdownService;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubSecretKeyProviderFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminStartsEncryptionRotation;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseScheduleEncryptionPoolRefresh;

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
    ScheduleLatestCipherPoolDataCalculator latestCipherPoolDataCalculator;

    @Autowired
    EncryptionRotator rotator;

    @Autowired
    PersistentCipherFactory cipherFactory;

    @Autowired
    SecHubSecretKeyProviderFactory secretKeyProviderFactory;

    @Autowired
    SecHubOutdatedEncryptionPoolSupport outdatedEncryptionPoolSupport;

    @Autowired
    ScheduleShutdownService shutdownService;

    @Autowired
    @Lazy
    DomainMessageService domainMessageService;

    Long latestCipherPoolId;

    ScheduleEncryptionPool scheduleEncryptionPool;

    @EventListener(ApplicationStartedEvent.class)
    @UseCaseScheduleEncryptionPoolRefresh(@Step(number = 1, next = 3, name = "Init encryption pool", description = "Encryption pool is created on startup"))
    void applicationStarted() throws ScheduleEncryptionException {
        // If a new started server is not able to handle all ciphers from cipher pool it
        // will just
        // not start (ensures old and new jobs can always be handled)
        initNewEncryptionPoolOrFail();
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
    @UseCaseScheduleEncryptionPoolRefresh(@Step(number = 2, next = 3, name = "Refresh encryption pool", description = "Encryption pool is refreshed (if necessary)"))
    @UseCaseAdminStartsEncryptionRotation(@Step(number = 5, name = "Refresh encryption pool", description = "Encryption pool is refreshed (necessary because pool changed before this method call)"))
    public void refreshEncryptionPoolAndLatestPoolIdIfNecessary() {

        if (isStillSameEncryptionPool()) {
            LOG.trace("Encryption pool has not changed. No update necessary");
            return;
        }

        LOG.info("Encryption pool has changed, start encryption pool recreation.");

        try {
            initNewEncryptionPoolOrFail();
            LOG.info("Encryption pool has been recreated sucessfully.");

        } catch (Exception e) {
            LOG.warn("Was not able to refresh encryption pool. Reason: {}", e.getMessage());

            if (outdatedEncryptionPoolSupport.isOutdatedEncryptionStillAllowedOnThisClusterMember()) {
                LOG.info("Failing encrytpion pool initialization is still accepted, will use old existing encryption pool.");
            } else {
                LOG.info("Old (outdated) encryption pool no longer accepted, will trigger shutdown");
                shutdownService.shutdownApplication();
            }

        }

    }

    private boolean isStillSameEncryptionPool() {
        return poolDataProvider.isContainingExactlyGivenPoolIds(scheduleEncryptionPool.getAllPoolIds());
    }

    @IsSendingAsyncMessage(MessageID.SCHEDULE_ENCRYPTION_POOL_INITIALIZED)
    private void initNewEncryptionPoolOrFail() throws ScheduleEncryptionException {
        List<ScheduleCipherPoolData> allPoolDataEntries = poolDataProvider.ensurePoolDataAvailable();

        scheduleEncryptionPool = scheduleEncryptionPoolFactory.createEncryptionPool(allPoolDataEntries);

        latestCipherPoolId = latestCipherPoolDataCalculator.calculateLatestPoolId(allPoolDataEntries);
        if (latestCipherPoolId == null) {
            throw new IllegalStateException("Was not able to determine latest cipher pool data!");
        }

        /* sanity check */
        if (scheduleEncryptionPool.getCipherForPoolId(latestCipherPoolId) == null) {
            throw new IllegalStateException("Encryption pool has no entry for latest cipher pool id: %d".formatted(latestCipherPoolId));
        }

        LOG.info("Encryption pool created ({} pool entries)", allPoolDataEntries.size());

        /* send message about new pool creation */
        DomainMessage message = new DomainMessage(MessageID.SCHEDULE_ENCRYPTION_POOL_INITIALIZED);
        domainMessageService.sendAsynchron(message);
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

    /**
     * Creates new initial cipher pool data entity. Will automatically check if
     * encryption process can be done with this instance and given test text. Pool
     * data will contain all relevant data, except information about user which was
     * responsible for the new pool data.
     *
     * @param data     encryption data
     * @param testText text to be used for initial test
     * @return initial pool data ready to be stored, but without setting the creator
     * @throws ScheduleEncryptionException if encryption process has any problems
     */
    public ScheduleCipherPoolData createInitialCipherPoolData(SecHubEncryptionData data, String testText) throws ScheduleEncryptionException {

        ScheduleCipherPoolData poolData = new ScheduleCipherPoolData();
        poolData.algorithm = data.getAlgorithm();
        poolData.created = LocalDateTime.now();
        poolData.passwordSourceData = data.getPasswordSourceData();
        poolData.secHubCipherPasswordSourceType = data.getPasswordSourceType();

        poolData.testText = testText;

        PersistentCipherType cipherType = poolData.getAlgorithm().getType();
        SecretKeyProvider secretKey = secretKeyProviderFactory.createSecretKeyProvider(cipherType, poolData.getPasswordSourceType(),
                poolData.getPasswordSourceData());
        PersistentCipher tempCipher = cipherFactory.createCipher(secretKey, cipherType);

        EncryptionResult result = encryptionSupport.encryptString(poolData.testText, tempCipher);
        poolData.testInitialVector = result.getInitialVector().getInitializationBytes();
        poolData.testEncrypted = result.getEncryptedData();

        // sanity check
        String decrypted = encryptionSupport.decryptString(poolData.testEncrypted, tempCipher, new InitializationVector(poolData.getTestInitialVector()));
        if (decrypted == null) {
            throw new ScheduleEncryptionException("Was not able to instantiate new cipher pool data, because decrypted value is null!");
        }
        if (!decrypted.equals(poolData.testText)) {
            throw new ScheduleEncryptionException("Was not able to instantiate new cipher pool data, because decrypted value is not origin test text!");
        }
        return poolData;

    }

    public Set<Long> getCurrentEncryptionPoolIds() {
        return scheduleEncryptionPool.getAllPoolIds();
    }

}
