// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;

/**
 * Provides cipher pool data
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class ScheduleCipherPoolDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleCipherPoolDataProvider.class);

    @Autowired
    ScheduleCipherPoolDataRepository repository;

    /**
     * Resolves all existing cipher pool data from database. If none is available, a
     * fallback will be persisted additionally.
     *
     * @return list of cipher pool data, never empty or <code>null</code>
     */
    public List<ScheduleCipherPoolData> ensurePoolDataAvailable() {
        List<ScheduleCipherPoolData> allPoolDataEntries = repository.findAll();

        if (allPoolDataEntries.isEmpty()) {

            ScheduleCipherPoolData fallbackEntry = createFallback();
            repository.save(fallbackEntry);

            LOG.warn(
                    "No pool data entry found - created and stored fallback with algorithm: {}. Remark: This may only happen inside tests! Real server have default initialized by SQL scripts!",
                    fallbackEntry.getAlgorithm());
            allPoolDataEntries = List.of(fallbackEntry);
        }

        if (allPoolDataEntries.isEmpty()) {
            throw new IllegalStateException("Found no pool data entry - should never happen");
        }
        return allPoolDataEntries;
    }

    private ScheduleCipherPoolData createFallback() {
        ScheduleCipherPoolData fallbackEntry = new ScheduleCipherPoolData();
        fallbackEntry.id = Long.valueOf(0);
        fallbackEntry.secHubCipherPasswordSourceType = SecHubCipherPasswordSourceType.NONE;
        fallbackEntry.testText = "fallback";
        fallbackEntry.testEncrypted = "fallback".getBytes();
        fallbackEntry.algorithm = SecHubCipherAlgorithm.NONE;
        fallbackEntry.created = LocalDateTime.now();
        fallbackEntry.createdFrom = null;
        fallbackEntry.testInitialVector = null;
        fallbackEntry.version = Integer.valueOf(0);
        return fallbackEntry;
    }

    /**
     * Checks if given set of pool ids are exactly the same pool ids available
     * inside database
     *
     * @param currentPoolIds
     * @return <code>true</code> when same pool ids, <code>false</code> if there is
     *         any difference
     * @throws IllegalArgumentException if current pool id set is <code>null</code>
     */
    public boolean isContainingExactlyGivenPoolIds(Set<Long> currentPoolIds) {
        if (currentPoolIds == null) {
            throw new IllegalArgumentException("Current pool ids may not be null!");
        }

        Set<Long> poolIdsFromDatabase = repository.fetchAllCipherPoolIds();

        int dbSize = poolIdsFromDatabase.size();
        int memorySize = currentPoolIds.size();

        if (dbSize != memorySize) {
            LOG.debug("Pool size differs. Memory: {}, Database: {}", memorySize, dbSize);
            return false;
        }

        boolean allPoolIdsContained = poolIdsFromDatabase.containsAll(currentPoolIds);

        LOG.trace("all pool ids contained: {}, found pool ids in db: {}", allPoolIdsContained, poolIdsFromDatabase);

        return allPoolIdsContained;
    }

}